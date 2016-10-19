package plugin.go.nuget.unit;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plugin.go.nuget.ConnectionHandler;
import plugin.go.nuget.NuGetFeedDocument;
import plugin.go.nuget.PackageConfigHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static utils.Constants.PACKAGE_CONFIGURATION;
import static utils.Constants.REPOSITORY_CONFIGURATION;

public class PackageConfigHandlerTest {
    private static final String URL = "SOME_URL";
    private static final String USERNAME = "SOME_USERNAME";
    private static final String PASSWORD = "SOME_PASSWORD";
    private static final String QUERYSTRING = "/GetUpdates()?packageIds='NUnit'&versions='0.0.1'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1";
    private static final String PACKAGE_ID = "NUnit";

    PackageConfigHandler packageConfigHandler;
    ConnectionHandler connectionHandler;

    @Before
    public void setup() {
        connectionHandler = mock(ConnectionHandler.class);
        packageConfigHandler = new PackageConfigHandler(connectionHandler);
    }

    @Test
    public void shouldErrorWhenPackageIDisMissing() {
        Map requestBody = createSampleRequestForPackageConfiguration("");

        List errorList = packageConfigHandler.handleValidatePackageConfiguration(requestBody);
        Assert.assertFalse(errorList.isEmpty());
        Assert.assertThat(errorList.get(0).toString(), containsString("Package ID cannot be empty"));
    }

    @Test
    public void shouldNotErrorWhenPackageConfigurationsAreValid() {
        Map requestBody = createSampleRequestForPackageConfiguration("ID");

        List errorList = packageConfigHandler.handleValidatePackageConfiguration(requestBody);
        Assert.assertTrue(errorList.isEmpty());
    }

    @Test
    public void shouldGetLatestRevisionDataFromConnection() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = dateFormat.parse("27/09/2016");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Map data = new HashMap();
        data.put("VERSION", "3.5.0");

        PackageRevision packageRevision = new PackageRevision("REVISION", date, "USER", "REVISION_COMMENT", "TRACKBACK_URL", data);

        NuGetFeedDocument mockDocument = mock(NuGetFeedDocument.class);
        when(mockDocument.getPackageRevision(false)).thenReturn(packageRevision);
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(mockDocument);

        Map revisionMap = packageConfigHandler.handleLatestRevision(createSampleRequestForCheckPackageConnection(URL, USERNAME, PASSWORD, PACKAGE_ID));

