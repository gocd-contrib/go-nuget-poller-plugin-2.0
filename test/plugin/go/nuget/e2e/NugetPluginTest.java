package plugin.go.nuget.e2e;


import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plugin.go.nuget.NugetController;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NugetPluginTest {
    private static final int SUCCESS_STATUS_CODE = 200;
    private static final String REQUEST_REPOSITORY_CONFIGURATION = "repository-configuration";
    private static final String REQUEST_PACKAGE_CONFIGURATION = "package-configuration";

    NugetController nugetController;
    GoPluginApiRequest goApiPluginRequest;

    String expectedRepositoryConfiguration = "{\"PASSWORD\":{\"display-order\":\"2\",\"display-name\":\"Password\",\"part-of-identity\":false,\"secure\":true,\"required\":false}," +
            "\"USERNAME\":{\"display-order\":\"1\",\"display-name\":\"Username\",\"part-of-identity\":false,\"secure\":true,\"required\":false}," +
            "\"REPOSITORY_URL\":{\"display-order\":\"0\",\"display-name\":\"Repository Url\",\"part-of-identity\":true,\"secure\":false,\"required\":true}}";

    String expectedPackageConfiguration = "{\"PACKAGE_NAME\":{\"display-order\":\"1\",\"display-name\":\"Package Name\",\"part-of-identity\":true,\"secure\":false,\"required\":true}," +
            "\"PACKAGE_ID\":{\"display-order\":\"0\",\"display-name\":\"Package ID\",\"part-of-identity\":true,\"secure\":false,\"required\":true}}";

    @Before
    public void setUp() {
        nugetController = new NugetController();
        goApiPluginRequest = mock(GoPluginApiRequest.class);

    }

    @Test
    public void shouldReturnConfigurationsWhenHandlingRepositoryConfigurationRequest() {
        when(goApiPluginRequest.requestName()).thenReturn(REQUEST_REPOSITORY_CONFIGURATION);

        GoPluginApiResponse response = nugetController.handle(goApiPluginRequest);

        Assert.assertEquals(SUCCESS_STATUS_CODE, response.responseCode());
        Assert.assertEquals(expectedRepositoryConfiguration, response.responseBody().toString());
    }

    @Test
    public void shouldReturnConfigurationsWhenHandlingPackageConfigurationRequest() {
        when(goApiPluginRequest.requestName()).thenReturn(REQUEST_PACKAGE_CONFIGURATION);

        GoPluginApiResponse response = nugetController.handle(goApiPluginRequest);

        Assert.assertEquals(SUCCESS_STATUS_CODE, response.responseCode());
        Assert.assertEquals(expectedPackageConfiguration, response.responseBody().toString());
    }
}
