package plugin.go.nuget;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PluginConfigHandler {

    public static Map createConfigurationField(String displayName, String displayOrder, boolean secure, boolean partOfIdentity, boolean required) {
        Map configMap = new HashMap();
        configMap.put("display-name", displayName);
        configMap.put("display-order", displayOrder);
        configMap.put("secure", secure);
        configMap.put("part-of-identity", partOfIdentity);
        configMap.put("required", required);

        return configMap;
    }

    public abstract Map handleConfiguration();

    public abstract List handleValidateConfiguration(Map request);
}
