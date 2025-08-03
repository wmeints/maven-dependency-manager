package com.infosupport.maven.util;

import com.infosupport.maven.model.SearchResult;

import java.util.List;

/**
 * Utility class to format search results into a table format.
 */
public class TableFormatter {
    
    private static final String COLUMN_SEPARATOR = " | ";
    private static final String HEADER_SEPARATOR = "-";
    
    /**
     * Format search results into a table with headers.
     */
    public static String formatSearchResults(List<SearchResult> results) {
        if (results == null || results.isEmpty()) {
            return "No dependencies found.";
        }
        
        // Calculate column widths
        int groupIdWidth = Math.max("GroupId".length(), 
                results.stream().mapToInt(r -> r.getGroupId().length()).max().orElse(0));
        int artifactIdWidth = Math.max("ArtifactId".length(), 
                results.stream().mapToInt(r -> r.getArtifactId().length()).max().orElse(0));
        int versionWidth = Math.max("Latest version".length(), 
                results.stream().mapToInt(r -> r.getLatestVersion().length()).max().orElse(0));
        
        StringBuilder table = new StringBuilder();
        
        // Header row
        table.append(formatRow("GroupId", "ArtifactId", "Latest version", 
                groupIdWidth, artifactIdWidth, versionWidth));
        table.append("\n");
        
        // Separator row
        table.append(formatSeparatorRow(groupIdWidth, artifactIdWidth, versionWidth));
        table.append("\n");
        
        // Data rows
        for (SearchResult result : results) {
            table.append(formatRow(result.getGroupId(), result.getArtifactId(), result.getLatestVersion(),
                    groupIdWidth, artifactIdWidth, versionWidth));
            table.append("\n");
        }
        
        return table.toString().trim();
    }
    
    /**
     * Format a single row with proper column alignment.
     */
    private static String formatRow(String groupId, String artifactId, String version,
                                   int groupIdWidth, int artifactIdWidth, int versionWidth) {
        return String.format("| %-" + groupIdWidth + "s" + COLUMN_SEPARATOR + 
                           "%-" + artifactIdWidth + "s" + COLUMN_SEPARATOR + 
                           "%-" + versionWidth + "s |",
                           groupId, artifactId, version);
    }
    
    /**
     * Format the separator row with dashes.
     */
    private static String formatSeparatorRow(int groupIdWidth, int artifactIdWidth, int versionWidth) {
        return String.format("| %s" + COLUMN_SEPARATOR + "%s" + COLUMN_SEPARATOR + "%s |",
                repeat(HEADER_SEPARATOR, groupIdWidth),
                repeat(HEADER_SEPARATOR, artifactIdWidth),
                repeat(HEADER_SEPARATOR, versionWidth));
    }
    
    /**
     * Repeat a string n times.
     */
    private static String repeat(String str, int times) {
        return str.repeat(Math.max(0, times));
    }
}