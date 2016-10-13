package plugin.go.nuget.unit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plugin.go.nuget.RepositoryConfigHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.Constants.REPOSITORY_CONFIGURATION;

public class RepositoryConfigHandlerTest {
    RepositoryConfigHandler repositoryConfigHandler;

    @Before
    public void setUp() {
        repositoryConfigHandler = new RepositoryConfigHandler();
    }

    @Test
    public void shouldErrorWhenInvalidRepositoryConfiguration(){
        Map invalidBody = createUrlRequestBody("");

        List errorList = repositoryConfigHandler.handleValidateRepositoryConfiguration(invalidBody);

        Assert.assertFalse(errorList.isEmpty());
    }

    @Test
    public void shouldReturnEmptyErrorListWhenValidRepositoryConfigurations(){
        Map validBody = createUrlRequestBody("http://testsite.com");

        List errorList = repositoryConfigHandler.handleValidateRepositoryConfiguration(validBody);

        Assert.assertTrue(errorList.isEmpty());
    }


    private Map createUrlRequestBody(String url) {
        Map urlMap =  new HashMap();
        urlMap.put("value", url);
        Map fieldsMap = new HashMap();
        fieldsMap.put("REPOSITORY_URL", urlMap);
        Map invalidBodyMap = new HashMap();
        invalidBodyMap.put(REPOSITORY_CONFIGURATION, fieldsMap);
        return invalidBodyMap;
    }

}