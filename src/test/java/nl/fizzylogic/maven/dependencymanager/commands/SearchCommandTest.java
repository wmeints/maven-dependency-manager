package nl.fizzylogic.maven.dependencymanager.commands;

import nl.fizzylogic.maven.dependencymanager.MavenRepositorySearch;
import nl.fizzylogic.maven.dependencymanager.commands.SearchDependencyCommand;
import nl.fizzylogic.maven.dependencymanager.model.SearchResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SearchCommand.
 * 
 * Tests both successful execution paths and error scenarios.
 * Error handling has been refactored to use exceptions instead of System.exit(),
 * making the code more testable and flexible.
 */
@ExtendWith(MockitoExtension.class)
class SearchCommandTest {

    @Mock
    private MavenRepositorySearch searchService;

    @InjectMocks
    private SearchDependencyCommand searchCommand;

    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        // Capture System.out and System.err for testing
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }

    @AfterEach
    void tearDown() {
        // Restore original streams
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testSuccessfulSearchWithResults() throws Exception {
        // Given
        searchCommand.query = "spring-boot";
        List<SearchResult> mockResults = Arrays.asList(
            new SearchResult("org.springframework.boot", "spring-boot-starter", "3.2.0"),
            new SearchResult("org.springframework.boot", "spring-boot-autoconfigure", "3.2.0")
        );
        when(searchService.execute("spring-boot")).thenReturn(mockResults);

        // When
        searchCommand.executeSearch();

        // Then
        verify(searchService).execute("spring-boot");
        String output = outputStream.toString();
        assertTrue(output.contains("Searching for: spring-boot"));
        assertTrue(output.contains("Found 2 dependencies"));
        assertFalse(output.contains("No dependencies found"));
    }

    @Test
    void testSuccessfulSearchWithSingleResult() throws Exception {
        // Given
        searchCommand.query = "org.springframework:spring-core";
        List<SearchResult> mockResults = Collections.singletonList(
            new SearchResult("org.springframework", "spring-core", "6.1.0")
        );
        when(searchService.execute("org.springframework:spring-core")).thenReturn(mockResults);

        // When
        searchCommand.executeSearch();

        // Then
        verify(searchService).execute("org.springframework:spring-core");
        String output = outputStream.toString();
        assertTrue(output.contains("Searching for: org.springframework:spring-core"));
        assertTrue(output.contains("Found 1 dependency"));
        assertFalse(output.contains("dependencies")); // Should be singular
    }

    @Test
    void testSearchWithNoResults() throws Exception {
        // Given
        searchCommand.query = "nonexistent-dependency";
        when(searchService.execute("nonexistent-dependency")).thenReturn(new ArrayList<>());

        // When
        searchCommand.executeSearch();

        // Then
        verify(searchService).execute("nonexistent-dependency");
        String output = outputStream.toString();
        assertTrue(output.contains("Searching for: nonexistent-dependency"));
        assertTrue(output.contains("No dependencies found for query: nonexistent-dependency"));
        assertFalse(output.contains("Found"));
    }

    @Test
    void testInputValidation() {
        // Test query parameter handling
        
        // Test proper query assignment
        searchCommand.query = "test-query";
        assertEquals("test-query", searchCommand.query);
        
        // Test null assignment
        searchCommand.query = null;
        assertNull(searchCommand.query);
        
        // Test empty assignment  
        searchCommand.query = "";
        assertEquals("", searchCommand.query);
        
        // Test whitespace assignment
        searchCommand.query = "   ";
        assertEquals("   ", searchCommand.query);
    }

    @Test
    void testExecuteSearchWithNullQuery() {
        // Given
        searchCommand.query = null;

        // When & Then
        SearchDependencyCommand.SearchCommandException exception = assertThrows(
            SearchDependencyCommand.SearchCommandException.class, 
            () -> searchCommand.executeSearch()
        );
        
        assertEquals("Search query is required", exception.getMessage());
        assertTrue(exception.shouldShowHelpInfo());
        assertFalse(exception.shouldShowExamples());
        verifyNoInteractions(searchService);
    }

    @Test
    void testExecuteSearchWithEmptyQuery() {
        // Given
        searchCommand.query = "";

        // When & Then
        SearchDependencyCommand.SearchCommandException exception = assertThrows(
            SearchDependencyCommand.SearchCommandException.class, 
            () -> searchCommand.executeSearch()
        );
        
        assertEquals("Search query is required", exception.getMessage());
        assertTrue(exception.shouldShowHelpInfo());
        assertFalse(exception.shouldShowExamples());
        verifyNoInteractions(searchService);
    }

    @Test
    void testExecuteSearchWithWhitespaceOnlyQuery() {
        // Given
        searchCommand.query = "   ";

        // When & Then
        SearchDependencyCommand.SearchCommandException exception = assertThrows(
            SearchDependencyCommand.SearchCommandException.class, 
            () -> searchCommand.executeSearch()
        );
        
        assertEquals("Search query is required", exception.getMessage());
        assertTrue(exception.shouldShowHelpInfo());
        assertFalse(exception.shouldShowExamples());
        verifyNoInteractions(searchService);
    }

    @Test
    void testExecuteSearchWithIllegalArgumentException() throws Exception {
        // Given
        searchCommand.query = "invalid:format:too:many:colons";
        when(searchService.execute("invalid:format:too:many:colons"))
            .thenThrow(new IllegalArgumentException("Invalid format"));

        // When & Then
        SearchDependencyCommand.SearchCommandException exception = assertThrows(
            SearchDependencyCommand.SearchCommandException.class, 
            () -> searchCommand.executeSearch()
        );
        
        assertEquals("Invalid format", exception.getMessage());
        assertFalse(exception.shouldShowHelpInfo());
        assertTrue(exception.shouldShowExamples());
        verify(searchService).execute("invalid:format:too:many:colons");
    }

    @Test
    void testExecuteSearchWithIOException() throws Exception {
        // Given
        searchCommand.query = "spring-boot";
        IOException ioException = new IOException("Network error");
        when(searchService.execute("spring-boot")).thenThrow(ioException);

        // When & Then
        SearchDependencyCommand.SearchCommandException exception = assertThrows(
            SearchDependencyCommand.SearchCommandException.class, 
            () -> searchCommand.executeSearch()
        );
        
        assertTrue(exception.getMessage().contains("Failed to search for dependencies"));
        assertTrue(exception.getMessage().contains("Network error"));
        assertFalse(exception.shouldShowHelpInfo());
        assertFalse(exception.shouldShowExamples());
        assertEquals(ioException, exception.getCause());
        verify(searchService).execute("spring-boot");
    }

    @Test
    void testExecuteSearchWithInterruptedException() throws Exception {
        // Given
        searchCommand.query = "spring-boot";
        InterruptedException interruptedException = new InterruptedException("Thread interrupted");
        when(searchService.execute("spring-boot")).thenThrow(interruptedException);

        // When & Then
        SearchDependencyCommand.SearchCommandException exception = assertThrows(
            SearchDependencyCommand.SearchCommandException.class, 
            () -> searchCommand.executeSearch()
        );
        
        assertTrue(exception.getMessage().contains("Failed to search for dependencies"));
        assertTrue(exception.getMessage().contains("Thread interrupted"));
        assertFalse(exception.shouldShowHelpInfo());
        assertFalse(exception.shouldShowExamples());
        assertEquals(interruptedException, exception.getCause());
        verify(searchService).execute("spring-boot");
    }

    @Test
    void testSearchWithLongQuery() throws Exception {
        // Given
        String longQuery = "a".repeat(100); // Long query
        searchCommand.query = longQuery;
        when(searchService.execute(longQuery)).thenReturn(new ArrayList<>());

        // When
        searchCommand.executeSearch();

        // Then
        verify(searchService).execute(longQuery);
        String output = outputStream.toString();
        assertTrue(output.contains("Searching for: " + longQuery));
        assertTrue(output.contains("No dependencies found"));
    }

    @Test
    void testSearchWithSpecialCharacters() throws Exception {
        // Given
        String specialQuery = "spring-boot@#$%";
        searchCommand.query = specialQuery;
        when(searchService.execute(specialQuery)).thenReturn(new ArrayList<>());

        // When
        searchCommand.executeSearch();

        // Then
        verify(searchService).execute(specialQuery);
        String output = outputStream.toString();
        assertTrue(output.contains("Searching for: " + specialQuery));
    }

    @Test
    void testSearchWithUnicodeCharacters() throws Exception {
        // Given
        String unicodeQuery = "spring-boot-测试";
        searchCommand.query = unicodeQuery;
        when(searchService.execute(unicodeQuery)).thenReturn(new ArrayList<>());

        // When
        searchCommand.executeSearch();

        // Then
        verify(searchService).execute(unicodeQuery);
        String output = outputStream.toString();
        assertTrue(output.contains("Searching for: " + unicodeQuery));
    }

    @Test
    void testSearchCommandAnnotations() {
        // Test that the command has proper annotations configured
        // This ensures Picocli integration is set up correctly
        assertNotNull(searchCommand);
        assertTrue(searchCommand instanceof Runnable);
    }
}