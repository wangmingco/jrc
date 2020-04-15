package co.wangming.jrc;

import co.wangming.jrc.classloader.ClassLoaderUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MavenUtil {

    public static JrcResult searchJar(String searchJarKeyword) {

        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpGet httpget = new HttpGet("https://search.maven.org/solrsearch/select?q=" + searchJarKeyword + "&rows=50&wt=json");


            // Create a custom response handler
            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };

            final String responseBody = httpclient.execute(httpget, responseHandler);
            return JrcResult.success(responseBody);
        } catch (IOException e) {
            return JrcResult.error(e.getMessage());
        }
    }

    /**
     * {
     * "id": "com.jolira:guice",
     * "g": "com.jolira",
     * "a": "guice",
     * "latestVersion": "3.0.0",
     * "repositoryId": "central",
     * "p": "jar",
     * "timestamp": 1301724755000,
     * "versionCount": 8,
     * "text": ["com.jolira", "guice", "-sources.jar", "-javadoc.jar", ".jar", ".pom"],
     * "ec": ["-sources.jar", "-javadoc.jar", ".jar", ".pom"]
     * }
     *
     * @return
     */
    public static JrcResult downloadJar(JSONObject json) {

        String GroupId = json.getString("g");
        String ArtifactId = json.getString("a");
        String LatestVersion = json.getString("latestVersion");

        String path = GroupId.replaceAll("\\.", "/") + "/";
        path += ArtifactId.replaceAll("\\.", "/") + "/";
        path += LatestVersion + "/";
        path += ArtifactId + "-" + LatestVersion + ".jar";

        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpGet httpget = new HttpGet("https://search.maven.org/remotecontent?filepath=" + path);

            String fileName = GroupId + "." + ArtifactId + "." + LatestVersion + ".jar";
            httpclient.execute(httpget, new FileDownloadResponseHandler(fileName));

            ClassLoaderUtil.setClassLoader(null);

            return JrcResult.success();
        } catch (IOException e) {
            return JrcResult.error(e.getMessage());
        }
    }

    private static class FileDownloadResponseHandler implements ResponseHandler<File> {

        private final File target;

        public FileDownloadResponseHandler(String fileName) {

            File lib = new File("./lib");
            if (!lib.exists()) {
                lib.mkdirs();
            }

            this.target = new File("./lib/" + fileName);
        }

        @Override
        public File handleResponse(HttpResponse response) throws IOException {
            InputStream source = response.getEntity().getContent();
            FileUtils.copyInputStreamToFile(source, this.target);
            return this.target;
        }

    }
}
