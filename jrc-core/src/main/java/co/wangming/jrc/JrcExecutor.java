package co.wangming.jrc;


import co.wangming.jrc.classloader.ClassLoaderFactory;
import co.wangming.jrc.classloader.JrcClassLoader;
import co.wangming.jrc.manager.JavaFileManagerFactory;
import co.wangming.jrc.manager.JrcJavaFileManager;
import com.alibaba.fastjson.JSON;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.strobel.assembler.metadata.Buffer;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created By WangMing On 2019/1/17
 **/
public enum JrcExecutor {

    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(JrcExecutor.class);

    private final Map<String, Map<String, ClassWrapper>> classBytesCache = new HashMap<>();

    private static final List<String> skip = new ArrayList() {{
        for (Method method : Object.class.getMethods()) {
            add(method.getName());
        }
        add("clone");
        add("finalize");
    }};

    private static class ClassWrapper {
        byte[] classBytes;
        Class targetClass;
    }

    private synchronized Map<String, Map<String, ClassWrapper>> getClassBytesCache() {
        return new HashMap<>(classBytesCache);
    }

    /***************************************************************************************************
     ***************************************   Class Path      *****************************************
     ***************************************************************************************************/
    private String classpath;

    {
        URLClassLoader parentClassLoader = (URLClassLoader) getClass().getClassLoader();
        Stream.of(parentClassLoader.getURLs()).map(URL::getFile).forEach(this::appendClassPath);

    }

    public JrcResult appendClassPath(byte[] bytes) {

        try {
            File pathDir = new File("./lib");
            if (!pathDir.exists()) {
                if (!pathDir.mkdirs()) {
                    return JrcResult.error("创建'./lib'失败");
                }

            }

            String libName = "lib_" + System.currentTimeMillis() + ".jar";
            File libFile = new File("./lib/" + libName);
            FileOutputStream fileOutputStream = new FileOutputStream(libFile);
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();

            appendClassPath(libFile.getCanonicalPath());
            addRuntimeClassPath(libFile.getCanonicalPath());

            return JrcResult.success(getAllCallpath());
        } catch (Exception e) {
            logger.error("", e);
            return JrcResult.error(e.getMessage());
        }
    }

    private void appendClassPath(String cp) {
        classpath += File.pathSeparator + cp;
    }

    private void addRuntimeClassPath(String jar) throws Exception {
        File file = new File(jar);
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(ClassLoader.getSystemClassLoader(), file.toURI().toURL());
    }

    private String getAllCallpath() {
        Properties properties = System.getProperties();
        Object allClassPath = properties.get("java.class.path");
        String str = allClassPath.toString();
        str = str.replace(":", "\n");
        return str;
    }


