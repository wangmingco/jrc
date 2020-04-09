package co.wangming.jrc.jrcwebserver;

import co.wangming.jrc.Result;
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

	@PostMapping(value = "/decompile")
	public Result decompiler(@RequestParam("file") MultipartFile file) {
		logger.info("decompile");
        try {
            byte[] bytes = IOUtils.toByteArray(file.getInputStream());
            return JrcExecutor.INSTANCE.decompile(bytes);
        } catch (Exception e) {
            logger.info("decompile", e);
            return Result.error(e.getMessage());
        }
	}

	@PostMapping(value = "/compileFile")
	public Result compileFile(@RequestParam("file") MultipartFile file) {
        try {
            String javasource = IOUtils.toString(file.getInputStream(), "UTF8");
            logger.info("compileFile : {}", javasource);
            return JrcExecutor.INSTANCE.compile(javasource);
        } catch (Exception e) {
            logger.info("compileFile", e);
            return Result.error(e.getMessage());
        }
	}

	@PostMapping(value = "/compileSource")
    public Result compileSource(@RequestParam("javasource") String javasource) {
        logger.info("compileSource:{}", javasource);
        try {
            return JrcExecutor.INSTANCE.compile(javasource);
        } catch (Exception e) {
            logger.info("compileSource", e);
            return Result.error(e.getMessage());
        }
	}

	@PostMapping(value = "/appendClassPath")
	public Result appendClassPath(@RequestParam("file") MultipartFile file) {

        try {
            byte[] bytes = IOUtils.toByteArray(file.getInputStream());
            logger.info("appendClassPath");
            return JrcExecutor.INSTANCE.appendClassPath(bytes);
        } catch (IOException e) {
            logger.info("appendClassPath", e);
            return Result.error(e.getMessage());
        }

	}

	@PostMapping(value = "/execute")
	public Result queryKeys(@RequestParam("key") String key, @RequestParam("method") String method) {
		logger.info("execute {} - {}", key, method);
        try {
            return JrcExecutor.INSTANCE.exec(key, method);
        } catch (Exception e) {
            logger.info("exec", e);
            return Result.error(e.getMessage());
        }
	}
}