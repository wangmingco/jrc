package co.wangming.jrc.classloader;

public interface JrcClassLoader {

    public Class defineClass(String name, byte[] b);

    public ClassLoader getClassLoader();
}