    /***************************************************************************************************
     ***************************************   编译(Java编译成Class)      *******************************
     ***************************************************************************************************/
    public JrcResult compile(String javaCode) throws Exception {

        logger.info("编译源码:{}", javaCode);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        JrcJavaFileManager fileManager = JavaFileManagerFactory.getJavaFileManager(compiler.getStandardFileManager(diagnostics, null, null));

        ClassInfo classInfo = getClassInfoFromJavaSource(javaCode);
        List<JavaFileObject> javaFileObjects = new ArrayList<>();
        javaFileObjects.add(new StringJavaFileObject(classInfo.className, javaCode));

        //使用编译选项可以改变默认编译行为。编译选项是一个元素为String类型的Iterable集合
        List<String> options = new ArrayList<>();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-classpath");
        options.add(classpath);

        StringWriter outWriter = new StringWriter();
        JavaCompiler.CompilationTask task = compiler.getTask(outWriter, fileManager, diagnostics, options, null, javaFileObjects);
        // 编译源程序
        boolean success = task.call();

        if (success) {
            //如果编译成功，用类加载器加载该类
            BytesJavaFileObject jco = fileManager.getJavaClassObject(classInfo.className);
            Map<String, ClassWrapper> cache = cacheClass(classInfo.className, jco.getBytes(), "Java");

            logger.info("编译完成:{}, {}", classInfo.className, cache.keySet());
            return JrcResult.success("ok");

        } else {
            //如果想得到具体的编译错误，可以对Diagnostics进行扫描
            StringBuilder error = new StringBuilder();
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                error.append(compilePrint(diagnostic));
            }
            logger.error("编译失败. \noutWriter:{} \ndiagnostics info:{}", outWriter.toString(), error.toString());
            return JrcResult.error(error.toString());
        }

    }

    private synchronized Map<String, ClassWrapper> cacheClass(String className, byte[] classBytes, String type) {

        if (classBytes == null || classBytes.length == 0) {
            return null;
        }

        if (classBytes[0] != -54 &&
                classBytes[1] != -2 &&
                classBytes[2] != -70 &&
                classBytes[3] != -66
        ) {
            return null;
        }

        JrcClassLoader defineClassLoader = ClassLoaderFactory.getClassLoader();

        Class<?> clazz = defineClassLoader.defineClass(className, classBytes);
        if (clazz == null) {
            logger.error("defineClass 失败:{}", className);
            return null;
        }

        ClassWrapper classWrapper = new ClassWrapper();
        classWrapper.classBytes = classBytes;
        classWrapper.targetClass = clazz;

        Map<String, ClassWrapper> cache = classBytesCache.get(className);
        if (cache == null) {
            cache = new HashMap<>();
            cache.put(type + "_V1", classWrapper);
            classBytesCache.put(className, cache);
        } else {

            cache.put(type + "_V" + (cache.size() + 1), classWrapper);
        }

        return cache;
    }

    private ClassWrapper getClassByNameAndVersion(String className, String version) {
        Map<String, ClassWrapper> cache = getClassBytesCache().get(className);
        if (cache == null) {
            return null;
        }
        return cache.get(version);
    }

    private String compilePrint(Diagnostic diagnostic) {
        return "Code:[" + diagnostic.getCode() + "]\n" +
                "Kind:[" + diagnostic.getKind() + "]\n" +
                "Position:[" + diagnostic.getPosition() + "]\n" +
                "Start Position:[" + diagnostic.getStartPosition() + "]\n" +
                "End Position:[" + diagnostic.getEndPosition() + "]\n" +
                "Source:[" + diagnostic.getSource() + "]\n" +
                "Message:[" + diagnostic.getMessage(null) + "]\n" +
                "LineNumber:[" + diagnostic.getLineNumber() + "]\n" +
                "ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n";
    }


    /***************************************************************************************************
     ***************************************   反编译(Class编译成Java)      ******************************
     ***************************************************************************************************/
    public JrcResult decompile(String className, String version) throws Exception {
        ClassWrapper classWrapper = getClassByNameAndVersion(className, version);
        if (classWrapper == null) {
            return JrcResult.error("没有找到目标类");
        }
        final StringWriter writer = new StringWriter();
        final DecompilerSettings settings = DecompilerSettings.javaDefaults();
        DefineClasspathTypeLoader defineClasspathTypeLoader = new DefineClasspathTypeLoader(className, classWrapper.classBytes);
        settings.setTypeLoader(defineClasspathTypeLoader);
        Decompiler.decompile(className, new PlainTextOutput(writer), settings);

        Map<String, Object> map = new HashMap<>();
        map.put("source", writer.toString());
        return JrcResult.success(map);
    }


    public JrcResult cacheClassFile(byte[] classBytes) throws Exception {

        ClassInfo classInfo = getClassInfoFromClassByteCode(classBytes);
        assert classInfo != null;

        Map<String, ClassWrapper> cache = cacheClass(classInfo.className, classBytes, "Class");

        Map<String, Object> map = new HashMap<>();
        map.put("versions", cache.keySet());
        map.put("className", classInfo.className);
        return JrcResult.success(map);
    }

    /**
     * 自定义一个TypeLoader, 用procyon自带的, 加载不到我们要反编译的类
     */
    private static final class DefineClasspathTypeLoader implements ITypeLoader {

        private final byte[] classBytes;
        private String className;

        private DefineClasspathTypeLoader(String className, byte[] classBytes) {
            this.classBytes = classBytes;
            this.className = className;
        }

        @Override
        public boolean tryLoadType(final String internalName, final Buffer buffer) {

            if (!className.equals(internalName)) {
                return false;
            }
            try {
                buffer.putByteArray(classBytes, 0, classBytes.length);
                buffer.flip();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    /***************************************************************************************************
     ***************************************   获取类信息                  ******************************
     ***************************************************************************************************/
    public List<ClassInfo> getClassVersionMethods() {

        List<ClassInfo> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, ClassWrapper>> stringMapEntry : getClassBytesCache().entrySet()) {

            ClassInfo classInfo = new ClassInfo();
            classInfo.className = stringMapEntry.getKey();

            for (Map.Entry<String, ClassWrapper> versionClass : stringMapEntry.getValue().entrySet()) {
                VersionInfo versionInfo = new VersionInfo();
                versionInfo.version = versionClass.getKey();
                for (Method method : versionClass.getValue().targetClass.getMethods()) {
                    if (skip.contains(method.getName())) {
                        continue;
                    }
                    versionInfo.methodNames.add(method.getName());
                }
                classInfo.versions.add(versionInfo);
            }

            result.add(classInfo);
        }

        logger.info("getClassVersionMethods : {}", JSON.toJSONString(result, true));
        return result;
    }

    public static class ClassInfo {
        public String className;
        public List<VersionInfo> versions = new ArrayList<>();
    }

    public static class VersionInfo {
        public String version;
        // 重写的方法 TODO
        public Set<String> methodNames = new HashSet<>();
    }


    private ClassInfo getClassInfoFromClassByteCode(final byte[] bytes) throws IOException, NotFoundException {

        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass cc = cp.makeClass(new ByteArrayInputStream(bytes));
            String className = cc.getName();
            Set<String> methodNames = getMethodNames(cc);

            ClassInfo classInfo = new ClassInfo();
            classInfo.className = className;

            VersionInfo versionInfo = new VersionInfo();
            versionInfo.version = "V1";
            versionInfo.methodNames = methodNames;

            classInfo.versions.add(versionInfo);
            return classInfo;
        } catch (Exception e) {
            logger.error("", e);
            throw e;
        }

    }

    private static Set<String> getMethodNames(CtClass cc) throws NotFoundException {

        Set<String> list = new HashSet<>();
        for (CtMethod method : cc.getMethods()) {
            if (method.getParameterTypes().length != 0) {
                continue;
            }
            if (skip.contains(method.getName())) {
                continue;
            }
            list.add(method.getName());
        }
        return list;
    }


    private static ClassInfo getClassInfoFromJavaSource(String javacode) {

        CompilationUnit cu = JavaParser.parse(javacode);
        MethodVisitor methodVisitor = new MethodVisitor();
        cu.accept(methodVisitor, null);

        ClassInfo classInfo = new ClassInfo();
        if (methodVisitor.packageName != null) {
            classInfo.className = methodVisitor.packageName + "." + methodVisitor.className;
        } else {
            classInfo.className = methodVisitor.className;
        }

        return classInfo;
    }

    private static class MethodVisitor extends VoidVisitorAdapter<Void> {

        private String className;
        String packageName;

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            className = n.getNameAsString();
            super.visit(n, arg);
        }

        @Override
        public void visit(PackageDeclaration n, Void arg) {
            packageName = n.getNameAsString();
            super.visit(n, arg);
        }
    }

    /***************************************************************************************************
     ***************************************  运行方法                     ******************************
     ***************************************************************************************************/

    public JrcResult exec(String className, String version, String methodName) {

        ClassWrapper classWrapper = getClassByNameAndVersion(className, version);
        if (classWrapper == null) {
            return JrcResult.error("没找到class : " + className);
        }

        Class clazz = classWrapper.targetClass;
        try {
            Method method = clazz.getMethod(methodName);
            Object result = null;
            if (Modifier.isStatic(method.getModifiers())) {
                result = method.invoke(clazz);
            } else {
                result = method.invoke(clazz.newInstance());
            }

            if (result != null) {
                return JrcResult.success(result);
            } else {
                return JrcResult.success("执行方法返回类型为void");
            }
        } catch (Exception e) {
            logger.error("", e);
            return JrcResult.error(e.getMessage());
        }
    }
}
