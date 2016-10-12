/**
 * Created by alisonpolton-simon on 10/7/16.
 */
package plugin.go.nuget;

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
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

    public static final int SUCCESS_RESPONSE_CODE = 200;

    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.accessor = goApplicationAccessor;
    }

    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        logger.info("Request name " + goPluginApiRequest.requestName());
        logger.info("Request body " + goPluginApiRequest.requestBody());
        logger.info("Request body " + goPluginApiRequest.requestParameters());
        logger.info("Request body " + goPluginApiRequest.requestHeaders());

        if (goPluginApiRequest.requestName().equals(REQUEST_REPOSITORY_CONFIGURATION)) {
            return RepositoryConfigs.handleRepositoryConfiguration();
        } else if (goPluginApiRequest.requestName().equals(REQUEST_PACKAGE_CONFIGURATION)) {
            return PackageConfigs.handlePackageConfiguration();
        } else if (goPluginApiRequest.requestName().equals(VALIDATE_REPOSITORY_CONFIGURATION)) {
            return RepositoryConfigs.handleValidateRepositoryConfiguration(goPluginApiRequest);
        } else if (goPluginApiRequest.requestName().equals(VALIDATE_PACKAGE_CONFIGURATION)) {
            return PackageConfigs.handleValidatePackageConfiguration(goPluginApiRequest);
        } else if (goPluginApiRequest.requestName().equals(CHECK_REPOSITORY_CONNECTION)) {
            return ConnectionChecker.handleCheckRepositoryConnection(goPluginApiRequest);
        }
        return null;
    }

    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("package-repository", Arrays.asList("1.0"));
    }






}
