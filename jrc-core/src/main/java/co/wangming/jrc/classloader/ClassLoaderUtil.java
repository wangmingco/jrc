package co.wangming.jrc.classloader;

public class ClassLoaderUtil {

    private static JrcClassLoader classLoader = null;

    public static JrcClassLoader getClassLoader() {
        if (classLoader == null) {
            return new JrcCommonClassLoader();
        } else {
            return classLoader;
        }
    }

    public static void setClassLoader(JrcClassLoader classLoader) {
        ClassLoaderUtil.classLoader = classLoader;
    }
}
