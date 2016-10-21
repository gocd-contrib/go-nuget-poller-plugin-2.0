package plugin.go.nuget;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;

import java.text.SimpleDateFormat;
import java.util.*;

import static utils.Constants.PACKAGE_CONFIGURATION;
import static utils.Constants.REPOSITORY_CONFIGURATION;

public class PackagePoller {
    private NugetQueryBuilder queryBuilder;
    private ConnectionHandler connectionHandler;

    private static Logger logger = Logger.getLoggerFor(PluginConfigHandler.class);

    public PackagePoller(ConnectionHandler connectionHandler, NugetQueryBuilder queryBuilder) {
        this.connectionHandler = connectionHandler;
        this.queryBuilder = queryBuilder;
    }

    public Map handleCheckPackageConnection(Map requestBodyMap) {
        Map response = new HashMap();
        List messages = new ArrayList();
        Map packageRevisionResponse;

        try {
            packageRevisionResponse = handleLatestRevision(requestBodyMap);
            String revision = (String) packageRevisionResponse.get("revision");
            if (revision != null) {
                messages.add("Successfully found revision: " + revision);
                response.put("status", "success");
                response.put("messages", messages);
                return response;
            }
        } catch (RuntimeException e) {
            logger.info(e.getMessage());
        }

        messages.add("No packages found");
        response.put("status", "failure");
        response.put("messages", messages);
        return response;
    }

    public Map handleLatestRevision(Map request) {
        return pollForRevision(request, "", false);
    }

    public Map handleLatestRevisionSince(Map request) {
        Map revisionMap = (Map) request.get("previous-revision");
        Map data = (Map) revisionMap.get("data");
        String previousVersion = (String) data.get("VERSION");

        return pollForRevision(request, previousVersion, true);
    }

    private Map pollForRevision(Map request, String knownPackageRevision, boolean lastVersionKnown) {
        // Use the Connection Handler to get the collection of data
        Map repoConfigMap = (Map) request.get(REPOSITORY_CONFIGURATION);

        String repoUrl = parseValueFromEmbeddedMap(repoConfigMap, "REPOSITORY_URL");
        String username = parseValueFromEmbeddedMap(repoConfigMap, "USERNAME");
        String password = parseValueFromEmbeddedMap(repoConfigMap, "PASSWORD");

        Map packageConfigMap = (Map) request.get(PACKAGE_CONFIGURATION);
        String packageId = parseValueFromEmbeddedMap(packageConfigMap, "PACKAGE_ID");
        String versionTo = parseValueFromEmbeddedMap(packageConfigMap, "POLL_VERSION_TO");
        String versionFrom = parseValueFromEmbeddedMap(packageConfigMap, "POLL_VERSION_FROM");
        String includePreRelease = parseValueFromEmbeddedMap(packageConfigMap, "INCLUDE_PRE_RELEASE");
        includePreRelease = (includePreRelease.isEmpty()) ? "yes" : includePreRelease;

        String optionsForFeed = queryBuilder.getQuery(packageId, knownPackageRevision, versionFrom, versionTo, includePreRelease);

        NuGetFeedDocument nuGetFeedDocument = connectionHandler.getNuGetFeedDocument(repoUrl, optionsForFeed, username, password);
        return parsePackageDataFromDocument(nuGetFeedDocument, lastVersionKnown);
    }

    private Map parsePackageDataFromDocument(NuGetFeedDocument nuGetFeedDocument, boolean lastVersionKnown) {
        Map packageRevisionMap = new HashMap();
        if (nuGetFeedDocument == null || nuGetFeedDocument.getPackageRevision(lastVersionKnown) == null) {
            return packageRevisionMap;
        }
        PackageRevision packageRevision = nuGetFeedDocument.getPackageRevision(lastVersionKnown);
        packageRevisionMap.put("revision", packageRevision.getRevision());
        packageRevisionMap.put("timestamp", formatTimestamp(packageRevision.getTimestamp()));
        packageRevisionMap.put("user", packageRevision.getUser());
        packageRevisionMap.put("revisionComment", packageRevision.getRevisionComment());
        packageRevisionMap.put("trackbackUrl", packageRevision.getTrackbackUrl());
        packageRevisionMap.put("data", packageRevision.getData());

        return packageRevisionMap;
    }

    private String formatTimestamp(Date timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String date = dateFormat.format(timestamp);
        return date;
    }

    private String parseValueFromEmbeddedMap(Map configMap, String fieldName) {
        if (configMap.get(fieldName) == null) return "";

        Map fieldMap = (Map) configMap.get(fieldName);
        String value = (String) fieldMap.get("value");
        return value;
    }

}
