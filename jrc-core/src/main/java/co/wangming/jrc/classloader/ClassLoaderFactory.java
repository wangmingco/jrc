package co.wangming.jrc.classloader;

import co.wangming.jrc.manager.springboot.SpringBootLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaderFactory {

    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderFactory.class);

    public static JrcClassLoader getClassLoader() {
        URL[] urls = getLibUrls();
        URLClassLoader urlClassLoader = new URLClassLoader(urls);
        JrcURLClassLoader jrcURLClassLoader = new JrcURLClassLoader(urlClassLoader);

        try {
            /**
             * 测试是否在springboot环境中，如果是的话，则使用SpringBootLauncher开始spring-loader构建ClassLoader
             */
            Class.forName("org.springframework.boot.SpringApplication");
            new SpringBootLauncher().launch();
            /**
             * {@link SpringBootLauncher#launch(String[], String, ClassLoader)} 将ClassLoader设置到 Thread的contextClassLoader 上
             */
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            JrcLaunchedURLClassLoader classloader = new JrcLaunchedURLClassLoader(currentClassLoader, jrcURLClassLoader);
            logger.info("处于springboot环境中，开启SpringBootLauncher扫描: {}", classloader);
            return classloader;
        } catch (Exception e) {
            logger.info("处于非springboot环境中，直接返回 JrcURLClassLoader: {}", jrcURLClassLoader);
            return jrcURLClassLoader;
        } finally {

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
