package nl.fizzylogic.maven.dependencymanager.commands;

import jakarta.inject.Inject;
import nl.fizzylogic.maven.dependencymanager.services.MavenDependencyResolver;
import nl.fizzylogic.maven.dependencymanager.services.ProjectModelEditor;
import nl.fizzylogic.maven.dependencymanager.model.DependencyCoordinates;
import nl.fizzylogic.maven.dependencymanager.model.ResolvedDependency;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
    name = "add",
    description = "Add a dependency to the current project's pom.xml",
    mixinStandardHelpOptions = true
)
public class AddDependencyCommand implements Runnable {
    
    @Parameters(
        paramLabel = "DEPENDENCY", 
        description = "The dependency coordinates in format <groupId>:<artifactId>[:<version>]. " +
                     "If version is omitted, the latest version will be resolved automatically."
    )
    String dependencyCoordinates;

    @Inject
    MavenDependencyResolver dependencyResolver;
    
    @Inject
    ProjectModelEditor pomManipulation;

    @Override
    public void run() {
        try {
            // Check if pom.xml exists in current directory
            if (!pomManipulation.pomExists()) {
                System.err.println("Error: No pom.xml file found in current directory");
                System.err.println("Please run this command from a Maven project directory");
                System.exit(1);
            }
            
            // Parse and validate dependency coordinates
            DependencyCoordinates coordinates = parseDependencyCoordinates(dependencyCoordinates);
            
            System.out.println("Adding dependency: " + coordinates.getGroupId() + ":" + 
                             coordinates.getArtifactId() + 
                             (coordinates.getVersion() != null ? ":" + coordinates.getVersion() : " (resolving latest version...)"));
            
            // Resolve dependency version
            ResolvedDependency resolved = dependencyResolver.resolveDependency(coordinates);
            
            if (resolved == null) {
                System.err.println("Error: Could not resolve dependency " + coordinates.getGroupId() + ":" + coordinates.getArtifactId());
                if (coordinates.getVersion() != null) {
                    System.err.println("The specified version " + coordinates.getVersion() + " may not exist in the configured repositories");
                } else {
                    System.err.println("The dependency may not exist in the configured repositories");
                }
                System.exit(1);
            }
            
            System.out.println("Resolved to version: " + resolved.getVersion());
            
            // Add dependency to POM
            boolean added = pomManipulation.addDependencyToPom(
                resolved.getGroupId(), 
                resolved.getArtifactId(), 
                resolved.getVersion()
            );
            
            if (added) {
                System.out.println("✓ Successfully added dependency to pom.xml:");
                System.out.println("  " + resolved.toString());
            } else {
                System.out.println("Dependency " + resolved.getGroupId() + ":" + resolved.getArtifactId() + " already exists in pom.xml");
                System.out.println("No changes made.");
            }
            
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            System.err.println("Usage examples:");
            System.err.println("  add org.springframework:spring-core");
            System.err.println("  add org.springframework:spring-core:5.3.21");
            System.err.println("  add com.fasterxml.jackson.core:jackson-core:2.15.2");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Parses dependency coordinates from string format groupId:artifactId[:version]
     * 
     * @param coordinates The dependency coordinates string
     * @return Parsed dependency coordinates
     * @throws IllegalArgumentException if the format is invalid
     */
    DependencyCoordinates parseDependencyCoordinates(String coordinates) {
        if (coordinates == null || coordinates.trim().isEmpty()) {
            throw new IllegalArgumentException("Dependency coordinates cannot be empty");
        }

        // Use -1 to include trailing empty strings
        String[] parts = coordinates.split(":", -1);
        
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException(
                "Invalid dependency coordinates format. Expected: <groupId>:<artifactId>[:<version>], got: " + coordinates
            );
        }

        String groupId = parts[0].trim();
        String artifactId = parts[1].trim();
        String version = parts.length == 3 ? parts[2].trim() : null;

        if (groupId.isEmpty()) {
            throw new IllegalArgumentException("GroupId cannot be empty");
        }
        
        if (artifactId.isEmpty()) {
            throw new IllegalArgumentException("ArtifactId cannot be empty");
        }
        
        if (parts.length == 3 && (version == null || version.isEmpty())) {
            throw new IllegalArgumentException("Version cannot be empty when specified");
        }

        return new DependencyCoordinates(groupId, artifactId, version);
    }

}
