package co.wangming.jrc.manager.springboot;

import co.wangming.jrc.classloader.ClassLoaderUtil;
import co.wangming.jrc.classloader.JrcLaunchedURLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.loader.Launcher;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.ExplodedArchive;
import org.springframework.boot.loader.archive.JarFileArchive;

import java.io.File;
import java.net.URI;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.jar.Manifest;

public class SpringBootLauncher extends Launcher {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootLauncher.class);

    Map<String, SpringBootArchiveEntry> entryCache = new HashMap<>();

    static final String BOOT_INF_CLASSES = "BOOT-INF/classes/";
    static final String BOOT_INF_LIB = "BOOT-INF/lib/";

    private final Archive archive;

    public SpringBootLauncher() {
        try {
            this.archive = createJrcArchive();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Archive createJrcArchive() throws Exception {
        Class<?> launcherClass = Class.forName("org.springframework.boot.loader.Launcher");
        ProtectionDomain protectionDomain = launcherClass.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
        String path = (location != null) ? location.getSchemeSpecificPart() : null;
        if (path == null) {
            throw new IllegalStateException("Unable to determine code source archive");
        }
        File root = new File(path);
        if (!root.exists()) {
            throw new IllegalStateException("Unable to determine code source archive from " + root);
        }
        return (root.isDirectory() ? new ExplodedArchive(root) : new JarFileArchive(root));
    }

    @Override
    protected String getMainClass() throws Exception {
        Manifest manifest = this.archive.getManifest();
        String mainClass = null;
        if (manifest != null) {
            mainClass = manifest.getMainAttributes().getValue("Start-Class");
        }
        if (mainClass == null) {
            throw new IllegalStateException("No 'Start-Class' manifest archiveEntry specified in " + this);
        }
        return mainClass;
    }

    @Override
    protected List<Archive> getClassPathArchives() throws Exception {
        List<Archive> archives = new ArrayList<>(this.archive.getNestedArchives(this::isNestedArchive));
        postProcessClassPathArchives(archives);

        logger.info("entryCache.size : {}", entryCache.size());
        return archives;
    }

    protected void postProcessClassPathArchives(List<Archive> archives) throws Exception {
        for (Archive archive : archives) {

            Iterator<Archive.Entry> ite = archive.iterator();
            while (ite.hasNext()) {
                Archive.Entry archiveEntry = ite.next();

                SpringBootArchiveEntry entryItem = new SpringBootArchiveEntry();
                entryItem.archiveEntry = archiveEntry;
                entryItem.archive = archive;
                entryCache.put(archiveEntry.getName(), entryItem);
            }

            postProcessClassPathArchives(archive.getNestedArchives(this::isNestedArchive));
        }
    }

    public List<SpringBootArchiveEntry> getEntries(String path) {
        List<SpringBootArchiveEntry> list = new ArrayList<>();
        for (Map.Entry<String, SpringBootArchiveEntry> stringEntryItemEntry : entryCache.entrySet()) {
            if (stringEntryItemEntry.getKey().startsWith(path)) {
                list.add(stringEntryItemEntry.getValue());
            }
        }
        return list;
    }

    protected boolean isNestedArchive(Archive.Entry entry) {
        if (entry.isDirectory()) {
            return entry.getName().equals(BOOT_INF_CLASSES);
        }
        return entry.getName().startsWith(BOOT_INF_LIB);
    }

    @Override
    protected void launch(String[] args, String mainClass, ClassLoader classLoader) throws Exception {
        Thread.currentThread().setContextClassLoader(classLoader);
        ClassLoaderUtil.setClassLoader(classLoader);
    }

    public void launch() throws Exception {
        super.launch(new String[]{});
    }


}
