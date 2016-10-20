package plugin.go.nuget.e2e;


import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plugin.go.nuget.NugetController;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NugetPluginTest {
    private static final int SUCCESS_STATUS_CODE = 200;
    private static final String REQUEST_REPOSITORY_CONFIGURATION = "repository-configuration";
    private static final String REQUEST_PACKAGE_CONFIGURATION = "package-configuration";

    NugetController nugetController;
    GoPluginApiRequest goApiPluginRequest;


    @Before
    public void setUp() {
        nugetController = new NugetController();
        goApiPluginRequest = mock(GoPluginApiRequest.class);

    }

    @Test
    public void shouldReturnConfigurationsWhenHandlingRepositoryConfigurationRequest() {
        String expectedRepositoryConfiguration = "{\"PASSWORD\":{\"display-order\":\"2\",\"display-name\":\"Password\",\"part-of-identity\":false,\"secure\":true,\"required\":false}," +
                "\"USERNAME\":{\"display-order\":\"1\",\"display-name\":\"Username\",\"part-of-identity\":false,\"secure\":false,\"required\":false}," +
                "\"REPOSITORY_URL\":{\"display-order\":\"0\",\"display-name\":\"Repository Url\",\"part-of-identity\":true,\"secure\":false,\"required\":true}}";

        Map expectedRepositoryConfigurationMap = (Map) new GsonBuilder().create().fromJson(expectedRepositoryConfiguration, Object.class);
        when(goApiPluginRequest.requestName()).thenReturn(REQUEST_REPOSITORY_CONFIGURATION);

        GoPluginApiResponse response = nugetController.handle(goApiPluginRequest);
        Map responseBodyMap = (Map) new GsonBuilder().create().fromJson(response.responseBody(), Object.class);

        Assert.assertEquals(SUCCESS_STATUS_CODE, response.responseCode());
        Assert.assertEquals(expectedRepositoryConfigurationMap, responseBodyMap);
    }

    @Test
    public void shouldReturnConfigurationsWhenHandlingPackageConfigurationRequest() {
        String expectedPackageConfiguration = "{\"PACKAGE_ID\":" +
                "{\"display-order\":\"0\",\"display-name\":\"Package ID\",\"part-of-identity\":true,\"secure\":false,\"required\":true}}";
        Map expectedPackageConfigurationMap = (Map) new GsonBuilder().create().fromJson(expectedPackageConfiguration, Object.class);

        when(goApiPluginRequest.requestName()).thenReturn(REQUEST_PACKAGE_CONFIGURATION);

        GoPluginApiResponse response = nugetController.handle(goApiPluginRequest);
        Map responseBodyMap = (Map) new GsonBuilder().create().fromJson(response.responseBody(), Object.class);

        Assert.assertEquals(SUCCESS_STATUS_CODE, response.responseCode());
        Assert.assertEquals(expectedPackageConfigurationMap, responseBodyMap);
    }

}
