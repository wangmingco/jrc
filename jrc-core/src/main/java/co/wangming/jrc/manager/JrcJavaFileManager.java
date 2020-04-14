package co.wangming.jrc.manager;

import co.wangming.jrc.BytesJavaFileObject;

import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;

public abstract class JrcJavaFileManager extends ForwardingJavaFileManager {

    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager delegate to this file manager
     */
    protected JrcJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    public abstract BytesJavaFileObject getJavaClassObject();
}
