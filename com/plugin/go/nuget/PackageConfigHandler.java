package plugin.go.nuget;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;

import java.text.SimpleDateFormat;
import java.util.*;

import static utils.Constants.PACKAGE_CONFIGURATION;
import static utils.Constants.REPOSITORY_CONFIGURATION;

public class PackageConfigHandler extends PluginConfigHandler {

    private ConnectionHandler connectionHandler;
    private static Logger logger = Logger.getLoggerFor(PluginConfigHandler.class);

    public PackageConfigHandler(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public Map handlePackageConfiguration() {
        Map packageConfig = new HashMap();

        packageConfig.put("PACKAGE_ID", createConfigurationField("Package ID", "0", false, true, true));

        return packageConfig;
    }

    public List handleValidatePackageConfiguration(Map request) {
        List validationList = new ArrayList();
        Map configMap = (Map) request.get("package-configuration");
        Map packageIDMap = (Map) configMap.get("PACKAGE_ID");

        if (packageIDMap.get("value").equals("")) {
            Map idErrors = new HashMap();
            idErrors.put("key", "PACKAGE_ID");
            idErrors.put("message", "Package ID cannot be empty");
            validationList.add(idErrors);
        }

        return validationList;
    }

    public Map handleCheckPackageConnection(Map requestBodyMap) {
        Map response = new HashMap();
        List messages = new ArrayList();
        Map packageRevisionResponse;

        try {
            packageRevisionResponse = handleLatestRevision(requestBodyMap);
            String revision = (String) packageRevisionResponse.get("revision");
            if(revision!=null){
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
        String knownPackageRevision = "0.0.1";
        return pollForRevision(request, knownPackageRevision, false);
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

        NuGetFeedDocument nuGetFeedDocument = connectionHandler.getNuGetFeedDocument(repoUrl, getQuery(packageId, knownPackageRevision), username, password);
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

    private String getQuery(String packageId, String knownVersion) {
        StringBuilder query = new StringBuilder();
        query.append("/GetUpdates()?");
        query.append(String.format("packageIds='%s'", packageId));
        query.append(String.format("&versions='%s'", knownVersion));
        query.append("&includePrerelease=").append(true);
        query.append("&includeAllVersions=true");//has to be true, filter gets applied later
//        if (upperBoundGiven()) {
//            query.append("&$filter=Version%20lt%20'").append(pollVersionTo).append("'");
//        }
        query.append("&$orderby=Version%20desc&$top=1");
        return query.toString();
    }
}
