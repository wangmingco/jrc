package co.wangming.jrc.manager.springboot;

import co.wangming.jrc.BytesJavaFileObject;
import co.wangming.jrc.manager.JrcJavaFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 类文件管理器
 * 用于JavaCompiler将编译好后的class，保存到jclassObject中
 */
public class SpringBootJavaFileManager extends JrcJavaFileManager {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootJavaFileManager.class);

    SpringBootLauncher springBootLauncher;
    /**
     * 保存编译后Class文件的对象
     */
    private BytesJavaFileObject bytesJavaFileObject;

    /**
     * 调用父类构造器
     *
     * @param standardManager
     */
    public SpringBootJavaFileManager(StandardJavaFileManager standardManager) {
        super(standardManager);

        try {
            springBootLauncher = new SpringBootLauncher();
            springBootLauncher.launch();
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public BytesJavaFileObject getJavaClassObject() {
        return bytesJavaFileObject;
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return Thread.currentThread().getContextClassLoader();
    }

    @Override
    public boolean hasLocation(Location location) {
        boolean result = super.hasLocation(location);

        return result;
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set set, boolean recurse) throws IOException {

        List<SpringBootArchiveEntry> entries = springBootLauncher.getEntries(packageName.replaceAll(".", "/"));
        logger.info("launcher entries size:{} -> {}", packageName, entries.size());
        List<JavaFileObject> list = entries.stream().map(it -> new JarJavaFileObject(it, JavaFileObject.Kind.CLASS)).collect(Collectors.toList());

        Iterable<JavaFileObject> superList = super.list(location, packageName, set, recurse);
        if (superList == null) {
            return list;
        }

        for (JavaFileObject o : superList) {
//            logger.info("JavaFileObject class:{}", o.getClass());
            list.add(o);
        }
//        logger.info("superList entries size:{} -> {}", packageName, list.size());

        return list;


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

        if (bytesJavaFileObject == null) {
            bytesJavaFileObject = new BytesJavaFileObject(className, kind);
            return bytesJavaFileObject;
        } else {
            return bytesJavaFileObject;
        }

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

}
