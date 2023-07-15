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

package plugin.go.nuget.e2e;


import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plugin.go.nuget.NugetController;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.Constants.*;

public class NugetPluginTest {

    NugetController nugetController;
    GoPluginApiRequest goApiPluginRequest;


    @BeforeEach
    public void setUp() {
        nugetController = new NugetController();
        goApiPluginRequest = mock(GoPluginApiRequest.class);
    }

    @Test
    public void shouldReturnConfigurationsWhenHandlingRepositoryConfigurationRequest() {
        String expectedRepositoryConfiguration = "{\"PASSWORD\":{\"display-order\":\"2\",\"display-name\":\"Password (use only with https)\",\"part-of-identity\":false,\"secure\":true,\"required\":false}," +
                "\"USERNAME\":{\"display-order\":\"1\",\"display-name\":\"Username\",\"part-of-identity\":false,\"secure\":false,\"required\":false}," +
                "\"REPO_URL\":{\"display-order\":\"0\",\"display-name\":\"Repository Url\",\"part-of-identity\":true,\"secure\":false,\"required\":true}}";

        Map expectedRepositoryConfigurationMap = (Map) new GsonBuilder().create().fromJson(expectedRepositoryConfiguration, Object.class);
        when(goApiPluginRequest.requestName()).thenReturn(REPOSITORY_CONFIGURATION);

        GoPluginApiResponse response = nugetController.handle(goApiPluginRequest);
        Map responseBodyMap = (Map) new GsonBuilder().create().fromJson(response.responseBody(), Object.class);

        assertEquals(SUCCESS_RESPONSE_CODE, response.responseCode());
        assertEquals(expectedRepositoryConfigurationMap, responseBodyMap);
    }

    @Test
    public void shouldReturnNoErrorsForCorrectRepositoryConfiguration() {
        String requestBody = "{\"repository-configuration\":" +
                "{\"REPO_URL\":{\"value\":\"http://nuget.org/api/v2/\"}," +
                "\"USERNAME\":{\"value\":\"\"}," +
                "\"PASSWORD\":{\"value\":\"\"}}}";
        when(goApiPluginRequest.requestName()).thenReturn(VALIDATE_REPOSITORY_CONFIGURATION);
        when(goApiPluginRequest.requestBody()).thenReturn(requestBody);

        GoPluginApiResponse response = nugetController.handle(goApiPluginRequest);

        List responseBodyList = (List) new GsonBuilder().create().fromJson(response.responseBody(), Object.class);

        assertEquals(SUCCESS_RESPONSE_CODE, response.responseCode());
        assertTrue(responseBodyList.isEmpty());
    }

    @Test
    public void shouldSuccessfullyConnectToRepository() {
        String requestBody = "{\"repository-configuration\":" +
                "{\"REPO_URL\":{\"value\":\"http://nuget.org/api/v2/\"}," +
                "\"USERNAME\":{\"value\":\"\"}," +
                "\"PASSWORD\":{\"value\":\"\"}}}";
        String expectedResponseAsString = "{\"messages\":[\"Successfully connected to repository url provided\"],\"status\":\"success\"}";
        when(goApiPluginRequest.requestName()).thenReturn(CHECK_REPOSITORY_CONNECTION);
        when(goApiPluginRequest.requestBody()).thenReturn(requestBody);

        GoPluginApiResponse response = nugetController.handle(goApiPluginRequest);

        Map responseAsMap = (Map) new GsonBuilder().create().fromJson(response.responseBody(), Object.class);
        Map expectedResponse = (Map) new GsonBuilder().create().fromJson(expectedResponseAsString, Object.class);
        assertEquals(SUCCESS_RESPONSE_CODE, response.responseCode());
        assertEquals(expectedResponse, responseAsMap);

    }

    @Test
    public void shouldReturnConfigurationsWhenHandlingPackageConfigurationRequest() {
        String expectedPackageConfiguration = "{\"POLL_VERSION_TO\":{\"display-name\":\"Version to poll \\u003c\",\"secure\":false,\"display-order\":\"2\",\"required\":false,\"part-of-identity\":false}," +
                "\"POLL_VERSION_FROM\":{\"display-name\":\"Version to poll \\u003e\\u003d\",\"secure\":false,\"display-order\":\"1\",\"required\":false,\"part-of-identity\":false}," +
                "\"PACKAGE_ID\":{\"display-name\":\"Package ID\",\"secure\":false,\"display-order\":\"0\",\"required\":true,\"part-of-identity\":true}," +
                "\"INCLUDE_PRE_RELEASE\":{\"display-name\":\"Include Prerelease? (yes/no, defaults to yes)\",\"secure\":false,\"display-order\":\"3\",\"required\":false,\"part-of-identity\":false}}\n";
        Map expectedPackageConfigurationMap = (Map) new GsonBuilder().create().fromJson(expectedPackageConfiguration, Object.class);

        when(goApiPluginRequest.requestName()).thenReturn(PACKAGE_CONFIGURATION);

        GoPluginApiResponse response = nugetController.handle(goApiPluginRequest);
        Map responseBodyMap = (Map) new GsonBuilder().create().fromJson(response.responseBody(), Object.class);

        assertEquals(SUCCESS_RESPONSE_CODE, response.responseCode());
        assertEquals(expectedPackageConfigurationMap, responseBodyMap);
    }

