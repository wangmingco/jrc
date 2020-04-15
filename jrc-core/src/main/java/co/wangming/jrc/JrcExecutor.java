package co.wangming.jrc;


import co.wangming.jrc.classloader.ClassLoaderUtil;
import co.wangming.jrc.classloader.JrcClassLoader;
import co.wangming.jrc.manager.JavaFileManagerFactory;
import co.wangming.jrc.manager.JrcJavaFileManager;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Created By WangMing On 2019/1/17
 **/
public class JrcExecutor {

    private static final Logger logger = LoggerFactory.getLogger(JrcExecutor.class);

    private final Map<String, Map<String,byte[]>> classBytesCache = new ConcurrentHashMap<>();

    private static final List<String> skip = new ArrayList() {{
        for (Method method : Object.class.getMethods()) {
            add(method.getName());
        }
        add("clone");
        add("finalize");
    }};

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

            ClassLoaderUtil.setClassLoader(null);

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

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        JrcJavaFileManager fileManager = JavaFileManagerFactory.getJavaFileManager(compiler.getStandardFileManager(diagnostics, null, null));

        ClassInfo classInfo = getClassFileFromJavaSource(javaCode);
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
            Map<String, byte[]> cache = cacheCompiledClassData(classInfo.className, jco.getBytes(), "Java");
            Map<String, Object> map = new HashMap<>();
            map.put("versions", cache.keySet());
            map.put("className", classInfo.className);
            return JrcResult.success(map);

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

    public Map<String, byte[]> cacheCompiledClassData(String className, byte[] classBytes, String type) {

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

        Map<String, byte[]> cache = classBytesCache.get(className);
        if (cache == null) {
            cache = new HashMap<>();
            cache.put(className + "_V1_" + type, classBytes);
            classBytesCache.put(className, cache);
        } else {

            cache.put(className + "_V" + (cache.size() + 1) + "_" + type, classBytes);
        }

        return cache;
    }

    private byte[] getClassBytes(String className, String version) {
        Map<String, byte[]> cache = classBytesCache.get(className);
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
        byte[] bytes = getClassBytes(className, version);
        if (bytes == null) {
            return JrcResult.error("没有找到目标类");
        }
        final StringWriter writer = new StringWriter();
        final DecompilerSettings settings = DecompilerSettings.javaDefaults();
        DefineClasspathTypeLoader defineClasspathTypeLoader = new DefineClasspathTypeLoader(className, bytes);
        settings.setTypeLoader(defineClasspathTypeLoader);
        Decompiler.decompile(className, new PlainTextOutput(writer), settings);

        Map<String, Object> map = new HashMap<>();
        map.put("source", writer.toString());
        return JrcResult.success(map);
    }


    public JrcResult cacheClassFile(byte[] classBytes) throws Exception {

        ClassInfo classInfo = getClassInfoFromClassByteCode(classBytes);
        assert classInfo != null;

        Map<String, byte[]> cache = cacheCompiledClassData(classInfo.className, classBytes, "Class");

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
    public ClassInfo getClassInfoFromClassByteCode(String className, String version) throws IOException, NotFoundException {
        Map<String, byte[]> cache = classBytesCache.get(className);
        if (cache == null) {
            return null;
        }
        byte[] bytes = cache.get(version);
        if (bytes == null) {
            return null;
        }
        return getClassInfoFromClassByteCode(bytes);
    }

    private ClassInfo getClassInfoFromClassByteCode(final byte[] bytes) throws IOException, NotFoundException {

        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass cc = cp.makeClass(new ByteArrayInputStream(bytes));
            String className = cc.getName();
            List<String> methodNames = getMethodNames(cc);

            ClassInfo classInfo = new ClassInfo();
            classInfo.className = className;
            classInfo.methodNames = methodNames;
            return classInfo;
        } catch (Exception e) {
            logger.error("", e);
            throw e;
        }

    }

    private static List<String> getMethodNames(CtClass cc) throws NotFoundException {

        List<String> list = new ArrayList<>();
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


    private static ClassInfo getClassFileFromJavaSource(String javacode) {

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


    public static final class ClassInfo {
        public String className;
        public List<String> methodNames;
    }

    /***************************************************************************************************
     ***************************************  运行方法                     ******************************
     ***************************************************************************************************/

    public JrcResult exec(String className, String version, String methodName) {

        byte[] classBytes = getClassBytes(className, version);
        if (classBytes == null) {
            return JrcResult.error("没找到class : " + className);
        }

        JrcClassLoader defineClassLoader = ClassLoaderUtil.getClassLoader();

        Class<?> clazz = defineClassLoader.defineClass(className, classBytes);
        if (clazz == null) {
            logger.error("defineClass 失败:{}", className);
            return JrcResult.error("defineClass 失败: " + className);
        }

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
