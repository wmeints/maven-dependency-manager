package nl.fizzylogic.maven.dependencymanager;

import nl.fizzylogic.maven.dependencymanager.model.SearchResult;
import nl.fizzylogic.maven.dependencymanager.util.TableFormatter;

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
        try {
            executeSearch();
        } catch (SearchCommandException e) {
            // Print error message and examples if applicable
            System.err.println("Error: " + e.getMessage());
            if (e.shouldShowExamples()) {
                System.err.println();
                System.err.println("Examples:");
                System.err.println("  search spring-boot");
                System.err.println("  search org.springframework:spring-core");
            }
            if (e.shouldShowHelpInfo()) {
                System.err.println("Use --help for usage information");
            }
            // Throw the exception to allow Picocli to handle it appropriately
            throw new CommandLine.ExecutionException(new CommandLine(SearchCommand.class), e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.severe("Search failed: " + e.getMessage());
            System.err.println("Error: Failed to search for dependencies");
            System.err.println("Details: " + e.getMessage());
            
            if (e.getMessage() != null && (e.getMessage().contains("UnknownHostException") || 
                e.getMessage().contains("ConnectException"))) {
                System.err.println("Please check your internet connection and try again.");
            }
            
            // Throw the exception to allow Picocli to handle it appropriately
            throw new CommandLine.ExecutionException(new CommandLine(SearchCommand.class), "Search failed", e);
        }
    }
    
    /**
     * Executes the search logic, separated for better testability.
     */
    void executeSearch() throws SearchCommandException {
        if (query == null || query.trim().isEmpty()) {
            throw new SearchCommandException("Search query is required", true, false);
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
            throw new SearchCommandException(e.getMessage(), false, true);
        } catch (Exception e) {
            throw new SearchCommandException("Failed to search for dependencies: " + e.getMessage(), false, false, e);
        }
    }
    
    /**
     * Custom exception for SearchCommand errors with context about what additional
     * information should be shown to the user.
     */
    public static class SearchCommandException extends Exception {
        private final boolean showHelpInfo;
        private final boolean showExamples;
        
        public SearchCommandException(String message, boolean showHelpInfo, boolean showExamples) {
            super(message);
            this.showHelpInfo = showHelpInfo;
            this.showExamples = showExamples;
        }
        
        public SearchCommandException(String message, boolean showHelpInfo, boolean showExamples, Throwable cause) {
            super(message, cause);
            this.showHelpInfo = showHelpInfo;
            this.showExamples = showExamples;
        }
        
        public boolean shouldShowHelpInfo() {
            return showHelpInfo;
        }
        
        public boolean shouldShowExamples() {
            return showExamples;
        }
    }
}
