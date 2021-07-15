package co.wangming.jrc.jrcwebserver;

import co.wangming.jrc.JrcExecutor;
import co.wangming.jrc.JrcResult;
import co.wangming.jrc.MavenUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
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
        try {
            byte[] bytes = IOUtils.toByteArray(file.getInputStream());
            return JrcExecutor.INSTANCE.cacheClassFile(bytes);
        } catch (Exception e) {
            logger.error("decompile error", e);
            return JrcResult.error(e.getMessage());
        }
	}

	@PostMapping(value = "/uploadJavaFile")
	public JrcResult uploadJavaFile(@RequestParam("file") MultipartFile file) {
        try {
            String javasource = IOUtils.toString(file.getInputStream(), "UTF8");
            return JrcExecutor.INSTANCE.compile(javasource);
        } catch (Exception e) {
            logger.error("compileFile error", e);
            return JrcResult.error(e.getMessage());
        }
	}

	@PostMapping(value = "/uploadJavaSource")
    public JrcResult uploadJavaSource(@RequestBody String javasource) {
        try {
            return JrcExecutor.INSTANCE.compile(javasource);
        } catch (Exception e) {
            logger.error("compileSource error", e);
            return JrcResult.error(e.getMessage());
        }
	}

	@PostMapping(value = "/uploadJarFile")
	public JrcResult uploadJarFile(@RequestParam("file") MultipartFile file) {

        try {
            byte[] bytes = IOUtils.toByteArray(file.getInputStream());
            logger.info("appendClassPath");
            return JrcExecutor.INSTANCE.appendClassPath(bytes);
        } catch (IOException e) {
            logger.error("appendClassPath error", e);
            return JrcResult.error(e.getMessage());
        }

    }

    @PostMapping(value = "/getClassVersionMethods")
    public JrcResult getClassVersionMethods() {
        try {
            return JrcResult.success(JrcExecutor.INSTANCE.getClassVersionMethods());
        } catch (Exception e) {
            logger.error("exec error", e);
            return JrcResult.error(e.getMessage());
        }
    }

    @PostMapping(value = "/decompile")
    public JrcResult decompile(@RequestParam("className") String className, @RequestParam("version") String version) {
        try {
            return JrcExecutor.INSTANCE.decompile(className, version);
        } catch (Exception e) {
            logger.error("exec error", e);
            return JrcResult.error(e.getMessage());
        }
    }

	@PostMapping(value = "/executeMethod")
    public JrcResult executeMethod(@RequestParam("className") String className,
                                   @RequestParam("version") String version,
                                   @RequestParam("method") String method) {
        logger.info("execute {} - {} - {}", className, version, method);
        try {
            return JrcExecutor.INSTANCE.exec(className, version, method);
        } catch (Exception e) {
            logger.error("exec error", e);
            return JrcResult.error(e.getMessage());
        }
	}

    @PostMapping(value = "/searchJar")
    public JrcResult searchJar(@RequestParam("searchJarKeyword") String searchJarKeyword) {
        logger.info("searchJar {}", searchJarKeyword);
        return MavenUtil.searchJar(searchJarKeyword);
    }

    @PostMapping(value = "/downloadJar")
    public JrcResult downloadJar(@RequestBody String body) {

        JSONObject json = JSON.parseObject(body);

        logger.info("downloadJar {}", body);

        MavenUtil.downloadJar(json);
        return JrcResult.success();
    }
}