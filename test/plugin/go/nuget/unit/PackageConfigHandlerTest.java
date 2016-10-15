package plugin.go.nuget.unit;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import plugin.go.nuget.ConnectionHandler;
import plugin.go.nuget.NuGetFeedDocument;
import plugin.go.nuget.PackageConfigHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utils.Constants.PACKAGE_CONFIGURATION;
import static utils.Constants.REPOSITORY_CONFIGURATION;

public class PackageConfigHandlerTest {
    private static String URL = "SOME_URL";
    private static String USERNAME = "SOME_USERNAME";
    private static String PASSWORD = "SOME_PASSWORD";

    PackageConfigHandler packageConfigHandler;
    ConnectionHandler connectionHandler;

    @Before
    public void setup() {
        connectionHandler = mock(ConnectionHandler.class);
        packageConfigHandler = new PackageConfigHandler(connectionHandler);
    }

    @Test
    public void shouldErrorWhenPackageIDisMissing() {
        Map requestBody = createPackageConfigurationRequestBody("");

        List errorList = packageConfigHandler.handleValidatePackageConfiguration(requestBody);
        Assert.assertFalse(errorList.isEmpty());
        Assert.assertThat(errorList.get(0).toString(), containsString("Package ID cannot be empty"));
    }

    @Test
    public void shouldNotErrorWhenPackageConfigurationsAreValid() {
        Map requestBody = createPackageConfigurationRequestBody("ID");

        List errorList = packageConfigHandler.handleValidatePackageConfiguration(requestBody);
        Assert.assertTrue(errorList.isEmpty());
    }

    @Test
    public void shouldGetLatestRevisionDataFromConnection() {
        PackageRevision packageRevision = new PackageRevision("REVISION", new Date(), "USER", "REVISION_COMMENT", "TRACKBACK_URL", new HashMap());

        NuGetFeedDocument mockDocument = mock(NuGetFeedDocument.class);
        when(mockDocument.getPackageRevision(false)).thenReturn(packageRevision);
        when(connectionHandler.getNuGetFeedDocument(URL, USERNAME, PASSWORD)).thenReturn(mockDocument);

        Map revisionMap = packageConfigHandler.handleLatestRevision(createUrlRequestBody(URL, USERNAME, PASSWORD));

        verify(connectionHandler).getNuGetFeedDocument(URL, USERNAME, PASSWORD);
        Assert.assertEquals(packageRevision.getRevision(), revisionMap.get("revision"));
        Assert.assertEquals(packageRevision.getTimestamp(), revisionMap.get("timestamp"));
        Assert.assertEquals(packageRevision.getUser(), revisionMap.get("user"));
        Assert.assertEquals(packageRevision.getRevisionComment(), revisionMap.get("revisionComment"));
        Assert.assertEquals(packageRevision.getData(), revisionMap.get("data"));
    }

    @Test
    public void shouldReturnEmptyMapIfNoPackageIsFound(){
        when(connectionHandler.getNuGetFeedDocument(URL, USERNAME, PASSWORD)).thenReturn(null);
        Map revisionMap = packageConfigHandler.handleLatestRevision(createUrlRequestBody(URL, USERNAME, PASSWORD));
        Assert.assertTrue(revisionMap.isEmpty());
    }


    private Map createPackageConfigurationRequestBody(String packageID) {
        Map packageIDMap = new HashMap();
        packageIDMap.put("value", packageID);

        Map packageConfigurationMap = new HashMap();
        packageConfigurationMap.put("PACKAGE_ID", packageIDMap);

        Map requestMap = new HashMap();
        requestMap.put(PACKAGE_CONFIGURATION, packageConfigurationMap);

        return requestMap;
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