package co.wangming.jrc.jrcwebserver;


import co.wangming.jrc.JrcExecutor;
import co.wangming.jrc.JrcResult;

public enum JrcContext {

    INSTANCE;

    private JrcExecutor jrcContext = new JrcExecutor();

    public JrcResult appendClassPath(byte[] bytes) {
        return jrcContext.appendClassPath(bytes);
    }

    public JrcResult compile(String javaCode) throws Exception {
        return jrcContext.compile(javaCode);
    }

    public JrcResult decompile(byte[] classBytes) throws Exception {
        return jrcContext.decompile(classBytes);
    }

    public JrcResult exec(String className, String methodName) {
        return jrcContext.exec(className, methodName);
    }


}
