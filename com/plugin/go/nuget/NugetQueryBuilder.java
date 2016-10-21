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
