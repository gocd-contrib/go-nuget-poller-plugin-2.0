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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import plugin.go.nuget.NugetQueryBuilder;

public class NugetQueryBuilderTest {

    NugetQueryBuilder nugetQueryBuilder;
    private static final String PACKAGE_ID = "NUnit";
    private static final String DEFAULT_N_UNIT_OPTIONS = "/GetUpdates()?packageIds='NUnit'&versions='0.0.1'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1";

    @Before
    public void setup() {
        nugetQueryBuilder = new NugetQueryBuilder();
    }

    @Test
    public void shouldUseDefaultsIfOnlyNUnitIsChosen() {
        String query = nugetQueryBuilder.getQuery(PACKAGE_ID, "", "", "", "");
        Assert.assertEquals(DEFAULT_N_UNIT_OPTIONS, query);
    }

    @Test
    public void shouldNotFailIfOptionalParametersAreNotPassedIn() {
        String query = nugetQueryBuilder.getQuery(PACKAGE_ID, "", null, null, null);
        Assert.assertEquals(DEFAULT_N_UNIT_OPTIONS, query);
    }

    @Test
    public void shouldHandleUpperBound() {
        String expectedOptions = "/GetUpdates()?packageIds='NUnit'&versions='0.0.1'&includePrerelease=true&includeAllVersions=true&$filter=Version%20lt%20'1.2'&$orderby=Version%20desc&$top=1";
        String query = nugetQueryBuilder.getQuery(PACKAGE_ID, "", null, "1.2", "yes");
        Assert.assertEquals(expectedOptions, query);

    }

    @Test
    public void shouldHandleLowerBound() {
        String expectedOptions = "/GetUpdates()?packageIds='NUnit'&versions='1.3'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1";
        String query = nugetQueryBuilder.getQuery(PACKAGE_ID, "", "1.3", null, "yes");
        Assert.assertEquals(expectedOptions, query);
    }

    @Test
    public void shouldHandleLowerAndUpperBound() {
        String expectedOptions = "/GetUpdates()?packageIds='NUnit'&versions='1.3'&includePrerelease=true&includeAllVersions=true&$filter=Version%20lt%20'1.6'&$orderby=Version%20desc&$top=1";
        String query = nugetQueryBuilder.getQuery(PACKAGE_ID, "", "1.3", "1.6", "yes");
        Assert.assertEquals(expectedOptions, query);
    }

    @Test
    public void shouldDefaultToIncludePreRelease() {
        String query = nugetQueryBuilder.getQuery(PACKAGE_ID, "", "", "", "invalidPreRelease");
        Assert.assertEquals(DEFAULT_N_UNIT_OPTIONS, query);
    }

    @Test
    public void shouldNotIncludePreReleaseWhenUserInputsNo() {
        String expectedOptions = "/GetUpdates()?packageIds='NUnit'&versions='0.0.1'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1";
        String query = nugetQueryBuilder.getQuery(PACKAGE_ID, "", "", "", "invalidPreRelease");
        Assert.assertEquals(expectedOptions, query);
    }

}
