package http.utils;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.util.HttpRepoURL;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;

public class Feed {
    private static Logger LOGGER = Logger.getLoggerFor(Feed.class);
    private final String url;

    public Feed(String url) {
        this.url = url;
    }

    public Document download() {
        OkHttpClient client = HttpRepoURL.getHttpClient();
        //TODO: Add Credential
        try {
            Request request = new Request.Builder()
                                .url(url)
                                .build();

            Response response = client.newCall(request).execute();
            if(!response.isSuccessful()){
                throw new RuntimeException(String.format("HTTP %s, %s",
                        response.code(), response.message()));
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().parse(response.body().byteStream());

        } catch (Exception ex) {
            String message = String.format("%s (%s) while getting package feed for : %s ", ex.getClass().getSimpleName(), ex.getMessage(), url);
            LOGGER.error(message);
            throw new RuntimeException(message, ex);
        }
    }

}


