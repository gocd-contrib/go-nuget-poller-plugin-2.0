package plugin.go.nuget.unit;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plugin.go.nuget.RepositoryConfigHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.Constants.REPOSITORY_CONFIGURATION;

public class RepositoryConfigHandlerTest {
    RepositoryConfigHandler repositoryConfigHandler;
    GoPluginApiRequest goApiPluginRequest;

    @Before
    public void setUp() {
        repositoryConfigHandler = new RepositoryConfigHandler();
        goApiPluginRequest = mock(GoPluginApiRequest.class);

    }

    @Test
    public void shouldErrorWhenInvalidRepositoryConfiguration(){
        String invalidBody = createUrlRequestBody("");
        when(goApiPluginRequest.requestBody()).thenReturn(invalidBody);

        List errorList = repositoryConfigHandler.handleValidateRepositoryConfiguration(goApiPluginRequest);

        Assert.assertFalse(errorList.isEmpty());
    }

    @Test
    public void shouldReturnEmptyErrorListWhenValidRepositoryConfigurations(){
        String validBody = createUrlRequestBody("http://testsite.com");
        when(goApiPluginRequest.requestBody()).thenReturn(validBody);

        List errorList = repositoryConfigHandler.handleValidateRepositoryConfiguration(goApiPluginRequest);

        Assert.assertTrue(errorList.isEmpty());
    }


    private String createUrlRequestBody(String url) {
        Map urlMap =  new HashMap();
        urlMap.put("value", url);
        Map fieldsMap = new HashMap();
        fieldsMap.put("REPOSITORY_URL", urlMap);
        Map invalidBodyMap = new HashMap();
        invalidBodyMap.put(REPOSITORY_CONFIGURATION, fieldsMap);
        return new GsonBuilder().serializeNulls().create().toJson(invalidBodyMap);
    }

}