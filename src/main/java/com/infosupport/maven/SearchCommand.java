package com.infosupport.maven;

import com.infosupport.maven.model.SearchResult;
import com.infosupport.maven.util.TableFormatter;

import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import jakarta.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

@CommandLine.Command(
    name = "search", 
    mixinStandardHelpOptions = true, 
    description = "Search for a dependency in the configured maven repositories",
    usageHelpAutoWidth = true,
    footer = {
        "",
        "Examples:",
        "  search spring-boot                  # Keyword search",
        "  search org.springframework:spring-core  # Exact groupId:artifactId search"
    }
)
public class SearchCommand implements Runnable {
    
    private static final Logger LOGGER = Logger.getLogger(SearchCommand.class.getName());
    
    @Parameters(
        paramLabel = "QUERY", 
        description = {
            "Query string to search for dependencies.",
            "Use 'groupId:artifactId' for exact search or keywords for general search."
        }
    )
    String query;
    
    @Inject
    MavenRepositorySearch searchService;

    @Override
    public void run() {
        if (query == null || query.trim().isEmpty()) {
            System.err.println("Error: Search query is required");
            System.err.println("Use --help for usage information");
            System.exit(1);
            return;
        }
        
        try {
            System.out.println("Searching for: " + query);
            System.out.println();
            
            List<SearchResult> results = searchService.execute(query);
            
            if (results.isEmpty()) {
                System.out.println("No dependencies found for query: " + query);
                return;
            }
            
            String table = TableFormatter.formatSearchResults(results);
            System.out.println(table);
            
            System.out.println();
            System.out.printf("Found %d %s%n", 
                    results.size(), 
                    results.size() == 1 ? "dependency" : "dependencies");
            
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            System.err.println("Examples:");
            System.err.println("  search spring-boot");
            System.err.println("  search org.springframework:spring-core");
            System.exit(1);
            
        } catch (Exception e) {
            LOGGER.severe("Search failed: " + e.getMessage());
            System.err.println("Error: Failed to search for dependencies");
            System.err.println("Details: " + e.getMessage());
            
            if (e.getMessage().contains("UnknownHostException") || 
                e.getMessage().contains("ConnectException")) {
                System.err.println("Please check your internet connection and try again.");
            }
            
            System.exit(1);
        }
    }
}
