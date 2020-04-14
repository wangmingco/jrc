package co.wangming.jrc.manager;

import co.wangming.jrc.manager.common.CommonJavaFileManager;
import co.wangming.jrc.manager.springboot.SpringBootJavaFileManager;

import javax.tools.StandardJavaFileManager;

public class JavaFileManagerFactory {

    public static JrcJavaFileManager getJavaFileManager(StandardJavaFileManager standardManager) {
        try {
            Class.forName("org.springframework.boot.SpringApplication");
            return new SpringBootJavaFileManager(standardManager);
        } catch (ClassNotFoundException e) {
            return new CommonJavaFileManager(standardManager);
        }
    }
}
