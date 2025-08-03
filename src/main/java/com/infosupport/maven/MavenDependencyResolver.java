package com.infosupport.maven;

import com.infosupport.maven.model.SearchResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service for resolving Maven dependencies and versions using Maven Central Search API.
 */
@ApplicationScoped
public class MavenDependencyResolver {
    
    private static final Logger LOGGER = Logger.getLogger(MavenDependencyResolver.class.getName());
    
    @Inject
    MavenRepositorySearch repositorySearch;

    /**
     * Resolve the latest version of a dependency.
     * 
     * @param groupId The dependency group ID
     * @param artifactId The dependency artifact ID
     * @return The latest version, or null if not found
     */
    public String resolveLatestVersion(String groupId, String artifactId) {
        try {
            // Use the search API to find the dependency
            String searchQuery = groupId + ":" + artifactId;
            List<SearchResult> results = repositorySearch.execute(searchQuery);
            
            if (results.isEmpty()) {
                LOGGER.warning("No versions found for " + groupId + ":" + artifactId);
                return null;
            }
            
            // The search API returns the latest version in the SearchResult
            SearchResult result = results.get(0);
            String latestVersion = result.getLatestVersion();
            
            LOGGER.info("Resolved latest version for " + groupId + ":" + artifactId + " = " + latestVersion);
            return latestVersion;
            
        } catch (Exception e) {
            LOGGER.severe("Failed to resolve latest version for " + groupId + ":" + artifactId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if a specific version of a dependency exists in the repositories.
     * 
     * @param groupId The dependency group ID
     * @param artifactId The dependency artifact ID
     * @param version The version to check
     * @return true if the dependency exists, false otherwise
     */
    public boolean dependencyExists(String groupId, String artifactId, String version) {
        try {
            // Use the search API to find the specific version
            String searchQuery = groupId + ":" + artifactId + ":" + version;
            List<SearchResult> results = repositorySearch.execute(searchQuery);
            
            // If we get results, the version exists
            boolean exists = !results.isEmpty();
            
            LOGGER.info("Dependency " + groupId + ":" + artifactId + ":" + version + 
                       (exists ? " exists" : " does not exist"));
            return exists;
            
        } catch (Exception e) {
            LOGGER.warning("Failed to check existence of " + groupId + ":" + artifactId + ":" + version + 
                          ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Resolve a dependency to a specific version or latest if version is null.
     * 
     * @param coordinates The dependency coordinates
     * @return ResolvedDependency with the resolved version, or null if resolution failed
     */
    public ResolvedDependency resolveDependency(AddDependencyCommand.DependencyCoordinates coordinates) {
        String groupId = coordinates.getGroupId();
        String artifactId = coordinates.getArtifactId();
        String version = coordinates.getVersion();
        
        if (version != null) {
            // Check if specific version exists
            if (dependencyExists(groupId, artifactId, version)) {
                return new ResolvedDependency(groupId, artifactId, version);
            } else {
                LOGGER.warning("Specified version " + version + " not found for " + groupId + ":" + artifactId);
                return null;
            }
        } else {
            // Resolve latest version
            String latestVersion = resolveLatestVersion(groupId, artifactId);
            if (latestVersion != null) {
                return new ResolvedDependency(groupId, artifactId, latestVersion);
            } else {
                LOGGER.warning("Could not resolve latest version for " + groupId + ":" + artifactId);
                return null;
            }
        }
    }

    /**
     * Value class representing a resolved dependency with its version.
     */
    public static class ResolvedDependency {
        private final String groupId;
        private final String artifactId;
        private final String version;

        public ResolvedDependency(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            return groupId + ":" + artifactId + ":" + version;
        }
    }
}