    @Test
    public void shouldReturnNoErrorsForCorrectPackageConfiguration() {
        String requestBody = "{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"http://nuget.org/api/v2/\"}}," +
                "\"package-configuration\":{\"PACKAGE_ID\":{\"value\":\"NUnit\"}}}";
        when(goApiPluginRequest.requestName()).thenReturn(VALIDATE_PACKAGE_CONFIGURATION);
        when(goApiPluginRequest.requestBody()).thenReturn(requestBody);

        GoPluginApiResponse response = nugetController.handle(goApiPluginRequest);

        List responseBodyList = (List) new GsonBuilder().create().fromJson(response.responseBody(), Object.class);

        assertEquals(SUCCESS_RESPONSE_CODE, response.responseCode());
        assertTrue(responseBodyList.isEmpty());
    }

    @Test
    public void shouldSuccessfullyConnectToPackage() {
        String requestBody = "{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"http://nuget.org/api/v2/\"}},"+
                              "\"package-configuration\":{"+"\"PACKAGE_ID\":{\"value\":\"JQuery\"},"+
                                                            "\"POLL_VERSION_FROM\":{\"value\":\"2.2.3\"},"+
                                                            "\"POLL_VERSION_TO\":{\"value\":\"2.2.5\"}," +
                                                            "\"INCLUDE_PRE_RELEASE\":{\"value\":\"yes\"}}}\n";
        String expectedResponseAsString = "{\"messages\":[\"Successfully found revision: jQuery-2.2.4\"],\"status\":\"success\"}";
        when(goApiPluginRequest.requestName()).thenReturn(CHECK_PACKAGE_CONNECTION);
        when(goApiPluginRequest.requestBody()).thenReturn(requestBody);

        GoPluginApiResponse response = nugetController.handle(goApiPluginRequest);

        Map responseAsMap = (Map) new GsonBuilder().create().fromJson(response.responseBody(), Object.class);
        Map expectedResponse = (Map) new GsonBuilder().create().fromJson(expectedResponseAsString, Object.class);
        assertEquals(SUCCESS_RESPONSE_CODE, response.responseCode());
        assertEquals(expectedResponse, responseAsMap);

    }

    @Test
    public void getLatestRevisionShouldBeSuccessful() {
        String requestBody = "{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"http://nuget.org/api/v2/\"}},"+
                "\"package-configuration\":{"+"\"PACKAGE_ID\":{\"value\":\"JQuery\"},"+
                "\"POLL_VERSION_FROM\":{\"value\":\"2.2.3\"},"+
                "\"POLL_VERSION_TO\":{\"value\":\"2.2.5\"}," +
                "\"INCLUDE_PRE_RELEASE\":{\"value\":\"yes\"}}}\n";
        when(goApiPluginRequest.requestName()).thenReturn(LATEST_REVISION);
        when(goApiPluginRequest.requestBody()).thenReturn(requestBody);

        GoPluginApiResponse response = nugetController.handle(goApiPluginRequest);

        assertEquals(SUCCESS_RESPONSE_CODE, response.responseCode());
    }

    @Test
    public void getLatestRevisionSinceShouldBeSuccessful() {
        String requestBody = "{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"http://nuget.org/api/v2/\"}},"+
                "\"package-configuration\":{\"PACKAGE_ID\":{\"value\":\"jQuery\"},"+
                                           "\"POLL_VERSION_FROM\":{\"value\":\"2.2.3\"},"+
                                           "\"POLL_VERSION_TO\":{\"value\":\"3\"},"+
                                           "\"INCLUDE_PRE_RELEASE\":{\"value\":\"no\"}},"+
                "\"previous-revision\":{\"revision\":\"jQuery-2.2.4\","+
                                       "\"timestamp\":\"2016-06-16T16:31:00.873Z\","+
                                       "\"data\":{\"LOCATION\":\"http://www.nuget.org/api/v2/package/jQuery/2.2.4\",\"VERSION\":\"2.2.4\"}}}";
        when(goApiPluginRequest.requestName()).thenReturn(LATEST_REVISION_SINCE);
        when(goApiPluginRequest.requestBody()).thenReturn(requestBody);

        GoPluginApiResponse response = nugetController.handle(goApiPluginRequest);

        assertEquals(SUCCESS_RESPONSE_CODE, response.responseCode());
    }

}
