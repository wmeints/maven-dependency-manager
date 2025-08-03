package nl.fizzylogic.maven.dependencymanager;

import nl.fizzylogic.maven.dependencymanager.commands.AddDependencyCommand;
import nl.fizzylogic.maven.dependencymanager.model.ResolvedDependency;
import nl.fizzylogic.maven.dependencymanager.model.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

class MavenDependencyResolverTest {

    @Mock
    private MavenRepositorySearch repositorySearch;
    
    private MavenDependencyResolver resolverService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolverService = new MavenDependencyResolver();
        resolverService.repositorySearch = repositorySearch;
    }

    @Test
    void testResolveLatestVersionWithValidDependency() throws Exception {
        // Mock search result
        SearchResult mockResult = new SearchResult("org.junit.jupiter", "junit-jupiter", "5.9.2");
        when(repositorySearch.execute("org.junit.jupiter:junit-jupiter"))
            .thenReturn(Arrays.asList(mockResult));
        
        String version = resolverService.resolveLatestVersion("org.junit.jupiter", "junit-jupiter");
        
        assertNotNull(version);
        assertEquals("5.9.2", version);
        verify(repositorySearch).execute("org.junit.jupiter:junit-jupiter");
    }

    @Test
    void testResolveLatestVersionWithInvalidDependency() throws Exception {
        // Mock empty search result
        when(repositorySearch.execute("invalid.group:invalid-artifact"))
            .thenReturn(Collections.emptyList());
        
        String version = resolverService.resolveLatestVersion("invalid.group", "invalid-artifact");
        
        assertNull(version);
        verify(repositorySearch).execute("invalid.group:invalid-artifact");
    }

    @Test
    void testDependencyExistsWithValidVersion() throws Exception {
        // Mock search result for existing version
        SearchResult mockResult = new SearchResult("org.junit.jupiter", "junit-jupiter", "5.8.2");
        when(repositorySearch.execute("org.junit.jupiter:junit-jupiter:5.8.2"))
            .thenReturn(Arrays.asList(mockResult));
        
        boolean exists = resolverService.dependencyExists("org.junit.jupiter", "junit-jupiter", "5.8.2");
        
        assertTrue(exists);
        verify(repositorySearch).execute("org.junit.jupiter:junit-jupiter:5.8.2");
    }

    @Test
    void testDependencyExistsWithInvalidVersion() throws Exception {
        // Mock empty search result for non-existing version
        when(repositorySearch.execute("org.junit.jupiter:junit-jupiter:99.99.99"))
            .thenReturn(Collections.emptyList());
        
        boolean exists = resolverService.dependencyExists("org.junit.jupiter", "junit-jupiter", "99.99.99");
        
        assertFalse(exists);
        verify(repositorySearch).execute("org.junit.jupiter:junit-jupiter:99.99.99");
    }

    @Test
    void testResolveDependencyWithVersion() throws Exception {
        // Mock search result for existing version
        SearchResult mockResult = new SearchResult("org.junit.jupiter", "junit-jupiter", "5.8.2");
        when(repositorySearch.execute("org.junit.jupiter:junit-jupiter:5.8.2"))
            .thenReturn(Arrays.asList(mockResult));
        
        AddDependencyCommand.DependencyCoordinates coordinates = 
            new AddDependencyCommand.DependencyCoordinates("org.junit.jupiter", "junit-jupiter", "5.8.2");
        
        ResolvedDependency resolved = 
            resolverService.resolveDependency(coordinates);
        
        assertNotNull(resolved);
        assertEquals("org.junit.jupiter", resolved.getGroupId());
        assertEquals("junit-jupiter", resolved.getArtifactId());
        assertEquals("5.8.2", resolved.getVersion());
    }

    @Test
    void testResolveDependencyWithoutVersion() throws Exception {
        // Mock search result for latest version
        SearchResult mockResult = new SearchResult("org.junit.jupiter", "junit-jupiter", "5.9.2");
        when(repositorySearch.execute("org.junit.jupiter:junit-jupiter"))
            .thenReturn(Arrays.asList(mockResult));
        
        AddDependencyCommand.DependencyCoordinates coordinates = 
            new AddDependencyCommand.DependencyCoordinates("org.junit.jupiter", "junit-jupiter", null);
        
        ResolvedDependency resolved = 
            resolverService.resolveDependency(coordinates);
        
        assertNotNull(resolved);
        assertEquals("org.junit.jupiter", resolved.getGroupId());
        assertEquals("junit-jupiter", resolved.getArtifactId());
        assertEquals("5.9.2", resolved.getVersion());
    }

    @Test
    void testResolvedDependencyToString() {
        ResolvedDependency resolved = 
            new ResolvedDependency("org.springframework", "spring-core", "5.3.21");
        
        assertEquals("org.springframework:spring-core:5.3.21", resolved.toString());
    }
}