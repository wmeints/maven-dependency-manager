package nl.fizzylogic.maven.dependencymanager.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SearchResultTest {

  @Test
  void testConstructor() {
    SearchResult result = new SearchResult("org.springframework", "spring-core", "6.0.0");

    assertEquals("org.springframework", result.getGroupId());
    assertEquals("spring-core", result.getArtifactId());
    assertEquals("6.0.0", result.getLatestVersion());
  }

  @Test
  void testBuilder() {
    SearchResult result =
        SearchResult.builder()
            .groupId("org.springframework")
            .artifactId("spring-core")
            .latestVersion("6.0.0")
            .build();

    assertEquals("org.springframework", result.getGroupId());
    assertEquals("spring-core", result.getArtifactId());
    assertEquals("6.0.0", result.getLatestVersion());
  }

  @Test
  void testToString() {
    SearchResult result = new SearchResult("org.springframework", "spring-core", "6.0.0");
    String str = result.toString();

    assertTrue(str.contains("org.springframework"));
    assertTrue(str.contains("spring-core"));
    assertTrue(str.contains("6.0.0"));
    assertTrue(str.contains("SearchResult"));
  }
}
