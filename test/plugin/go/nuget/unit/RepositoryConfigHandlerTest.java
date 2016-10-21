package plugin.go.nuget.unit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plugin.go.nuget.ConnectionHandler;
import plugin.go.nuget.RepositoryConfigHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static utils.Constants.REPOSITORY_CONFIGURATION;

public class RepositoryConfigHandlerTest {
    RepositoryConfigHandler repositoryConfigHandler;
    ConnectionHandler connectionHandler;

    @Before
    public void setUp() {
        connectionHandler = mock(ConnectionHandler.class);
        repositoryConfigHandler = new RepositoryConfigHandler(connectionHandler);
    }

    @Test
    public void shouldErrorWhenInvalidRepositoryConfiguration() {
        Map invalidBody = createUrlRequestBody("", "", "");

        List errorList = repositoryConfigHandler.handleValidateConfiguration(invalidBody);

        Assert.assertFalse(errorList.isEmpty());
    }

    @Test
    public void shouldReturnEmptyErrorListWhenValidRepositoryConfigurations() {
        Map validBody = createUrlRequestBody("http://testsite.com", "", "");

        List errorList = repositoryConfigHandler.handleValidateConfiguration(validBody);

        Assert.assertTrue(errorList.isEmpty());
    }

    @Test
    public void shouldUseConnectionHandlerToCheckConnectionWithMetadata() {
        String SOME_URL = "http://www.nuget.com/";
        String SOME_USERNAME = "SomeUsername";
        String SOME_PASSWORD = "somePassword";

        repositoryConfigHandler.handleCheckRepositoryConnection(createUrlRequestBody(SOME_URL, SOME_USERNAME, SOME_PASSWORD));

        verify(connectionHandler).checkConnectionToUrlWithMetadata(SOME_URL, SOME_USERNAME, SOME_PASSWORD);
    }

    private Map createUrlRequestBody(String url, String username, String password) {
        Map urlMap = new HashMap();
        urlMap.put("value", url);
        Map fieldsMap = new HashMap();
        fieldsMap.put("REPOSITORY_URL", urlMap);
        Map usernameMap = new HashMap();
        usernameMap.put("value", username);
        fieldsMap.put("USERNAME", usernameMap);
        Map passwordMap = new HashMap();
        passwordMap.put("value", password);
        fieldsMap.put("PASSWORD", passwordMap);
        Map bodyMap = new HashMap();
        bodyMap.put(REPOSITORY_CONFIGURATION, fieldsMap);
        return bodyMap;
    }


}