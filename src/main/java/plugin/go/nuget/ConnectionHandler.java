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

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.util.HttpRepoURL;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionHandler {
    private static Logger logger = Logger.getLoggerFor(ConnectionHandler.class);

    public Map checkConnectionToUrlWithMetadata(String url, String username, String password) {
        HttpRepoURL repoConnection = new HttpRepoURL(metadataUrl(url), username, password);
        try {
            repoConnection.checkConnection();
        } catch (Exception e) {
            Map responseMap = formatConnectionResponse("failure", "Unsuccessful Connection");
            return responseMap;
        }

        Map responseMap = formatConnectionResponse("success", "Successfully connected to repository url provided");
        return responseMap;
    }

    public NuGetFeedDocument getNuGetFeedDocument(String baseUrl, String queryParams, String username, String password) {
        Map repoConnectionResponseMap = checkConnectionToUrlWithMetadata(baseUrl, username, password);
        if (!repoConnectionSuccessful(repoConnectionResponseMap)) {
            return null;
        }
        try {
            Document xmlDocument = new HttpRepoURL(baseUrl, username, password).download(baseUrl + queryParams);
            logger.info("Package information is \n" + convertDocumentToString(xmlDocument));
            return new NuGetFeedDocument(xmlDocument);
        } catch (RuntimeException e) {
            logger.info(e.getMessage());
        }
        return null;
    }

    private Map formatConnectionResponse(String status, String message) {
        Map responseMap = new HashMap();
        responseMap.put("status", status);
        List messages = Arrays.asList(message);
        responseMap.put("messages", messages);
        return responseMap;
    }

    private boolean repoConnectionSuccessful(Map repoConnectionResponseMap) {
        if (repoConnectionResponseMap.get("status") == "failure") {
            return false;
        } else return true;
    }

    // We use $metada because nuget uses the ODATA format.
    // This distinguishes nuget feeds from generic sites (though it would accept as valid a non-nuget feed ODATA site)
    private String metadataUrl(String url) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url + "$metadata";
    }

    private static String convertDocumentToString(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            // below code to remove XML declaration
            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            return output;
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return null;
    }
}
