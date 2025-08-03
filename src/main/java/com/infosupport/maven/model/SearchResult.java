package com.infosupport.maven.model;

/**
 * Represents a search result for a Maven dependency.
 */
public class SearchResult {
    private final String groupId;
    private final String artifactId;
    private final String latestVersion;

    public SearchResult(String groupId, String artifactId, String latestVersion) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.latestVersion = latestVersion;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    @Override
    public String toString() {
        return String.format("SearchResult{groupId='%s', artifactId='%s', latestVersion='%s'}", 
                groupId, artifactId, latestVersion);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String groupId;
        private String artifactId;
        private String latestVersion;

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder artifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder latestVersion(String latestVersion) {
            this.latestVersion = latestVersion;
            return this;
        }

        public SearchResult build() {
            return new SearchResult(groupId, artifactId, latestVersion);
        }
    }
}