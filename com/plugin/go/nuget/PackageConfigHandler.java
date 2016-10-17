package plugin.go.nuget;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map handleLatestRevision(Map request) {
        // Use the Connection Handler to get the collection of data
        Map configMap = (Map) request.get("repository-configuration");

        String repoUrl = parseValueFromEmbeddedMap(configMap, "REPOSITORY_URL");
        String username = parseValueFromEmbeddedMap(configMap, "USERNAME");
        String password = parseValueFromEmbeddedMap(configMap, "PASSWORD");

        NuGetFeedDocument nuGetFeedDocument = connectionHandler.getNuGetFeedDocument(repoUrl, getQuery(), username, password);
        return parsePackageDataFromDocument(nuGetFeedDocument);
    }

    private Map parsePackageDataFromDocument(NuGetFeedDocument nuGetFeedDocument) {
        //TODO handle this properly...
        Map packageRevisionMap = new HashMap();
        if(nuGetFeedDocument == null) {
            return packageRevisionMap;
        }
        PackageRevision packageRevision = nuGetFeedDocument.getPackageRevision(false);
        packageRevisionMap.put("revision", packageRevision.getRevision());
        packageRevisionMap.put("timestamp", packageRevision.getTimestamp());
        packageRevisionMap.put("user", packageRevision.getUser());
        packageRevisionMap.put("revisionComment", packageRevision.getRevisionComment());
        packageRevisionMap.put("trackbackUrl", packageRevision.getTrackbackUrl());
        packageRevisionMap.put("data", packageRevision.getData());

        return packageRevisionMap;
    }

    private String parseValueFromEmbeddedMap(Map configMap, String fieldName) {
        if (configMap.get(fieldName) == null) return "";

        Map fieldMap = (Map) configMap.get(fieldName);
        String value = (String) fieldMap.get("value");
        return value;
    }

    private String getQuery() {
        StringBuilder query = new StringBuilder();
        query.append("/GetUpdates()?");
        query.append(String.format("packageIds='%s'", "NUnit"));
        query.append(String.format("&versions='%s'", "0.0.1"));
        query.append("&includePrerelease=").append(true);
        query.append("&includeAllVersions=true");//has to be true, filter gets applied later
//        if (upperBoundGiven()) {
//            query.append("&$filter=Version%20lt%20'").append(pollVersionTo).append("'");
//        }
        query.append("&$orderby=Version%20desc&$top=1");
        return query.toString();
    }
}
