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

import java.util.*;

@Extension
public class NugetController implements GoPlugin {
    private GoApplicationAccessor accessor;
    private static Logger logger = Logger.getLoggerFor(NugetController.class);
    private PackageConfigs packageConfigs = new PackageConfigs();
    private RepositoryConfigs repositoryConfigs = new RepositoryConfigs();
    private ConnectionChecker connectionChecker = new ConnectionChecker();

    private static final String REQUEST_REPOSITORY_CONFIGURATION = "repository-configuration";
    private static final String VALIDATE_REPOSITORY_CONFIGURATION = "validate-repository-configuration";
    private static final String CHECK_REPOSITORY_CONNECTION = "check-repository-connection";
    private static final String REQUEST_PACKAGE_CONFIGURATION = "package-configuration";
    private static final String VALIDATE_PACKAGE_CONFIGURATION = "validate-package-configuration";

    public static final int SUCCESS_RESPONSE_CODE = 200;

    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.accessor = goApplicationAccessor;
    }

    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {

        logger.info("Request name " + goPluginApiRequest.requestName());
        logger.info("Request body " + goPluginApiRequest.requestBody());
        logger.info("Request body " + goPluginApiRequest.requestParameters());
        logger.info("Request body " + goPluginApiRequest.requestHeaders());

        Object result = null;
        String requestName = goPluginApiRequest.requestName();

        if (requestName.equals(REQUEST_REPOSITORY_CONFIGURATION)) {
            result = repositoryConfigs.handleRepositoryConfiguration();
        } else if (requestName.equals(VALIDATE_REPOSITORY_CONFIGURATION)) {
            result = repositoryConfigs.handleValidateRepositoryConfiguration(goPluginApiRequest);
        } else if (requestName.equals(CHECK_REPOSITORY_CONNECTION)) {
            result = connectionChecker.handleCheckRepositoryConnection(goPluginApiRequest);
        } else if (requestName.equals(REQUEST_PACKAGE_CONFIGURATION)) {
            result = packageConfigs.handlePackageConfiguration();
        } else if (requestName.equals(VALIDATE_PACKAGE_CONFIGURATION)) {
            result = packageConfigs.handleValidatePackageConfiguration(goPluginApiRequest);
        }

        if (result != null) {
            return createResponse(SUCCESS_RESPONSE_CODE, result);
        }

        return null;
    }

    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("package-repository", Arrays.asList("1.0"));
    }

    private static GoPluginApiResponse createResponse(int responseCode, Object body) {
        final DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(responseCode);
        response.setResponseBody(new GsonBuilder().serializeNulls().create().toJson(body));
        return response;
    }

}
