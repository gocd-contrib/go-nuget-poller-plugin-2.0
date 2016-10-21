package plugin.go.nuget.builders;

import java.util.HashMap;
import java.util.Map;

import static utils.Constants.PACKAGE_CONFIGURATION;
import static utils.Constants.REPOSITORY_CONFIGURATION;

public class RequestBuilder {

    private Map request;

    public RequestBuilder() {
        request = new HashMap();
    }

    public RequestBuilder withRespositoryConfiguration(String url, String username, String password) {
        Map urlMap = new HashMap();
        urlMap.put("value", url);
        Map fieldsMap = new HashMap();
        fieldsMap.put("REPOSITORY_URL", urlMap);
        Map usernameMap = new HashMap();
        usernameMap.put("value", username);
        fieldsMap.put("USERNAME", usernameMap);
        Map passwordMap = new HashMap();
        passwordMap.put("value", password);
        fieldsMap.put("PASSWORD", passwordMap);
        request.put(REPOSITORY_CONFIGURATION, fieldsMap);
        return this;
    }

    public RequestBuilder withPackageConfiguration(String packageID) {
        Map packageIDMap = new HashMap();
        packageIDMap.put("value", packageID);

        Map packageConfigurationMap = new HashMap();
        packageConfigurationMap.put("PACKAGE_ID", packageIDMap);

        request.put(PACKAGE_CONFIGURATION, packageConfigurationMap);
        return this;
    }

    public RequestBuilder withPreviousRevision(String version) {
        Map dataMap = new HashMap();
        dataMap.put("VERSION", version);
        Map revisionInfoMap = new HashMap();
        revisionInfoMap.put("data", dataMap);
        revisionInfoMap.put("timestamp", "2011-07-14T19:43:37.100Z");
        revisionInfoMap.put("revision", "abc-10.2.1.rpm");
        request.put("previous-revision", revisionInfoMap);
        return this;
    }

    public Map build() {
        return request;
    }

}
