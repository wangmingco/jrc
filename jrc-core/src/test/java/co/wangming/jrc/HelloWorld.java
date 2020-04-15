package co.wangming.jrc;

import org.slf4j.LoggerFactory;

/**
 * 在spring boot环境中，测试加载第三方依赖
 */
public class HelloWorld {

    public static String getLogger() {

        LoggerFactory.getLogger("co.wangming.jrc.HelloWorld");
        LoggerFactory.getLogger(HelloWorld.class);

        return "OK";
    }
}
