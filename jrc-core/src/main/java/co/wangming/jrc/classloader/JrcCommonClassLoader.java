package co.wangming.jrc.classloader;

public class JrcCommonClassLoader extends ClassLoader implements JrcClassLoader {

    @Override
    public Class defineClass(String name, byte[] b) {
        try {
            return loadClass(name);
        } catch (ClassNotFoundException e) {
        }
        return defineClass(name, b, 0, b.length);
    }

}
