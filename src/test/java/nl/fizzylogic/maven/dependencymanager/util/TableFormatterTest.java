package nl.fizzylogic.maven.dependencymanager.util;

import nl.fizzylogic.maven.dependencymanager.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableFormatterTest {

    @Test
    void testFormatEmptyResults() {
        List<SearchResult> emptyResults = Collections.emptyList();
        String result = TableFormatter.formatSearchResults(emptyResults);
        assertEquals("No dependencies found.", result);
    }

    @Test
    void testFormatNullResults() {
        String result = TableFormatter.formatSearchResults(null);
        assertEquals("No dependencies found.", result);
    }

    @Test
    void testFormatSingleResult() {
        SearchResult result = new SearchResult("org.springframework", "spring-core", "6.0.0");
        List<SearchResult> results = Collections.singletonList(result);
        
        String formatted = TableFormatter.formatSearchResults(results);
        
        assertNotNull(formatted);
        assertTrue(formatted.contains("GroupId"));
        assertTrue(formatted.contains("ArtifactId"));
        assertTrue(formatted.contains("Latest version"));
        assertTrue(formatted.contains("org.springframework"));
        assertTrue(formatted.contains("spring-core"));
        assertTrue(formatted.contains("6.0.0"));
    }

    @Test
    void testFormatMultipleResults() {
        List<SearchResult> results = Arrays.asList(
            new SearchResult("org.springframework", "spring-core", "6.0.0"),
            new SearchResult("org.springframework", "spring-boot", "3.0.0"),
            new SearchResult("com.fasterxml.jackson.core", "jackson-core", "2.15.0")
        );
        
        String formatted = TableFormatter.formatSearchResults(results);
        
        assertNotNull(formatted);
        assertTrue(formatted.contains("org.springframework"));
        assertTrue(formatted.contains("spring-core"));
        assertTrue(formatted.contains("spring-boot"));
        assertTrue(formatted.contains("com.fasterxml.jackson.core"));
        assertTrue(formatted.contains("jackson-core"));
        
        // Check that table structure is present
        assertTrue(formatted.contains("|"));
        assertTrue(formatted.contains("-"));
    }

    @Test
    void testFormatHandlesVariableColumnWidths() {
        List<SearchResult> results = Arrays.asList(
            new SearchResult("a", "b", "1.0"),
            new SearchResult("very.long.group.id.name", "very-long-artifact-id-name", "1.0.0-SNAPSHOT")
        );
        
        String formatted = TableFormatter.formatSearchResults(results);
        
        assertNotNull(formatted);
        // The table should accommodate the longest values in each column
        assertTrue(formatted.contains("very.long.group.id.name"));
        assertTrue(formatted.contains("very-long-artifact-id-name"));
        assertTrue(formatted.contains("1.0.0-SNAPSHOT"));
    }
}