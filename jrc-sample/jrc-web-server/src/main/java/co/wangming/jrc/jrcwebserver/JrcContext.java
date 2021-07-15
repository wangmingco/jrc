package co.wangming.jrc.jrcwebserver;


import co.wangming.jrc.JrcExecutor;
import co.wangming.jrc.JrcResult;

import java.util.List;

public enum JrcContext {

    INSTANCE;

    private JrcExecutor jrcExecutor = new JrcExecutor();

    public JrcResult appendClassPath(byte[] bytes) {
        return jrcExecutor.appendClassPath(bytes);
    }

    public JrcResult compile(String javaCode) throws Exception {
        return jrcExecutor.compile(javaCode);
    }

    public JrcResult cacheClassFile(byte[] classBytes) throws Exception {
        return jrcExecutor.cacheClassFile(classBytes);
    }

    public JrcResult decompile(String className, String version) throws Exception {
        return jrcExecutor.decompile(className, version);
    }

    public JrcResult getClassVersionMethods() {
        try {
            List<JrcExecutor.ClassInfo> classInfo = jrcExecutor.getClassVersionMethods();
            if (classInfo == null) {
                return JrcResult.error("找不到目标类字节码");
            }
            return JrcResult.success(classInfo);
        } catch (Exception e) {
            return JrcResult.error(e.getMessage());
        }
    }

    public JrcResult exec(String className, String version, String methodName) {
        return jrcExecutor.exec(className, version, methodName);
    }


}
