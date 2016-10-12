package plugin.go.nuget;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositoryConfigs extends PluginConfigs{

    public Map handleRepositoryConfiguration() {
        Map repositoryConfig = new HashMap();

        repositoryConfig.put("REPOSITORY_URL", createConfigurationField("Repository Url", "0", false, true, true));
        repositoryConfig.put("USERNAME", createConfigurationField("Username", "1", true, false, false));
        repositoryConfig.put("PASSWORD", createConfigurationField("Password", "2", true, false, false));

        return repositoryConfig;
    }

    public List handleValidateRepositoryConfiguration(GoPluginApiRequest request) {
        List validationList = new ArrayList();
        Map requestMap = (Map) new GsonBuilder().create().fromJson(request.requestBody(), Object.class);
        Map configMap = (Map) requestMap.get("repository-configuration");
        Map urlMap = (Map) configMap.get("REPOSITORY_URL");

        if (urlMap.get("value").equals("")) {
            Map errors = new HashMap();
            errors.put("key", "REPOSITORY_URL");
            errors.put("message", "Url cannot be empty");
            validationList.add(errors);
        }

        return validationList;
    }


}
