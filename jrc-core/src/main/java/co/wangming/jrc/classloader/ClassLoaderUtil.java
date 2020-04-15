package co.wangming.jrc.classloader;

import co.wangming.jrc.manager.springboot.SpringBootLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaderUtil {

    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderUtil.class);

    private static JrcClassLoader classLoader = null;

    static {
        setClassLoader(null);
    }

    public static JrcClassLoader getClassLoader() {
        return classLoader;
    }

    public static void setClassLoader(ClassLoader currentClassLoader) {
        URL[] urls = getLibUrls();
        URLClassLoader urlClassLoader = new URLClassLoader(urls);
        JrcURLClassLoader jrcURLClassLoader = new JrcURLClassLoader(urlClassLoader);

        try {
            Class.forName("org.springframework.boot.SpringApplication");

            if (currentClassLoader == null && classLoader == null) {
                new SpringBootLauncher().launch();
                logger.info("当前classloader为空，处于springboot环境中，开启SpringBootLauncher扫描。新的：{}", classLoader);
                currentClassLoader = Thread.currentThread().getContextClassLoader();
                classLoader = new JrcLaunchedURLClassLoader(currentClassLoader, jrcURLClassLoader);

            } else if (classLoader != null) {
                classLoader.setURLClassLoader(jrcURLClassLoader);
            } else {

            }

        } catch (Exception e) {
            classLoader = jrcURLClassLoader;
        } finally {
            logger.info("class loader 设置完成：{}", classLoader);
        }

    }

    private static URL[] getLibUrls() {
        URL[] urls = new URL[]{};
        File file = new File("./lib/");
        if (file.exists()) {
            File[] files = file.listFiles();
            final int length = files.length;
            urls = new URL[length];
            for (int i = 0; i < length; i++) {
                try {
                    urls[i] = files[i].toURI().toURL();
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return urls;
    }
}
