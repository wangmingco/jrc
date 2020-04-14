package co.wangming.jrc.manager;

import co.wangming.jrc.BytesJavaFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class JrcJavaFileManager extends ForwardingJavaFileManager {

    private static final Logger logger = LoggerFactory.getLogger(JrcJavaFileManager.class);

    /**
     * 保存编译后Class文件的对象
     */
    private Map<String, BytesJavaFileObject> fileObjectHashMap = new HashMap<>();

    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager delegate to this file manager
     */
    protected JrcJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    public BytesJavaFileObject getJavaClassObject(String className) {
        return fileObjectHashMap.get(className);
    }

    /**
     * 将JavaFileObject对象的引用交给JavaCompiler，让它将编译好后的Class文件装载进来
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {

        BytesJavaFileObject fileObject = fileObjectHashMap.get(className);
        if (fileObject == null) {
            fileObject = new BytesJavaFileObject(className, kind);
            fileObjectHashMap.put(className, fileObject);
            return fileObject;
        } else {
            return fileObject;
        }

    }

    @Override
    public boolean hasLocation(Location location) {
        return super.hasLocation(location);
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
