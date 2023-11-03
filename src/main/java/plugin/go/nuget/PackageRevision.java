/*
 * Copyright 2023 Thoughtworks, Inc.
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
 */

package plugin.go.nuget;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackageRevision {
    private static final Pattern DATA_KEY_PATTERN = Pattern.compile("[a-zA-Z0-9_]*");
    private static final String DATA_KEY_EMPTY_MESSAGE = "Key names cannot be null or empty.";

    private final String revision;

    private final Date timestamp;

    private final String user;

    private final String revisionComment;

    private final String trackbackUrl;

    private final Map<String, String> data;

    public PackageRevision(String revision, Date timestamp, String user, String revisionComment, String trackbackUrl) {
        this(revision, timestamp, user, revisionComment, trackbackUrl, new HashMap<>());
    }

    public PackageRevision(String revision, Date timestamp, String user, String revisionComment, String trackbackUrl, Map<String, String> data) {
        this.revision = revision;
        this.timestamp = timestamp;
        this.user = user;
        this.revisionComment = revisionComment;
        this.trackbackUrl = trackbackUrl;
        validateDataKeys(data);
        this.data = data;
    }

    private void validateDataKeys(Map<String, String> data) {
        if (data != null) {
            for (String key : data.keySet()) {
                validateDataKey(key);
            }
        }
    }

    public String getRevision() {
        return revision;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUser() {
        return user;
    }

    public String getRevisionComment() {
        return revisionComment;
    }

    public String getTrackbackUrl() {
        return trackbackUrl;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void addData(String key, String value) {
        validateDataKey(key);
        data.put(key, value);
    }

    public void validateDataKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException(DATA_KEY_EMPTY_MESSAGE);
        }
        Matcher matcher = DATA_KEY_PATTERN.matcher(key);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(dataKeyInvalidMessage(key));
        }
    }

    private String dataKeyInvalidMessage(String key) {
        return String.format("Key '%s' is invalid. Key names should consists of only alphanumeric characters and/or underscores.", key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackageRevision that = (PackageRevision) o;

        if (revision != null ? !revision.equals(that.revision) : that.revision != null) {
            return false;
        }
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) {
            return false;
        }
        return user != null ? user.equals(that.user) : that.user == null;
    }

    @Override
    public int hashCode() {
        int result = revision != null ? revision.hashCode() : 0;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PackageRevision{" +
                "revision='" + revision + '\'' +
                ", timestamp=" + timestamp +
                ", user='" + user + '\'' +
                ", revisionComment='" + revisionComment + '\'' +
                ", trackbackUrl='" + trackbackUrl + '\'' +
                ", data=" + data +
                '}';
    }

}

