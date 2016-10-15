package plugin.go.nuget;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;

import java.util.*;

public class PackageConfigHandler extends PluginConfigHandler {

    private ConnectionHandler connectionHandler;
    private static Logger logger = Logger.getLoggerFor(PluginConfigHandler.class);

    //TODO Build me
    private final String hardcodedQueryString = "GetUpdates()?packageIds='NUnit'&versions='3.5.0'&includePrerelease=true&includeAllVersions=true&$orderby=Version%20desc&$top=1";

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

        logger.info("We are about to make the connection " +repoUrl + hardcodedQueryString);
        NuGetFeedDocument nuGetFeedDocument = connectionHandler.getNuGetFeedDocument(repoUrl + hardcodedQueryString, username, password);
        return parsePackageDataFromDocument(nuGetFeedDocument);
    }

    private Map parsePackageDataFromDocument(NuGetFeedDocument nuGetFeedDocument) {
        logger.info("parsing the package data: "+ nuGetFeedDocument.getPackageVersion());
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
}
