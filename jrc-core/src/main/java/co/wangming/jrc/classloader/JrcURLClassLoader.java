package co.wangming.jrc.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class JrcURLClassLoader extends JrcClassLoader {

    private ClassLoader classLoader;

    public JrcURLClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Class defineClass(String name, byte[] b) {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
        }
        return defineClass(name, b, 0, b.length);
    }

    @Override
    public void setURLClassLoader(JrcClassLoader classLoader) {

    }

    @Override
    public Class<?> jrcLoadClass(String name) throws ClassNotFoundException {
        return classLoader.loadClass(name);
    }

    @Override
    public URL jrcGetResource(String name) {
        return classLoader.getResource(name);
    }

    @Override
    public Enumeration<URL> jrcGetResources(String name) throws IOException {
        return classLoader.getResources(name);
    }

    @Override
    public InputStream jrcGetResourceAsStream(String name) {
        return classLoader.getResourceAsStream(name);
    }

    @Override
    public void jrcSetDefaultAssertionStatus(boolean enabled) {
        classLoader.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void jrcSetPackageAssertionStatus(String packageName, boolean enabled) {
        classLoader.setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public void jrcSetClassAssertionStatus(String className, boolean enabled) {
        classLoader.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void jrcClearAssertionStatus() {
        classLoader.clearAssertionStatus();
    }

}
