package plugin.go.nuget;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.util.HttpRepoURL;
import http.utils.Feed;
import org.w3c.dom.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionHandler {
    private static Logger logger = Logger.getLoggerFor(ConnectionHandler.class);

    public Map checkConnectionToUrlWithMetadata(String url, String username, String password) {
        HttpRepoURL repoConnection =  new HttpRepoURL(metadataUrl(url), username, password);
        try {
            repoConnection.checkConnection();
        } catch (Exception e) {
            Map responseMap = formatConnectionResponse("failure", "Unsuccessful Connection");
            return responseMap;
        }

        Map responseMap = formatConnectionResponse("success", "Successfully connected to repository url provided");
        return responseMap;
    }

    public NuGetFeedDocument getNuGetFeedDocument(String baseUrl, String queryParams, String username, String password) {
        Map repoConnectionResponseMap = checkConnectionToUrlWithMetadata(baseUrl, username, password);
        if(!repoConnectionSuccessful(repoConnectionResponseMap)){
            return null;
        }
        Document xmlDocument = new Feed(baseUrl + queryParams).download();
        return new NuGetFeedDocument(xmlDocument);
    }

    private Map formatConnectionResponse(String status, String message) {
        Map responseMap = new HashMap();
        responseMap.put("status", status);
        List messages = Arrays.asList(message);
        responseMap.put("messages", messages);
        return responseMap;
    }

    private boolean repoConnectionSuccessful(Map repoConnectionResponseMap) {
        if (repoConnectionResponseMap.get("status") == "failure") {
            return false;
        } else return true;
    }

    // We use $metada because nuget uses the ODATA format.
    // This distinguishes nuget feeds from generic sites (though it would accept as valid a non-nuget feed ODATA site)
    private String metadataUrl(String url) {
        if(!url.endsWith("/")) {
            url += "/";
        }
        return url + "$metadata";
    }
}
