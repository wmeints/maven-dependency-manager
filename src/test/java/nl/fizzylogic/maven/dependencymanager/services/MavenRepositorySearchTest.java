package nl.fizzylogic.maven.dependencymanager.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MavenRepositorySearchTest {

    @Mock
    MavenConfiguration repositoryService;

    @InjectMocks
    MavenRepositorySearch searchService;

    @BeforeEach
    void setUp() {
        searchService = new MavenRepositorySearch();
    }

    @Test
    void testSearchWithEmptyQuery() {
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.execute("");
        });
    }

    @Test
    void testSearchWithNullQuery() {
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.execute(null);
        });
    }

    @Test
    void testSearchWithWhitespaceQuery() {
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.execute("   ");
        });
    }

    // Note: Integration tests for actual API calls would require
    // network connectivity and should be run separately
}