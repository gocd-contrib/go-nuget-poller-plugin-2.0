package plugin.go.nuget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageConfigHandler extends PluginConfigHandler {

    private ConnectionHandler connectionHandler;

    public PackageConfigHandler(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public Map handleConfiguration() {
        Map packageConfig = new HashMap();

        packageConfig.put("PACKAGE_ID", createConfigurationField("Package ID", "0", false, true, true));
        packageConfig.put("POLL_VERSION_FROM", createConfigurationField("Version to poll >=", "1", false, false, false));
        packageConfig.put("POLL_VERSION_TO", createConfigurationField("Version to poll <", "2", false, false, false));
        packageConfig.put("INCLUDE_PRE_RELEASE", createConfigurationField("Include Prerelease? (yes/no, defaults to yes)", "3", false, false, false));

        return packageConfig;
    }

    public List handleValidateConfiguration(Map request) {
        List validationList = new ArrayList();
        Map configMap = (Map) request.get("package-configuration");
        Map packageIDMap = (Map) configMap.get("PACKAGE_ID");

        if (packageIDMap.get("value").equals("")) {
            Map idErrors = new HashMap();
            idErrors.put("key", "PACKAGE_ID");
            idErrors.put("message", "Package ID cannot be empty");
            validationList.add(idErrors);
        }

        return validationList;
    }
}
