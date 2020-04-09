package co.wangming.jrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * 类文件管理器
 * 用于JavaCompiler将编译好后的class，保存到jclassObject中
 */
public class JavaFileCacheManager extends ForwardingJavaFileManager {

    private static final Logger logger = LoggerFactory.getLogger(JavaFileCacheManager.class);

    /**
     * 保存编译后Class文件的对象
     */
    private BytesJavaFileObject bytesJavaFileObject;

    private SpringClassLoader springClassLoader = new SpringClassLoader();

    /**
     * 调用父类构造器
     *
     * @param standardManager
     */
    public JavaFileCacheManager(StandardJavaFileManager standardManager) {
        super(standardManager);
    }

    public BytesJavaFileObject getJavaClassObject() {
        return bytesJavaFileObject;
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        if (StandardLocation.CLASS_PATH.equals(location)) {
            return springClassLoader;
        } else {
            return super.getClassLoader(location);
        }
    }

    @Override
    public boolean hasLocation(Location location) {
        boolean result = super.hasLocation(location);
//		logger.info("hasLocation : {} --> {}", location, result);

        return result;
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set set, boolean recurse) throws IOException {
        if (StandardLocation.CLASS_PATH.equals(location) && !"".equals(packageName) && !packageName.startsWith("java")) {
            // 如果是要加载ClassPath上的内容， 则自定义处理Spring
            String classPath = packageName.replace(".", "/");
            ClassPathResource classPathResource = new ClassPathResource(classPath);
            File file = classPathResource.getFile();
            logger.info("list file1 :{}, {}, {}, {}", packageName, file.getName(), file.getPath(), file.isDirectory());
            File[] files = file.listFiles();

            for (File file1 : files) {
                file1.getName();
                logger.info("list file2 :{}, {}, {}", packageName, file1.getName(), file1.getPath());
            }

            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            return null;
        } else {
            return super.list(location, packageName, set, recurse);
        }

    }

    /**
     * 将 JavaFileObject 转换成className
     *
     * @param location PLATFORM_CLASS_PATH
     * @param file     /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/ct.sym(META-INF/sym/rt.jar/java/lang/Comparable.class)
     * @return java.lang.Comparable
     */
    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        String inferBinaryName = super.inferBinaryName(location, file);

        return inferBinaryName;
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        logger.info("isSameFile : {}", a);

        return super.isSameFile(a, b);
    }

    /**
     * 处理 编译命令的参数，例如-encoding， -classpath 等
     *
     * @param current   参数名，如 -classpath
     * @param remaining 参数值列表
     * @return
     */
    @Override
    public boolean handleOption(String current, Iterator remaining) {
        return super.handleOption(current, remaining);
    }

    @Override
    public int isSupportedOption(String option) {
        logger.info("isSupportedOption : {}", option);

        return super.isSupportedOption(option);
    }

    /**
     * 将JavaFileObject对象的引用交给JavaCompiler，让它将编译好后的Class文件装载进来
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {

        if (bytesJavaFileObject == null)
            bytesJavaFileObject = new BytesJavaFileObject(className, kind);

        return bytesJavaFileObject;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        logger.info("getJavaFileForInput : {}", location);

        return super.getJavaFileForInput(location, className, kind);
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        logger.info("getFileForInput : {}", location);

        return super.getFileForInput(location, packageName, relativeName);
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        logger.info("getFileForOutput : {}", location);

        return super.getFileForOutput(location, packageName, relativeName, sibling);
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    private static class SpringClassLoader extends ClassLoader {

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            logger.info("load class:{}", name);

            String classPath = name.replace(".", "/");
            classPath = classPath + ".class";
            ClassPathResource classPathResource = new ClassPathResource(classPath);
            return classPathResource.getClassLoader().loadClass(name);
        }

    }
}
