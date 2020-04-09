package co.wangming.jrc.jrcwebserver;

import co.wangming.jrc.JrcContext;
import co.wangming.jrc.Result;

public enum JrcExecutor {

    INSTANCE;

    private JrcContext jrcContext = new JrcContext();

    public Result appendClassPath(byte[] bytes) {
        return jrcContext.appendClassPath(bytes);
    }

    public Result compile(String javaCode) throws Exception {
        return jrcContext.compile(javaCode);
    }

    public Result decompile(byte[] classBytes) throws Exception {
        return jrcContext.decompile(classBytes);
    }

    public Result exec(String className, String methodName) {
        return jrcContext.exec(className, methodName);
    }


}
