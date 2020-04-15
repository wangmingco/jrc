package co.wangming.jrc.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public abstract class JrcClassLoader extends ClassLoader {

    public abstract Class defineClass(String name, byte[] b);

    public abstract void setURLClassLoader(JrcClassLoader classLoader);

    @Override
    public final Class<?> loadClass(String name) throws ClassNotFoundException {
        return jrcLoadClass(name);
    }

    public abstract Class<?> jrcLoadClass(String name) throws ClassNotFoundException;


    @Override
    public final URL getResource(String name) {
        return jrcGetResource(name);
    }

    public abstract URL jrcGetResource(String name);

    @Override
    public final Enumeration<URL> getResources(String name) throws IOException {
        return jrcGetResources(name);
    }

    public abstract Enumeration<URL> jrcGetResources(String name) throws IOException;

    @Override
    public final InputStream getResourceAsStream(String name) {
        return jrcGetResourceAsStream(name);
    }

    public abstract InputStream jrcGetResourceAsStream(String name);

    @Override
    public final void setDefaultAssertionStatus(boolean enabled) {
        jrcSetDefaultAssertionStatus(enabled);
    }

    public abstract void jrcSetDefaultAssertionStatus(boolean enabled);

    @Override
    public final void setPackageAssertionStatus(String packageName, boolean enabled) {
        jrcSetPackageAssertionStatus(packageName, enabled);
    }

    public abstract void jrcSetPackageAssertionStatus(String packageName, boolean enabled);

    @Override
    public final void setClassAssertionStatus(String className, boolean enabled) {
        jrcSetClassAssertionStatus(className, enabled);
    }

    public abstract void jrcSetClassAssertionStatus(String className, boolean enabled);

    @Override
    public final void clearAssertionStatus() {
        jrcClearAssertionStatus();
    }

    public abstract void jrcClearAssertionStatus();
}
