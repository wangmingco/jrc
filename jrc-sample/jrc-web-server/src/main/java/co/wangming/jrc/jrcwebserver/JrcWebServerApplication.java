package co.wangming.jrc.jrcwebserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class JrcWebServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(JrcWebServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(JrcWebServerApplication.class, args);
    }

    @Configuration
    public static class JrcWebMvcConfiguration implements WebMvcConfigurer {

        @Bean
        public JrcHttpTraceLogFilter httpTraceLogFilter() {
            logger.info("开启http trace log");
            return new JrcHttpTraceLogFilter();
        }
    }
}
