package co.wangming.jrc.manager.springboot;

import org.apache.commons.io.IOUtils;

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

    private final Kind kind;
    private final SpringBootArchiveEntry springBootArchiveEntry;
    private URI uri;

    public JarJavaFileObject(SpringBootArchiveEntry springBootArchiveEntry, Kind kind) {
        this.kind = kind;
        this.springBootArchiveEntry = springBootArchiveEntry;
        try {
            uri = URI.create(springBootArchiveEntry.archive.getUrl().toString() + springBootArchiveEntry.archiveEntry.getName());
        } catch (MalformedURLException e) {
        }
    }


    public byte[] getBytes() {
        try {
            InputStream in = openInputStream();
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public boolean isNameCompatible(String simpleName, Kind kind) {
        String baseName = simpleName + kind.extension;
        return kind.equals(getKind())
                && (baseName.equals(toUri().getPath())
                || toUri().getPath().endsWith("/" + baseName));
    }

    @Override
    public NestingKind getNestingKind() {
        return null;
    }

    @Override
    public Modifier getAccessLevel() {
        return null;
    }

    @Override
    public URI toUri() {
        return uri;
    }

    @Override
    public String getName() {
        return springBootArchiveEntry.archiveEntry.getName();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        try {
            String classEntryName = springBootArchiveEntry.archiveEntry.getName();
            URLConnection connection = springBootArchiveEntry.archive.getUrl().openConnection();

            JarFile jarFile = ((JarURLConnection) connection).getJarFile();
            ZipEntry entry = jarFile.getEntry(classEntryName);
            return jarFile.getInputStream(entry);
        } catch (IOException ex) {
            // Ignore
            return null;
        }

    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return null;
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
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
        return null;
    }

    @Override
    public Writer openWriter() throws IOException {
        return null;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public boolean delete() {
        return false;
    }
}
