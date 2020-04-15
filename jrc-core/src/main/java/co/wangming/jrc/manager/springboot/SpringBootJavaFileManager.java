package co.wangming.jrc.manager.springboot;

import co.wangming.jrc.classloader.ClassLoaderUtil;
import co.wangming.jrc.manager.JrcJavaFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SpringBootJavaFileManager extends JrcJavaFileManager {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootJavaFileManager.class);

    SpringBootLauncher springBootLauncher;

    public SpringBootJavaFileManager(StandardJavaFileManager standardManager) {
        super(standardManager);

        try {
            springBootLauncher = new SpringBootLauncher();
            springBootLauncher.launch();
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return ClassLoaderUtil.getClassLoader().getClassLoader();
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set set, boolean recurse) throws IOException {

        String packagePath = packageName.replaceAll("\\.", "/");
        List<SpringBootArchiveEntry> entries = springBootLauncher.getEntries(packagePath);

        List<JavaFileObject> list = entries.stream().map(it -> new JarJavaFileObject(it, JavaFileObject.Kind.CLASS)).collect(Collectors.toList());

        Iterable<JavaFileObject> superList = super.list(location, packageName, set, recurse);
        if (superList == null) {
            return list;
        }

        for (JavaFileObject o : superList) {
            list.add(o);
        }

        return list;
    }

    /**
     * 将 JavaFileObject 转换成className
     *
     * @param location PLATFORM_CLASS_PATH
     * @param file     /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/lib/ct.sym(META-INF/sym/rt.jar/java/lang/Comparable.class)
     * @return java.lang.Comparable
     */
    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof JarJavaFileObject) {
            return file.getName();
        } else {
            return super.inferBinaryName(location, file);
        }
    }

}
