package co.wangming.jrc;

public class JrcUtil {

    public static String getClassNameFromPath(String name) {
        if (name.endsWith(".class")) {
            name = name.substring(0, name.length() - 6);
        }
        name = name.replace("/", ".");

        return name;
    }
}
