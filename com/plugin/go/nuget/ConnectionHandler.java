package plugin.go.nuget;

import com.tw.go.plugin.util.HttpRepoURL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionHandler {

    public Map handleCheckRepositoryConnection(Map request) {
        HttpRepoURL repoConnection = createRepositoryConnection(request);
        try {
            repoConnection.checkConnection();
        } catch (Exception e) {
            Map responseMap = formatConnectionResponse("failure", "Unsuccessful Connection");
            return responseMap;
        }

        Map responseMap = formatConnectionResponse("success", "Successfully connected to repository url provided");
        return responseMap;
    }

    private Map formatConnectionResponse(String status, String message) {
        Map responseMap = new HashMap();
        responseMap.put("status", status);
        List messages = Arrays.asList(message);
        responseMap.put("messages", messages);
        return responseMap;
    }

    private HttpRepoURL createRepositoryConnection(Map request) {
        Map configMap = (Map) request.get("repository-configuration");

        Map urlMap = (Map) configMap.get("REPOSITORY_URL");
        Map usernameMap = (Map) configMap.get("USERNAME");
        Map passwordMap = (Map) configMap.get("PASSWORD");

        String url = (String) urlMap.get("value");
        // We use $metada because nuget uses the ODATA format.
        // This distinguishes nuget feeds from generic sites (though it would accept as valid a non-nuget feed ODATA site)
        url = metadataUrl(url);
        String username = (String) usernameMap.get("value");
        String password = (String) passwordMap.get("value");

        HttpRepoURL connection = new HttpRepoURL(url, username, password);
        return connection;
    }

    private String metadataUrl(String url) {
        if(!url.endsWith("/")) {
            url += "/";
        }
        return url + "$metadata";
    }
}
