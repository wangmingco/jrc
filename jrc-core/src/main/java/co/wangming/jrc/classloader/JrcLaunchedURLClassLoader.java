package co.wangming.jrc.classloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JrcLaunchedURLClassLoader implements JrcClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(JrcLaunchedURLClassLoader.class);

    private ClassLoader launchedURLClassLoader;

    public JrcLaunchedURLClassLoader(ClassLoader launchedURLClassLoader) {
        this.launchedURLClassLoader = launchedURLClassLoader;
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
    public ClassLoader getClassLoader() {
        return launchedURLClassLoader;
    }
}
