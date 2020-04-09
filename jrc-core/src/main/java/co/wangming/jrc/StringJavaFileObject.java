package co.wangming.jrc;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * 用于将java源码保存在content属性中
 */
public class StringJavaFileObject extends SimpleJavaFileObject {

    /**
     * 保存java code
     */
    private String content;


    /**
     * 调用父类构造器，并设置content
     *
     * @param className
     * @param content
     */
    public StringJavaFileObject(String className, String content) {
        super(URI.create("string:///" + className.replace('.', '/')
                + Kind.SOURCE.extension), Kind.SOURCE);
        this.content = content;
    }

    /**
     * 实现getCharContent，使得JavaCompiler可以从content获取java源码
     *
     * @param ignoreEncodingErrors
     * @return
     */
    @Override
    public String getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }
}
