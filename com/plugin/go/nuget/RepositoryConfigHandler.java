package plugin.go.nuget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositoryConfigHandler extends PluginConfigHandler {

    public Map handleRepositoryConfiguration() {
        Map repositoryConfig = new HashMap();

        repositoryConfig.put("REPOSITORY_URL", createConfigurationField("Repository Url", "0", false, true, true));
        repositoryConfig.put("USERNAME", createConfigurationField("Username", "1", false, false, false));
        repositoryConfig.put("PASSWORD", createConfigurationField("Password", "2", true, false, false));

        return repositoryConfig;
    }

    public List handleValidateRepositoryConfiguration(Map request) {
        List validationList = new ArrayList();

        Map configMap = (Map) request.get("repository-configuration");
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
