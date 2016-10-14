package plugin.go.nuget;

import com.tw.go.plugin.util.HttpRepoURL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionHandler {

    public Map checkConnectionToUrl(String url, String username, String password) {
        HttpRepoURL repoConnection =  new HttpRepoURL(url, username, password);
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
}
