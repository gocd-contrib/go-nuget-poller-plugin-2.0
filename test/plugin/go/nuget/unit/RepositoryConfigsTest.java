package plugin.go.nuget.unit;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plugin.go.nuget.RepositoryConfigs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepositoryConfigsTest {
    RepositoryConfigs repositoryConfigs;
    GoPluginApiRequest goApiPluginRequest;

    @Before
    public void setUp() {
        repositoryConfigs = new RepositoryConfigs();
        goApiPluginRequest = mock(GoPluginApiRequest.class);

    }

    @Test
    public void shouldErrorWhenInvalidRepositoryConfiguration(){
        String invalidBody = createUrlRequestBody("");
        when(goApiPluginRequest.requestBody()).thenReturn(invalidBody);

        List responseList = repositoryConfigs.handleValidateRepositoryConfiguration(goApiPluginRequest);

        Assert.assertFalse(responseList.isEmpty());
    }

    @Test
    public void shouldReturnEmptyErrorListWhenValidRepositoryConfigurations(){
        String validBody = createUrlRequestBody("http://testsite.com");
        when(goApiPluginRequest.requestBody()).thenReturn(validBody);

        List responseList = repositoryConfigs.handleValidateRepositoryConfiguration(goApiPluginRequest);

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