/**
 * Created by alisonpolton-simon on 10/7/16.
 */
package plugin.go.nuget;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.tw.go.plugin.util.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@Extension
public class NugetPoller implements GoPlugin {
    private GoApplicationAccessor accessor;
    private static Logger logger = Logger.getLoggerFor(NugetPoller.class);
    private static final String REQUEST_REPOSITORY_CONFIGURATION = "repository-configuration";
    private static final String REQUEST_PACKAGE_CONFIGURATION = "package-configuration";
    private static final String VALIDATE_REPOSITORY_CONFIGURATION = "validate-repository-configuration";
    private static final String VALIDATE_PACKAGE_CONFIGURATION = "validate-package-configuration";
    private static final String CHECK_REPOSITORY_CONNECTION = "check-repository-connection";

    private static final int SUCCESS_RESPONSE_CODE = 200;

    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.accessor = goApplicationAccessor;
    }

    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        logger.info("SALLIE IS GOING CRAZY");
        logger.info("Request name " + goPluginApiRequest.requestName());
        logger.info("Request body " + goPluginApiRequest.requestBody());
        logger.info("Request body " + goPluginApiRequest.requestParameters());
        logger.info("Request body " + goPluginApiRequest.requestHeaders());

        if (goPluginApiRequest.requestName().equals(REQUEST_REPOSITORY_CONFIGURATION)) {
            return handleRepositoryConfiguration();
        } else if (goPluginApiRequest.requestName().equals(REQUEST_PACKAGE_CONFIGURATION)) {
            return handlePackageConfiguration();
        } else if (goPluginApiRequest.requestName().equals(VALIDATE_REPOSITORY_CONFIGURATION)) {
            return handleValidateRepositoryConfiguration(goPluginApiRequest);
        } else if (goPluginApiRequest.requestName().equals(VALIDATE_PACKAGE_CONFIGURATION)) {
            return handleValidatePackageConfiguration(goPluginApiRequest);
        } else if (goPluginApiRequest.requestName().equals(CHECK_REPOSITORY_CONNECTION)) {
            return handleCheckRepositoryConnection(goPluginApiRequest);
        }
        return null;
    }

    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("package-repository", Arrays.asList("1.0"));
    }

    private GoPluginApiResponse handleRepositoryConfiguration() {
        Map repositoryConfig = new HashMap();

        repositoryConfig.put("REPOSITORY_URL", createConfigurationField("Repository Url", "0", false, true, true));
        repositoryConfig.put("USERNAME", createConfigurationField("Username", "1", true, false, false));
        repositoryConfig.put("PASSWORD", createConfigurationField("Password", "2", true, false, false));

        return createResponse(SUCCESS_RESPONSE_CODE, repositoryConfig);
    }

    private GoPluginApiResponse handlePackageConfiguration() {
        Map packageConfig = new HashMap();

        packageConfig.put("PACKAGE_ID", createConfigurationField("Package ID", "0", false, true, true));
        packageConfig.put("PACKAGE_NAME", createConfigurationField("Package Name", "1", false, true, true));

        return createResponse(SUCCESS_RESPONSE_CODE, packageConfig);
    }

    private GoPluginApiResponse handleValidateRepositoryConfiguration(GoPluginApiRequest request) {
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

        return createResponse(SUCCESS_RESPONSE_CODE, validationList);
    }

    private GoPluginApiResponse handleValidatePackageConfiguration(GoPluginApiRequest goPluginApiRequest) {
        return null;
    }

    private GoPluginApiResponse handleCheckRepositoryConnection(GoPluginApiRequest request) {
        HttpRepoURL repoConnection = createRepositoryConnection(request);
        try {
            repoConnection.checkConnection();
        } catch (Exception e) {
            Map responseMap = formatConnectionResponse("failure", "Unsuccessful Connection");
            return createResponse(200, responseMap);
        }

        Map responseMap = formatConnectionResponse("success","Successfully connected to repository url provided");
        return createResponse(200, responseMap);
    }

    private Map formatConnectionResponse(String status, String message) {
        Map responseMap = new HashMap();
        responseMap.put("status",status);
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

    private GoPluginApiResponse createResponse(int responseCode, Object body) {
        final DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(responseCode);
        response.setResponseBody(new GsonBuilder().serializeNulls().create().toJson(body));
        return response;
    }
    private Map createConfigurationField(String displayName, String displayOrder, boolean secure, boolean partOfIdentity, boolean required) {
        Map configMap = new HashMap();
        configMap.put("display-name", displayName);
        configMap.put("display-order", displayOrder);
        configMap.put("secure", secure);
        configMap.put("part-of-identity", partOfIdentity);
        configMap.put("required", required);

        return configMap;
    }

}