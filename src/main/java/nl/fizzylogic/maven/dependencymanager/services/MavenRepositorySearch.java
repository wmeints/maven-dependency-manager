package nl.fizzylogic.maven.dependencymanager.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.fizzylogic.maven.dependencymanager.model.SearchResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service to search for Maven dependencies using the Maven Central Search API.
 */
@ApplicationScoped
public class MavenRepositorySearch {
    
    private static final Logger LOGGER = Logger.getLogger(MavenRepositorySearch.class.getName());
    private static final String MAVEN_CENTRAL_SEARCH_URL = "https://search.maven.org/solrsearch/select";
    private static final int MAX_RESULTS = 20;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Inject
    MavenConfiguration repositoryService;
    
    public MavenRepositorySearch() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Search for dependencies using either exact groupId:artifactId format or keyword search.
     */
    public List<SearchResult> execute(String query) throws IOException, InterruptedException {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        
        String searchQuery = buildSearchQuery(query.trim());
        String searchUrl = buildSearchUrl(searchQuery);
        
        LOGGER.info("Searching with URL: " + searchUrl);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(searchUrl))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Search request failed with status: " + response.statusCode());
        }
        
        return parseSearchResponse(response.body());
    }

    /**
     * Build search query based on input format.
     * If input contains colons, treat as exact search with different formats:
     * - groupId:artifactId (exact search)
     * - groupId:artifactId:version (exact search with version)
     * Otherwise, treat as keyword search.
     */
    private String buildSearchQuery(String input) {
        if (input.contains(":")) {
            String[] parts = input.split(":");
            
            if (parts.length == 2) {
                // Exact search: groupId:artifactId
                String groupId = parts[0].trim();
                String artifactId = parts[1].trim();
                
                if (groupId.isEmpty() || artifactId.isEmpty()) {
                    throw new IllegalArgumentException("GroupId and artifactId cannot be empty");
                }
                
                return String.format("g:\"%s\" AND a:\"%s\"", groupId, artifactId);
            } else if (parts.length == 3) {
                // Exact search: groupId:artifactId:version
                String groupId = parts[0].trim();
                String artifactId = parts[1].trim();
                String version = parts[2].trim();
                
                if (groupId.isEmpty() || artifactId.isEmpty() || version.isEmpty()) {
                    throw new IllegalArgumentException("GroupId, artifactId, and version cannot be empty");
                }
                
                return String.format("g:\"%s\" AND a:\"%s\" AND v:\"%s\"", groupId, artifactId, version);
            } else {
                throw new IllegalArgumentException("Invalid format. Use 'groupId:artifactId' or 'groupId:artifactId:version' for exact search, or keywords for general search");
            }
        } else {
            // Keyword search
            return input;
        }
    }

    /**
     * Build the complete search URL with encoded parameters.
     */
    private String buildSearchUrl(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            return String.format("%s?q=%s&rows=%d&wt=json", 
                    MAVEN_CENTRAL_SEARCH_URL, encodedQuery, MAX_RESULTS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build search URL", e);
        }
    }

    /**
     * Parse JSON response from Maven Central Search API.
     */
    private List<SearchResult> parseSearchResponse(String jsonResponse) throws IOException {
        List<SearchResult> results = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode response = root.get("response");
            
            if (response == null) {
                LOGGER.warning("No 'response' field in search result");
                return results;
            }
            
            JsonNode docs = response.get("docs");
            if (docs == null || !docs.isArray()) {
                LOGGER.info("No documents found in search response");
                return results;
            }
            
            for (JsonNode doc : docs) {
                SearchResult result = parseDocument(doc);
                if (result != null) {
                    results.add(result);
                }
            }
            
        } catch (Exception e) {
            LOGGER.severe("Failed to parse search response: " + e.getMessage());
            throw new IOException("Failed to parse search response", e);
        }
        
        return results;
    }

    /**
     * Parse a single document from the search response.
     */
    private SearchResult parseDocument(JsonNode doc) {
        try {
            JsonNode groupIdNode = doc.get("g");
            JsonNode artifactIdNode = doc.get("a");
            JsonNode versionNode = doc.get("latestVersion");
            
            if (groupIdNode == null || artifactIdNode == null || versionNode == null) {
                LOGGER.warning("Missing required fields in document: " + doc);
                return null;
            }
            
            String groupId = groupIdNode.asText();
            String artifactId = artifactIdNode.asText();
            String latestVersion = versionNode.asText();
            
            return new SearchResult(groupId, artifactId, latestVersion);
            
        } catch (Exception e) {
            LOGGER.warning("Failed to parse document: " + e.getMessage());
            return null;
        }
    }
}