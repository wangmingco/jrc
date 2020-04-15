package co.wangming.jrc.classloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

public class JrcLaunchedURLClassLoader extends JrcClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(JrcLaunchedURLClassLoader.class);

    private ClassLoader launchedURLClassLoader;
    private JrcClassLoader jrcUrlClassLoader;

    public JrcLaunchedURLClassLoader(ClassLoader launchedURLClassLoader, JrcClassLoader jrcUrlClassLoader) {
        this.launchedURLClassLoader = launchedURLClassLoader;
        this.jrcUrlClassLoader = jrcUrlClassLoader;
    }

    public Class defineClass(String name, byte[] b) {
        try {
            return launchedURLClassLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
        }
        try {
            Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class});
            boolean isAccessible = defineClassMethod.isAccessible();
            if (!isAccessible) {
                defineClassMethod.setAccessible(true);
            }

            Object result = defineClassMethod.invoke(launchedURLClassLoader, name, b, 0, b.length);
            defineClassMethod.setAccessible(isAccessible);

            return (Class) result;
        } catch (NoSuchMethodException e) {
            logger.error("defineClass name:{}", name, e);
            return null;
        } catch (IllegalAccessException e) {
            logger.error("defineClass name:{}", name, e);
            return null;
        } catch (InvocationTargetException e) {
            logger.error("defineClass name:{}", name, e);
            return null;
        }
    }

    @Override
    public void setURLClassLoader(JrcClassLoader classLoader) {
        jrcUrlClassLoader = classLoader;
    }

    @Override
    public Class<?> jrcLoadClass(String name) throws ClassNotFoundException {
        try {
            return launchedURLClassLoader.loadClass(name);
        } catch (Exception e) {
            return jrcUrlClassLoader.loadClass(name);
        }
    }

    @Override
    public URL jrcGetResource(String name) {
        try {
            return launchedURLClassLoader.getResource(name);
        }catch (Exception e) {
            return jrcUrlClassLoader.getResource(name);
        }

    }

    @Override
    public Enumeration<URL> jrcGetResources(String name) throws IOException {
        try {
            return launchedURLClassLoader.getResources(name);
        }catch (Exception e) {
            return jrcUrlClassLoader.getResources(name);
        }

    }

    @Override
    public InputStream jrcGetResourceAsStream(String name) {
        try {
            return launchedURLClassLoader.getResourceAsStream(name);
        }catch (Exception e) {
            return jrcUrlClassLoader.getResourceAsStream(name);
        }

    }

    @Override
    public void jrcSetDefaultAssertionStatus(boolean enabled) {
        try {
            launchedURLClassLoader.setDefaultAssertionStatus(enabled);
        }catch (Exception e) {

        }
        try {
            jrcUrlClassLoader.setDefaultAssertionStatus(enabled);
        }catch (Exception e) {

        }

    }

    @Override
    public void jrcSetPackageAssertionStatus(String packageName, boolean enabled) {
        try {
            launchedURLClassLoader.setPackageAssertionStatus(packageName, enabled);
        }catch (Exception e) {

        }
        try {
            jrcUrlClassLoader.setPackageAssertionStatus(packageName, enabled);
        }catch (Exception e) {

        }

    }

    @Override
    public void jrcSetClassAssertionStatus(String className, boolean enabled) {
        try {
            launchedURLClassLoader.setClassAssertionStatus(className, enabled);
        }catch (Exception e) {

        }
        try {
            jrcUrlClassLoader.setClassAssertionStatus(className, enabled);
        }catch (Exception e) {

        }

    }

    @Override
    public void jrcClearAssertionStatus() {
        try {
            launchedURLClassLoader.clearAssertionStatus();
        }catch (Exception e) {

        }
        try {
            jrcUrlClassLoader.clearAssertionStatus();
        }catch (Exception e) {

        }

    }
}
