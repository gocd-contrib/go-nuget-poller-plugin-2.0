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

package plugin.go.nuget;

public class NugetQueryBuilder {

    public String getQuery(String packageId, String knownVersion, String versionFrom, String versionTo, String includePreRelease) {
        StringBuilder query = new StringBuilder();
        query.append("/GetUpdates()?");
        query.append(String.format("packageIds='%s'", packageId));
        query.append(String.format("&versions='%s'", calculateVersion(knownVersion, versionFrom)));
        query.append("&includePrerelease=").append(convertIncludePreRelease(includePreRelease));
        query.append("&includeAllVersions=true");//has to be true, filter gets applied later
        if (versionTo != null && !versionTo.isEmpty()) {
            query.append("&$filter=Version%20lt%20'").append(versionTo).append("'");
        }
        query.append("&$orderby=Version%20desc&$top=1");
        return query.toString();
    }

    private String calculateVersion(String knownVersion, String versionFrom) {
        if (knownVersion != null && !knownVersion.isEmpty()) return knownVersion;
        if (versionFrom != null && !versionFrom.isEmpty()) return versionFrom;
        return "0.0.1";
    }

    private boolean convertIncludePreRelease(String includePreRelease) {
        if (includePreRelease != null && includePreRelease.equals("No")) {
            return false;
        }
        return true;
    }
}
