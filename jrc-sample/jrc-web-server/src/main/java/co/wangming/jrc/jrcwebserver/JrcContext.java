package co.wangming.jrc.jrcwebserver;


import co.wangming.jrc.JrcExecutor;
import co.wangming.jrc.JrcResult;

import java.util.HashMap;
import java.util.Map;

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

    public JrcResult classInfo(String className, String version) {
        try {
            JrcExecutor.ClassInfo classInfo = jrcExecutor.getClassInfoFromClassByteCode(className, version);
            if (classInfo == null) {
                return JrcResult.error("找不到目标类字节码");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("methodNames", classInfo.methodNames);
            return JrcResult.success(map);
        } catch (Exception e) {
            return JrcResult.error(e.getMessage());
        }
    }

    public JrcResult exec(String className, String version, String methodName) {
        return jrcExecutor.exec(className, version, methodName);
    }


}
