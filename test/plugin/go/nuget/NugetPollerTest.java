package plugin.go.nuget;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NugetPollerTest {

    static int SUCCESS_STATUS_CODE = 200;
    NugetPoller nugetPoller;
    GoPluginApiRequest goApiPluginRequest;


    String expectedRepositoryConfiguration = "{\"PASSWORD\":{\"display-order\":\"2\",\"display-name\":\"Password\",\"part-of-identity\":false,\"secure\":true,\"required\":false}," +
            "\"USERNAME\":{\"display-order\":\"1\",\"display-name\":\"Username\",\"part-of-identity\":false,\"secure\":true,\"required\":false}," +
            "\"REPOSITORY_URL\":{\"display-order\":\"0\",\"display-name\":\"Repository Url\",\"part-of-identity\":true,\"secure\":false,\"required\":true}}";

    String expectedPackageConfiguration = "{\"PACKAGE_NAME\":{\"display-order\":\"1\",\"display-name\":\"Package Name\",\"part-of-identity\":true,\"secure\":false,\"required\":true}," +
            "\"PACKAGE_ID\":{\"display-order\":\"0\",\"display-name\":\"Package ID\",\"part-of-identity\":true,\"secure\":false,\"required\":true}}";

    @Before
    public void setUp() {
        nugetPoller = new NugetPoller();
        goApiPluginRequest = mock(GoPluginApiRequest.class);

    }

    @Test
    public void shouldReturnConfigurationsWhenHandlingRepositoryConfigurationRequest() {
        when(goApiPluginRequest.requestName()).thenReturn("repository-configuration");

        GoPluginApiResponse response = nugetPoller.handle(goApiPluginRequest);

        Assert.assertEquals(SUCCESS_STATUS_CODE, response.responseCode());
        Assert.assertEquals(expectedRepositoryConfiguration, response.responseBody().toString());
    }

    @Test
    public void shouldReturnConfigurationsWhenHandlingPackageConfigurationRequest() {
        when(goApiPluginRequest.requestName()).thenReturn("package-configuration");

        GoPluginApiResponse response = nugetPoller.handle(goApiPluginRequest);

        Assert.assertEquals(SUCCESS_STATUS_CODE, response.responseCode());
        Assert.assertEquals(expectedPackageConfiguration, response.responseBody().toString());
    }

    @Test
     public void shouldErrorWhenInvalidRepositoryConfiguration(){
        when(goApiPluginRequest.requestName()).thenReturn("validate-repository-configuration");
        String invalidBody = createUrlRequestBody("");
        when(goApiPluginRequest.requestBody()).thenReturn(invalidBody);

        GoPluginApiResponse response = nugetPoller.handle(goApiPluginRequest);
        List responseList = (List) new GsonBuilder().create().fromJson(response.responseBody(), Object.class);

        Assert.assertEquals(SUCCESS_STATUS_CODE, response.responseCode());
        Assert.assertFalse(responseList.isEmpty());
    }

    @Test
    public void shouldReturnEmptyErrorListWhenValidRepositoryConfigurations(){
        when(goApiPluginRequest.requestName()).thenReturn("validate-repository-configuration");
        String validBody = createUrlRequestBody("http://testsite.com");
        when(goApiPluginRequest.requestBody()).thenReturn(validBody);

        GoPluginApiResponse response = nugetPoller.handle(goApiPluginRequest);
        List responseList = (List) new GsonBuilder().create().fromJson(response.responseBody(), Object.class);

        Assert.assertEquals(SUCCESS_STATUS_CODE, response.responseCode());
        Assert.assertTrue(responseList.isEmpty());

    }

    private String createUrlRequestBody(String url) {
        Map urlMap=  new HashMap();
        urlMap.put("value", url);
        Map fieldsMap = new HashMap();
        fieldsMap.put("REPOSITORY_URL", urlMap);
        Map invalidBodyMap = new HashMap();
        invalidBodyMap.put("repository-configuration", fieldsMap);
        return new GsonBuilder().serializeNulls().create().toJson(invalidBodyMap);
    }
}
