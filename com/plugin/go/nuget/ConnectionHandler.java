package plugin.go.nuget;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.tw.go.plugin.util.HttpRepoURL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionHandler {

    public Map handleCheckRepositoryConnection(GoPluginApiRequest request) {
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

    private HttpRepoURL createRepositoryConnection(GoPluginApiRequest request) {
        Map requestMap = (Map) new GsonBuilder().create().fromJson(request.requestBody(), Object.class);
        Map configMap = (Map) requestMap.get("repository-configuration");

        Map urlMap = (Map) configMap.get("REPOSITORY_URL");
        Map usernameMap = (Map) configMap.get("USERNAME");
        Map passwordMap = (Map) configMap.get("PASSWORD");

        String url = (String) urlMap.get("value");
        String username = (String) usernameMap.get("value");
        String password = (String) passwordMap.get("value");

        HttpRepoURL connection = new HttpRepoURL(url, username, password);
        return connection;
    }
}
