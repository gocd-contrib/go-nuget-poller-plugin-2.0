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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Date;

public class NuGetFeedDocument {
    public static final String SCHEMA_ADO_DATASERVICES = "http://schemas.microsoft.com/ado/2007/08/dataservices";
    public static final String SCHEMA_ADO_DATASERVICES_METADATA = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
    private final Document xmlFeed;

    public NuGetFeedDocument(Document xmlDocument) {
        this.xmlFeed = xmlDocument;
    }

    String getPackageLocation() {
        return firstOf(xmlFeed.getElementsByTagName("content")).getAttributes().getNamedItem("src").getTextContent();
    }

    String getAuthor() {
        return xmlFeed.getElementsByTagName("name").item(0).getTextContent();
    }

    Date getPublishedDate() {
        return javax.xml.bind.DatatypeConverter.parseDateTime(getProperty(getProperties(), "Published")).getTime();
    }

    private String getProperty(NodeList properties, String name) {
        Element element = firstOf(properties);
        NodeList elementsByTagNameNS = element.getElementsByTagNameNS(SCHEMA_ADO_DATASERVICES, name);
        Node item = elementsByTagNameNS.item(0);
        return item == null ? "" : item.getTextContent();
    }

    private NodeList getProperties() {
        return xmlFeed.getElementsByTagNameNS(SCHEMA_ADO_DATASERVICES_METADATA, "properties");
    }

    String getEntryTitle() {
        NodeList titles = firstOf(getEntries()).getElementsByTagName("title");
        return titles.item(0).getTextContent();
    }

    NodeList getEntries() {
        return xmlFeed.getElementsByTagName("entry");
    }

    private Element firstOf(NodeList nodeList) {
        return ((Element) nodeList.item(0));
    }

    public String getPackageVersion() {
        return getProperty(getProperties(), "Version");
    }

    public PackageRevision getPackageRevision(boolean lastVersionKnown) {
        if (getEntries().getLength() == 0) {
            if (lastVersionKnown) return null;
            else throw new NuGetException("No such package found");
        }
        if (getEntries().getLength() > 1)
            throw new NuGetException(String.format("Multiple entries in feed for %s %s", getEntryTitle(), getPackageVersion()));
        PackageRevision result = new PackageRevision(getPackageLabel(), getPublishedDate(), getAuthor(), getReleaseNotes(), getProjectUrl());
        result.addData("LOCATION", getPackageLocation());
        result.addData("VERSION", getPackageVersion());
        return result;
    }

    private String getReleaseNotes() {
        String releaseNotes = getProperty(getProperties(), "ReleaseNotes");
        if (releaseNotes == null || releaseNotes.trim().isEmpty()) return "";
        return firstNonEmptyLine(releaseNotes);
    }

    private String firstNonEmptyLine(String s) {
        String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.trim().isEmpty())
                return line.trim();
        }
        return "";
    }

    private String getProjectUrl() {
        String projectUrl = getProperty(getProperties(), "ProjectUrl");
        if (projectUrl == null || projectUrl.trim().isEmpty()) return "";
        return firstNonEmptyLine(projectUrl);
    }

    private String getPackageLabel() {
        return getEntryTitle() + "-" + getPackageVersion();
    }
}
