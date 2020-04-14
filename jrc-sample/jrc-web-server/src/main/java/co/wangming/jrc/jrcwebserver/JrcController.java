package co.wangming.jrc.jrcwebserver;

import co.wangming.jrc.JrcResult;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created By WangMing On 2019/1/16
 **/
@RestController
@RequestMapping("/jrc")
public class JrcController {

	private static final Logger logger = LoggerFactory.getLogger(JrcController.class);

	@PostMapping(value = "/uploadClassFile")
	public JrcResult uploadClassFile(@RequestParam("file") MultipartFile file) {
		logger.info("decompile");
        try {
            byte[] bytes = IOUtils.toByteArray(file.getInputStream());
            return JrcContext.INSTANCE.decompile(bytes);
        } catch (Exception e) {
            logger.info("decompile", e);
            return JrcResult.error(e.getMessage());
        }
	}

	@PostMapping(value = "/uploadJavaFile")
	public JrcResult uploadJavaFile(@RequestParam("file") MultipartFile file) {
        try {
            String javasource = IOUtils.toString(file.getInputStream(), "UTF8");
            logger.info("compileFile : {}", javasource);
            return JrcContext.INSTANCE.compile(javasource);
        } catch (Exception e) {
            logger.info("compileFile", e);
            return JrcResult.error(e.getMessage());
        }
	}

	@PostMapping(value = "/uploadJavaSource")
    public JrcResult uploadJavaSource(@RequestParam("javasource") String javasource) {
        logger.info("compileSource:{}", javasource);
        try {
            return JrcContext.INSTANCE.compile(javasource);
        } catch (Exception e) {
            logger.info("compileSource", e);
            return JrcResult.error(e.getMessage());
        }
	}

	@PostMapping(value = "/uploadJarFile")
	public JrcResult uploadJarFile(@RequestParam("file") MultipartFile file) {

        try {
            byte[] bytes = IOUtils.toByteArray(file.getInputStream());
            logger.info("appendClassPath");
            return JrcContext.INSTANCE.appendClassPath(bytes);
        } catch (IOException e) {
            logger.info("appendClassPath", e);
            return JrcResult.error(e.getMessage());
        }

	}

	@PostMapping(value = "/executeMethod")
	public JrcResult executeMethod(@RequestParam("key") String key, @RequestParam("method") String method) {
		logger.info("execute {} - {}", key, method);
        try {
            return JrcContext.INSTANCE.exec(key, method);
        } catch (Exception e) {
            logger.info("exec", e);
            return JrcResult.error(e.getMessage());
        }
	}
}