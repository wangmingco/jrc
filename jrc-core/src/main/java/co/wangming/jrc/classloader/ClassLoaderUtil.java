package co.wangming.jrc.classloader;

import co.wangming.jrc.manager.springboot.SpringBootLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassLoaderUtil {

    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderUtil.class);

    private static JrcClassLoader classLoader = null;

    public static JrcClassLoader getClassLoader() {

        if (classLoader == null) {
            try {
                Class.forName("org.springframework.boot.SpringApplication");
                new SpringBootLauncher().launch();
                logger.info("当前classloader为空，处于springboot环境中，开启SpringBootLauncher扫描。新的：{}", classLoader);
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
