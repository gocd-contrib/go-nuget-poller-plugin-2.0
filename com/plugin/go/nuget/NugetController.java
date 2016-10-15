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

import java.util.Arrays;
import java.util.Map;

import static utils.Constants.*;

@Extension
public class NugetController implements GoPlugin {
    private GoApplicationAccessor accessor;
    private static Logger logger = Logger.getLoggerFor(NugetController.class);
    private ConnectionHandler connectionHandler = new ConnectionHandler();
    private PackageConfigHandler packageConfigHandler = new PackageConfigHandler(connectionHandler);
    private RepositoryConfigHandler repositoryConfigHandler = new RepositoryConfigHandler(connectionHandler);


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
        Map requestBodyMap = (Map) new GsonBuilder().create().fromJson(goPluginApiRequest.requestBody(), Object.class);

        if (requestName.equals(REPOSITORY_CONFIGURATION)) {
            result = repositoryConfigHandler.handleRepositoryConfiguration();
        } else if (requestName.equals(VALIDATE_REPOSITORY_CONFIGURATION)) {
            result = repositoryConfigHandler.handleValidateRepositoryConfiguration(requestBodyMap);
        } else if (requestName.equals(CHECK_REPOSITORY_CONNECTION)) {
            result = repositoryConfigHandler.handleCheckRepositoryConnection(requestBodyMap);
        } else if (requestName.equals(PACKAGE_CONFIGURATION)) {
            result = packageConfigHandler.handlePackageConfiguration();
        } else if (requestName.equals(VALIDATE_PACKAGE_CONFIGURATION)) {
            result = packageConfigHandler.handleValidatePackageConfiguration(requestBodyMap);
        } else if(requestName.equals(LATEST_REVISION)){
            result = packageConfigHandler.handleLatestRevision(requestBodyMap);
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
