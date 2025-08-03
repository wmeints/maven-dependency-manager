package nl.fizzylogic.maven.dependencymanager;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Service for reading, modifying, and writing Maven POM files.
 */
@ApplicationScoped
public class ProjectModelEditor {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectModelEditor.class.getName());
    private static final String POM_FILE_NAME = "pom.xml";

    /**
     * Check if a pom.xml file exists in the current directory.
     * 
     * @return true if pom.xml exists, false otherwise
     */
    public boolean pomExists() {
        return pomExists(new File("."));
    }

    /**
     * Check if a pom.xml file exists in the specified directory.
     * 
     * @param directory The directory to check
     * @return true if pom.xml exists, false otherwise
     */
    public boolean pomExists(File directory) {
        File pomFile = new File(directory, POM_FILE_NAME);
        return pomFile.exists() && pomFile.isFile();
    }

    /**
     * Read and parse the pom.xml file from the current directory.
     * 
     * @return The parsed Maven Model
     * @throws IOException if the pom.xml file cannot be read or parsed
     */
    public Model readPom() throws IOException {
        return readPom(new File("."));
    }

    /**
     * Read and parse the pom.xml file from the specified directory.
     * 
     * @param directory The directory containing the pom.xml file
     * @return The parsed Maven Model
     * @throws IOException if the pom.xml file cannot be read or parsed
     */
    public Model readPom(File directory) throws IOException {
        File pomFile = new File(directory, POM_FILE_NAME);
        
        if (!pomFile.exists()) {
            throw new IOException("pom.xml file not found in directory: " + directory.getAbsolutePath());
        }
        
        LOGGER.info("Reading pom.xml from: " + pomFile.getAbsolutePath());
        
        try (FileReader reader = new FileReader(pomFile)) {
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            return pomReader.read(reader);
        } catch (Exception e) {
            throw new IOException("Failed to parse pom.xml: " + e.getMessage(), e);
        }
    }

    /**
     * Write the Maven Model back to the pom.xml file.
     * 
     * @param model The Maven Model to write
     * @throws IOException if the pom.xml file cannot be written
     */
    public void writePom(Model model) throws IOException {
        writePom(model, new File("."));
    }

    /**
     * Write the Maven Model back to the pom.xml file in the specified directory.
     * 
     * @param model The Maven Model to write
     * @param directory The directory to write the pom.xml file to
     * @throws IOException if the pom.xml file cannot be written
     */
    public void writePom(Model model, File directory) throws IOException {
        File pomFile = new File(directory, POM_FILE_NAME);
        
        LOGGER.info("Writing pom.xml to: " + pomFile.getAbsolutePath());
        
        try (FileWriter writer = new FileWriter(pomFile)) {
            MavenXpp3Writer pomWriter = new MavenXpp3Writer();
            pomWriter.write(writer, model);
        } catch (Exception e) {
            throw new IOException("Failed to write pom.xml: " + e.getMessage(), e);
        }
    }

    /**
     * Add a dependency to the Maven Model.
     * This method checks for existing dependencies to avoid duplicates.
     * 
     * @param model The Maven Model to modify
     * @param groupId The dependency group ID
     * @param artifactId The dependency artifact ID
     * @param version The dependency version
     * @return true if the dependency was added, false if it already exists
     */
    public boolean addDependency(Model model, String groupId, String artifactId, String version) {
        // Check if dependency already exists
        if (dependencyExists(model, groupId, artifactId)) {
            LOGGER.warning("Dependency " + groupId + ":" + artifactId + " already exists in pom.xml");
            return false;
        }
        
        // Create new dependency
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        
        // Add to model
        model.addDependency(dependency);
        
        LOGGER.info("Added dependency: " + groupId + ":" + artifactId + ":" + version);
        return true;
    }

    /**
     * Check if a dependency already exists in the Maven Model.
     * 
     * @param model The Maven Model to check
     * @param groupId The dependency group ID
     * @param artifactId The dependency artifact ID
     * @return true if the dependency exists, false otherwise
     */
    public boolean dependencyExists(Model model, String groupId, String artifactId) {
        return model.getDependencies().stream()
                .anyMatch(dep -> groupId.equals(dep.getGroupId()) && artifactId.equals(dep.getArtifactId()));
    }

    /**
     * Add a dependency to the current project's pom.xml file.
     * This is a convenience method that reads the POM, adds the dependency, and writes it back.
     * 
     * @param groupId The dependency group ID
     * @param artifactId The dependency artifact ID
     * @param version The dependency version
     * @return true if the dependency was successfully added, false if it already exists
     * @throws IOException if there are issues reading or writing the pom.xml file
     */
    public boolean addDependencyToPom(String groupId, String artifactId, String version) throws IOException {
        if (!pomExists()) {
            throw new IOException("No pom.xml file found in current directory");
        }
        
        // Read current POM
        Model model = readPom();
        
        // Add dependency
        boolean added = addDependency(model, groupId, artifactId, version);
        
        if (added) {
            // Write back to file
            writePom(model);
            LOGGER.info("Successfully updated pom.xml with dependency: " + groupId + ":" + artifactId + ":" + version);
        }
        
        return added;
    }

    /**
     * Get the current project's coordinates from the pom.xml.
     * 
     * @return A string in format "groupId:artifactId:version"
     * @throws IOException if the pom.xml cannot be read
     */
    public String getProjectCoordinates() throws IOException {
        Model model = readPom();
        String groupId = model.getGroupId() != null ? model.getGroupId() : 
                        (model.getParent() != null ? model.getParent().getGroupId() : "unknown");
        String artifactId = model.getArtifactId() != null ? model.getArtifactId() : "unknown";
        String version = model.getVersion() != null ? model.getVersion() : 
                        (model.getParent() != null ? model.getParent().getVersion() : "unknown");
        
        return groupId + ":" + artifactId + ":" + version;
    }
}