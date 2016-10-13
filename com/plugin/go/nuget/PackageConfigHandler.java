package plugin.go.nuget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageConfigHandler extends PluginConfigHandler {

    public Map handlePackageConfiguration() {
        Map packageConfig = new HashMap();

        packageConfig.put("PACKAGE_ID", createConfigurationField("Package ID", "0", false, true, true));

        return packageConfig;
    }

    public List handleValidatePackageConfiguration(Map request) {
        List validationList = new ArrayList();
        Map configMap = (Map) request.get("package-configuration");
        Map packageIDMap = (Map) configMap.get("PACKAGE_ID");

        if(packageIDMap.get("value").equals("")) {
            Map idErrors = new HashMap();
            idErrors.put("key", "PACKAGE_ID");
            idErrors.put("message", "Package ID cannot be empty");
            validationList.add(idErrors);
        }

        return validationList;
    }
}
