package nl.fizzylogic.maven.dependencymanager.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MavenConfigurationTest {

  private MavenConfiguration mavenConfiguration;

  @BeforeEach
  void setUp() {
    mavenConfiguration = new MavenConfiguration();
  }

  @Test
  void testGetRepositoryUrlsIncludesMavenCentral() {
    List<String> repositories = mavenConfiguration.getRepositoryUrls();

    assertNotNull(repositories);
    assertFalse(repositories.isEmpty());
    assertTrue(repositories.contains("https://repo1.maven.org/maven2"));
  }

  @Test
  void testGetRepositoryUrlsNotNull() {
    List<String> repositories = mavenConfiguration.getRepositoryUrls();

    assertNotNull(repositories);
    // Should at least contain Maven Central
    assertTrue(repositories.size() >= 1);
  }
}
