package co.wangming.jrc;

import org.slf4j.LoggerFactory;

/**
 * 在spring boot环境中，测试加载第三方依赖
 */
public class HelloWorld {

    public static String getLogger() {

        System.out.println(Thread.currentThread().getContextClassLoader());
        LoggerFactory.getLogger("co.wangming.jrc.HelloWorld");
        LoggerFactory.getLogger(HelloWorld.class);

        return "OK";
    }

    public static void main(String[] args) {
        getLogger();
    }
}
