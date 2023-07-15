/*
 * Copyright 2016 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package plugin.go.nuget.unit;


import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plugin.go.nuget.ConnectionHandler;
import plugin.go.nuget.NuGetFeedDocument;
import plugin.go.nuget.NugetQueryBuilder;
import plugin.go.nuget.PackagePoller;
import plugin.go.nuget.builders.RequestBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @BeforeEach
    public void setup() {
        connectionHandler = mock(ConnectionHandler.class);
        packagePoller = new PackagePoller(connectionHandler, new NugetQueryBuilder());
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
        assertEquals(packageRevision.getRevision(), revisionMap.get("revision"));
        assertThat((String) revisionMap.get("timestamp")).contains("2016-09-27");
        assertEquals(packageRevision.getUser(), revisionMap.get("user"));
        assertEquals(packageRevision.getRevisionComment(), revisionMap.get("revisionComment"));
        assertEquals(packageRevision.getData(), revisionMap.get("data"));
        assertEquals(packageRevision.getData().get("VERSION"), "3.5.0");
    }

    @Test
    public void shouldReturnEmptyMapIfNoPackageIsFound() {
        setUpRequestWithPackageAndRepoConfigurations();
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(null);
        Map revisionMap = packagePoller.handleLatestRevision(sampleRequest);
        assertTrue(revisionMap.isEmpty());
    }

    @Test
    public void shouldFailIfPackageIsNull() {
        setUpRequestWithPackageAndRepoConfigurations();
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(null);

        Map revisionMap = packagePoller.handleCheckPackageConnection(sampleRequest);

        verify(connectionHandler).getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD);

        assertEquals("failure", revisionMap.get("status"));
        assertEquals(((List) revisionMap.get("messages")).get(0), "No packages found");
    }

    @Test
    public void shouldFailIfPackageIsEmptyMap() {
        setUpRequestWithPackageAndRepoConfigurations();
        when(connectionHandler.getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD)).thenReturn(new NuGetFeedDocument(null));

        Map revisionMap = packagePoller.handleCheckPackageConnection(sampleRequest);

        verify(connectionHandler).getNuGetFeedDocument(URL, QUERYSTRING, USERNAME, PASSWORD);

        assertEquals("failure", revisionMap.get("status"));
        assertEquals(((List) revisionMap.get("messages")).get(0), "No packages found");
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

        assertEquals("success", revisionMap.get("status"));
        assertEquals(((List) revisionMap.get("messages")).get(0), "Successfully found revision: " + revision);
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

        assertTrue(revisionMap.isEmpty());
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

        assertEquals(revision, revisionMap.get("revision"));
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
