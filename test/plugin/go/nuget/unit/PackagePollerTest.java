package plugin.go.nuget.unit;


import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plugin.go.nuget.ConnectionHandler;
import plugin.go.nuget.NuGetFeedDocument;
import plugin.go.nuget.PackagePoller;
import plugin.go.nuget.builders.RequestBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;

public class PackagePollerTest {
    private static final String URL = "SOME_URL";
    private static final String USERNAME = "SOME_USERNAME";
    private static final String PASSWORD = "SOME_PASSWORD";
    private static final String QUERYSTRING = "/GetUpdates()?packageIds='NUnit'&versions='0.0.1'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1";
    private static final String PACKAGE_ID = "NUnit";
    private Map sampleRequest;

    PackagePoller packagePoller;
    ConnectionHandler connectionHandler;

    @Before
    public void setup() {
        connectionHandler = mock(ConnectionHandler.class);
        packagePoller = new PackagePoller(connectionHandler);
    }

    public void setUpRequestWithPackageAndRepoConfigurations() {
        sampleRequest = new RequestBuilder().withRespositoryConfiguration(URL, USERNAME, PASSWORD).withPackageConfiguration(PACKAGE_ID).build();
    }

    @Test
    public void shouldGetLatestRevisionDataFromConnection() {
        setUpRequestWithPackageAndRepoConfigurations();
        Map data = new HashMap();
        data.put("VERSION", "3.5.0");
        PackageRevision packageRevision = new PackageRevision("REVISION", buildDate(), "USER", "REVISION_COMMENT", "TRACKBACK_URL", data);
        NuGetFeedDocument mockDocument = mock(NuGetFeedDocument.class);
        when(mockDocument.getPackageRevision(false)).thenReturn(packageRevision);
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(mockDocument);

        Map revisionMap = packagePoller.handleLatestRevision(sampleRequest);

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
        setUpRequestWithPackageAndRepoConfigurations();
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(null);
        Map revisionMap = packagePoller.handleLatestRevision(sampleRequest);
        Assert.assertTrue(revisionMap.isEmpty());
    }

    @Test
    public void shouldFailIfPackageIsNull() {
        setUpRequestWithPackageAndRepoConfigurations();
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(null);

        Map revisionMap = packagePoller.handleCheckPackageConnection(sampleRequest);

        verify(connectionHandler).getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD);

        Assert.assertEquals("failure", revisionMap.get("status"));
        Assert.assertEquals(((List) revisionMap.get("messages")).get(0), "No packages found");
    }

    @Test
    public void shouldFailIfPackageIsEmptyMap() {
        setUpRequestWithPackageAndRepoConfigurations();
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(new NuGetFeedDocument(null));

        Map revisionMap = packagePoller.handleCheckPackageConnection(sampleRequest);

        verify(connectionHandler).getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD);

        Assert.assertEquals("failure", revisionMap.get("status"));
        Assert.assertEquals(((List) revisionMap.get("messages")).get(0), "No packages found");
    }

    @Test
    public void shouldSucceedIfPackageExists() {
        setUpRequestWithPackageAndRepoConfigurations();
        String revision = "NUnit3.5.1";
        PackageRevision packageRevision = new PackageRevision(revision, new Date(), "USER", "REVISION_COMMENT", "TRACKBACK_URL", new HashMap());
        NuGetFeedDocument mockDocument = mock(NuGetFeedDocument.class);
        when(mockDocument.getPackageRevision(false)).thenReturn(packageRevision);
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(mockDocument);

        Map revisionMap = packagePoller.handleCheckPackageConnection(sampleRequest);

        verify(connectionHandler).getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD);

        Assert.assertEquals("success", revisionMap.get("status"));
        Assert.assertEquals(((List) revisionMap.get("messages")).get(0), "Successfully found revision: " + revision);
    }


    @Test
    public void shouldReturnEmptyMapIfNoNewerPackageExists() {
        String version = "3.5.1";
        Map sampleRequest = new RequestBuilder().withRespositoryConfiguration(URL, USERNAME, PASSWORD)
                .withPackageConfiguration(PACKAGE_ID)
                .withPreviousRevision(version)
                .build();
        String latestRevisionSinceQueryString = "/GetUpdates()?packageIds='NUnit'&versions='" + version + "'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1";
        NuGetFeedDocument mockDocument = mock(NuGetFeedDocument.class);
        when(mockDocument.getPackageRevision(true)).thenReturn(null);
        when(connectionHandler.getNuGetFeedDocument(URL, latestRevisionSinceQueryString, USERNAME, PASSWORD)).thenReturn(mockDocument);

        Map revisionMap = packagePoller.handleLatestRevisionSince(sampleRequest);

        verify(connectionHandler).getNuGetFeedDocument(URL, latestRevisionSinceQueryString, USERNAME, PASSWORD);

        Assert.assertTrue(revisionMap.isEmpty());
    }

    @Test
    public void shouldReturnPackageDataIfNewerPackageExists() {
        String version = "1.1.1";
        Map sampleRequest = new RequestBuilder().withRespositoryConfiguration(URL, USERNAME, PASSWORD)
                .withPackageConfiguration(PACKAGE_ID)
                .withPreviousRevision(version)
                .build();
        String revision = "NUnit-3.5.1";
        String latestRevisionSinceQueryString = "/GetUpdates()?packageIds='NUnit'&versions='" + version + "'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1";
        NuGetFeedDocument mockDocument = mock(NuGetFeedDocument.class);
        PackageRevision mockPackageRevision = mock(PackageRevision.class);
        when(mockDocument.getPackageRevision(true)).thenReturn(mockPackageRevision);
        when(mockPackageRevision.getRevision()).thenReturn(revision);
        when(mockPackageRevision.getTimestamp()).thenReturn(new Date());
        when(connectionHandler.getNuGetFeedDocument(URL, latestRevisionSinceQueryString, USERNAME, PASSWORD)).thenReturn(mockDocument);

        Map revisionMap = packagePoller.handleLatestRevisionSince(sampleRequest);

        verify(connectionHandler).getNuGetFeedDocument(URL, latestRevisionSinceQueryString, USERNAME, PASSWORD);

        Assert.assertEquals(revision, revisionMap.get("revision"));
    }

    private Date buildDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = dateFormat.parse("27/09/2016");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
