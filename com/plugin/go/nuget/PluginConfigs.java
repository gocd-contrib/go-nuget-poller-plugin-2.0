package plugin.go.nuget;


import java.util.HashMap;
import java.util.Map;

public class PluginConfigs {

    public static Map createConfigurationField(String displayName, String displayOrder, boolean secure, boolean partOfIdentity, boolean required) {
        Map configMap = new HashMap();
        configMap.put("display-name", displayName);
        configMap.put("display-order", displayOrder);
        configMap.put("secure", secure);
        configMap.put("part-of-identity", partOfIdentity);
        configMap.put("required", required);

        return configMap;
    }
}
