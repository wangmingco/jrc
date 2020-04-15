package co.wangming.jrc.classloader;

import co.wangming.jrc.manager.springboot.SpringBootLauncher;

public class ClassLoaderUtil {

    private static JrcClassLoader classLoader = null;

    public static JrcClassLoader getClassLoader() {

        if (classLoader == null) {
            try {
                Class.forName("org.springframework.boot.SpringApplication");
                new SpringBootLauncher().launch();
                return classLoader;
            } catch (Exception e) {
                return new JrcCommonClassLoader();
            }
        } else {
            return classLoader;
        }
    }

    public static void setClassLoader(JrcClassLoader classLoader) {
        ClassLoaderUtil.classLoader = classLoader;
    }
}
