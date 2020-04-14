package co.wangming.jrc.manager.common;

import co.wangming.jrc.BytesJavaFileObject;
import co.wangming.jrc.manager.JrcJavaFileManager;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;

public class CommonJavaFileManager extends JrcJavaFileManager {


    private BytesJavaFileObject jclassObject;

    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager delegate to this file manager
     */
    public CommonJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    /**
     * 将JavaFileObject对象的引用交给JavaCompiler，让它将编译好后的Class文件装载进来
     *
     * @param location
     * @param className
     * @param kind
     * @param sibling
     * @return
     * @throws IOException
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling)
            throws IOException {
        if (jclassObject == null)
            jclassObject = new BytesJavaFileObject(className, kind);
        return jclassObject;
    }

    @Override
    public BytesJavaFileObject getJavaClassObject() {
        return jclassObject;
    }
}
