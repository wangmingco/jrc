package co.wangming.jrc.manager.common;

import co.wangming.jrc.manager.JrcJavaFileManager;

import javax.tools.JavaFileManager;

public class CommonJavaFileManager extends JrcJavaFileManager {

    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager delegate to this file manager
     */
    public CommonJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

}
