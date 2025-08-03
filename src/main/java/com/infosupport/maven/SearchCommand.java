package com.infosupport.maven;

import picocli.CommandLine;
import picocli.CommandLine.Parameters;

@CommandLine.Command(name = "search", mixinStandardHelpOptions = true, description = "Search for a dependency in the configured maven repositories")
public class SearchCommand implements Runnable {
    @Parameters(paramLabel = "QUERY", description = "Query string to search for")
    String query;

    public void run() {
        
    }
}
