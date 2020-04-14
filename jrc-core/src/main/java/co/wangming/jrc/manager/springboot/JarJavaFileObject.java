package co.wangming.jrc.manager.springboot;

import co.wangming.jrc.JrcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import java.io.*;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLConnection;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JarJavaFileObject implements JavaFileObject {

    private static final Logger logger = LoggerFactory.getLogger(JarJavaFileObject.class);

    private final Kind kind;
    private final SpringBootArchiveEntry springBootArchiveEntry;
    private URI uri;
    private String className;

    public JarJavaFileObject(SpringBootArchiveEntry springBootArchiveEntry, Kind kind) {
        className = JrcUtil.getClassNameFromPath(springBootArchiveEntry.archiveEntry.getName());

        this.kind = kind;
        this.springBootArchiveEntry = springBootArchiveEntry;
        try {
            uri = URI.create(springBootArchiveEntry.archive.getUrl().toString() + springBootArchiveEntry.archiveEntry.getName());
        } catch (MalformedURLException e) {
            logger.error("", e);
        }
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public boolean isNameCompatible(String simpleName, Kind kind) {
        logger.info("isNameCompatible");

        String baseName = simpleName + kind.extension;
        return kind.equals(getKind())
                && (baseName.equals(toUri().getPath())
                || toUri().getPath().endsWith("/" + baseName));
    }

    @Override
    public NestingKind getNestingKind() {
        logger.info("getNestingKind");

        return null;
    }

    @Override
    public Modifier getAccessLevel() {
        logger.info("getAccessLevel");

        return null;
    }

    @Override
    public URI toUri() {
        logger.info("toUri");

        return uri;
    }

    @Override
    public String getName() {
        return className;
    }

    @Override
    public InputStream openInputStream() throws IOException {

        try {
            String classEntryName = springBootArchiveEntry.archiveEntry.getName();

            URLConnection connection = springBootArchiveEntry.archive.getUrl().openConnection();

            JarFile jarFile = ((JarURLConnection) connection).getJarFile();
            ZipEntry entry = jarFile.getEntry(classEntryName);
            InputStream in = jarFile.getInputStream(entry);

            return in;
        } catch (IOException ex) {
            // Ignore
            return null;
        }

    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        logger.info("openOutputStream");

        return null;
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        logger.info("openReader");

        try {
            String classEntryName = springBootArchiveEntry.archiveEntry.getName();
            URLConnection connection = springBootArchiveEntry.archive.getUrl().openConnection();

            JarFile jarFile = ((JarURLConnection) connection).getJarFile();
            ZipEntry entry = jarFile.getEntry(classEntryName);
            return new InputStreamReader(jarFile.getInputStream(entry));
        } catch (IOException ex) {
            // Ignore
            return null;
        }
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        logger.info("getCharContent");

        return null;
    }

    @Override
    public Writer openWriter() throws IOException {
        logger.info("openWriter");

        return null;
    }

    @Override
    public long getLastModified() {
        logger.info("getLastModified");

        return 0;
    }

    @Override
    public boolean delete() {
        logger.info("delete");

        return false;
    }
}