        verify(connectionHandler).getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD);
        Assert.assertEquals(packageRevision.getRevision(), revisionMap.get("revision"));
        Assert.assertThat((String) revisionMap.get("timestamp"), containsString("2016-09-27"));
        Assert.assertEquals(packageRevision.getUser(), revisionMap.get("user"));
        Assert.assertEquals(packageRevision.getRevisionComment(), revisionMap.get("revisionComment"));
        Assert.assertEquals(packageRevision.getData(), revisionMap.get("data"));
        Assert.assertEquals(packageRevision.getData().get("VERSION"), "3.5.0");
    }

    @Test
    public void shouldReturnEmptyMapIfNoPackageIsFound() {
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(null);
        Map revisionMap = packageConfigHandler.handleLatestRevision(createSampleRequestForCheckPackageConnection(URL, USERNAME, PASSWORD, PACKAGE_ID));
        Assert.assertTrue(revisionMap.isEmpty());
    }

    @Test
    public void shouldFailIfPackageIsNull() {
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(null);

        Map revisionMap = packageConfigHandler.handleCheckPackageConnection(createSampleRequestForCheckPackageConnection(URL, USERNAME, PASSWORD, PACKAGE_ID));

        verify(connectionHandler).getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD);

        Assert.assertEquals("failure", revisionMap.get("status"));
        Assert.assertEquals(((List) revisionMap.get("messages")).get(0), "No packages found");
    }

    @Test
    public void shouldFailIfPackageIsEmptyMap() {
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(null);

        Map revisionMap = packageConfigHandler.handleCheckPackageConnection(createSampleRequestForCheckPackageConnection(URL, USERNAME, PASSWORD, PACKAGE_ID));

        verify(connectionHandler).getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD);

        Assert.assertEquals("failure", revisionMap.get("status"));
        Assert.assertEquals(((List) revisionMap.get("messages")).get(0), "No packages found");
    }


    @Test
    public void shouldSucceedIfPackageExists() {
        String revision = "NUnit3.5.1";
        PackageRevision packageRevision = new PackageRevision(revision, new Date(), "USER", "REVISION_COMMENT", "TRACKBACK_URL", new HashMap());
        NuGetFeedDocument mockDocument = mock(NuGetFeedDocument.class);
        when(mockDocument.getPackageRevision(false)).thenReturn(packageRevision);
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(mockDocument);

        Map revisionMap = packageConfigHandler.handleCheckPackageConnection(createSampleRequestForCheckPackageConnection(URL, USERNAME, PASSWORD, PACKAGE_ID));

        verify(connectionHandler).getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD);

        Assert.assertEquals("success", revisionMap.get("status"));
        Assert.assertEquals(((List) revisionMap.get("messages")).get(0), "Successfully found revision: " + revision);
    }

    @Test
    public void shouldReturnEmptyMapIfNoNewerPackageExists() {
        String version = "3.5.1";
        String latestRevisionSinceQueryString = "/GetUpdates()?packageIds='NUnit'&versions='"+version+"'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1";
        NuGetFeedDocument mockDocument = mock(NuGetFeedDocument.class);
        when(mockDocument.getPackageRevision(true)).thenReturn(null);
        when(connectionHandler.getNuGetFeedDocument(URL, latestRevisionSinceQueryString, USERNAME, PASSWORD)).thenReturn(mockDocument);

        Map revisionMap = packageConfigHandler.handleLatestRevisionSince(createSampleRequestForLatestRevisionSince(URL, USERNAME, PASSWORD, PACKAGE_ID, version));

        verify(connectionHandler).getNuGetFeedDocument(URL, latestRevisionSinceQueryString, USERNAME, PASSWORD);

        Assert.assertTrue(revisionMap.isEmpty());
    }

    @Test
    public void shouldReturnPackageDataIfNewerPackageExists() {
        String version = "1.1.1";
        String revision = "NUnit-3.5.1";
        String latestRevisionSinceQueryString = "/GetUpdates()?packageIds='NUnit'&versions='"+version+"'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1";
        NuGetFeedDocument mockDocument = mock(NuGetFeedDocument.class);
        PackageRevision mockPackageRevision = mock(PackageRevision.class);
        when(mockDocument.getPackageRevision(true)).thenReturn(mockPackageRevision);
        when(mockPackageRevision.getRevision()).thenReturn(revision);
        when(mockPackageRevision.getTimestamp()).thenReturn(new Date());
        when(connectionHandler.getNuGetFeedDocument(URL, latestRevisionSinceQueryString, USERNAME, PASSWORD)).thenReturn(mockDocument);

        Map revisionMap = packageConfigHandler.handleLatestRevisionSince(createSampleRequestForLatestRevisionSince(URL, USERNAME, PASSWORD, PACKAGE_ID, version));

        verify(connectionHandler).getNuGetFeedDocument(URL, latestRevisionSinceQueryString, USERNAME, PASSWORD);

        Assert.assertEquals(revision, revisionMap.get("revision"));
    }

    private Map createSampleRequestForPackageConfiguration(String packageID) {
        Map packageIDMap = new HashMap();
        packageIDMap.put("value", packageID);

        Map packageConfigurationMap = new HashMap();
        packageConfigurationMap.put("PACKAGE_ID", packageIDMap);

        Map requestMap = new HashMap();
        requestMap.put(PACKAGE_CONFIGURATION, packageConfigurationMap);

        return requestMap;
    }

    private Map createSampleRequestForRepositoryConfiguration(String url, String username, String password) {
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

    private Map createSampleRequestForCheckPackageConnection(String url, String username, String password, String packageID) {
        Map repoConfigs = createSampleRequestForRepositoryConfiguration(url, username, password);
        Map packageConfigs = createSampleRequestForPackageConfiguration(packageID);
        Map compositeMap = new HashMap();
        compositeMap.putAll(repoConfigs);
        compositeMap.putAll(packageConfigs);
        return compositeMap;
    }

    private Map createMapOfPreviousRevision(String version) {
        Map dataMap = new HashMap();
        dataMap.put("VERSION", version);
        Map revisionInfoMap = new HashMap();
        revisionInfoMap.put("data", dataMap);
        revisionInfoMap.put("timestamp", "2011-07-14T19:43:37.100Z");
        revisionInfoMap.put("revision", "abc-10.2.1.rpm");
        Map previousRevisionMap = new HashMap();
        previousRevisionMap.put("previous-revision", revisionInfoMap);
        return previousRevisionMap;
    }

    private Map createSampleRequestForLatestRevisionSince(String url, String username, String password, String packageID, String version){
        Map repoConfigs = createSampleRequestForRepositoryConfiguration(url, username, password);
        Map packageConfigs = createSampleRequestForPackageConfiguration(packageID);
        Map revisionMap = createMapOfPreviousRevision(version);
        Map compositeMap = new HashMap();
        compositeMap.putAll(repoConfigs);
        compositeMap.putAll(packageConfigs);
        compositeMap.putAll(revisionMap);
        return compositeMap;
    }
}