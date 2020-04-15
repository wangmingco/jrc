package co.wangming.jrc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

public class MavenUtilTest {

    @Test
    public void searchJar() {
        JrcResult result = MavenUtil.searchJar("netty");

        JSONObject jsonObject = JSON.parseObject(result.getData().toString());
        JSONObject response = jsonObject.getJSONObject("response");
        JSONArray doc = response.getJSONArray("docs");
        System.out.println(doc);
    }


    @Test
    public void downloadJar() {
        String content = "{\n" +
                "\"id\": \"com.jolira:guice\",\n" +
                "\"g\": \"com.jolira\",\n" +
                "\"a\": \"guice\",\n" +
                "\"latestVersion\": \"3.0.0\",\n" +
                "\"repositoryId\": \"central\",\n" +
                "\"p\": \"jar\",\n" +
                "\"timestamp\": 1301724755000,\n" +
                "\"versionCount\": 8,\n" +
                "\"text\": [\"com.jolira\", \"guice\", \"-sources.jar\", \"-javadoc.jar\", \".jar\", \".pom\"],\n" +
                "\"ec\": [\"-sources.jar\", \"-javadoc.jar\", \".jar\", \".pom\"]\n" +
                "}";

        JSONObject json = JSON.parseObject(content);

        MavenUtil.downloadJar(json);
    }
}
