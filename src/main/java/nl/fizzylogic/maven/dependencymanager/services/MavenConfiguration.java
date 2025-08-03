package nl.fizzylogic.maven.dependencymanager.services;

import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilder;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service to load Maven repository configuration from user and global settings.
 */
@ApplicationScoped
public class MavenConfiguration {
    
    private static final Logger LOGGER = Logger.getLogger(MavenConfiguration.class.getName());
    private static final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2";
    private static final String USER_SETTINGS_PATH = System.getProperty("user.home") + "/.m2/settings.xml";
    private static final String GLOBAL_SETTINGS_PATH = System.getenv("M2_HOME") != null ? 
            System.getenv("M2_HOME") + "/conf/settings.xml" : "/usr/share/maven/conf/settings.xml";

    /**
     * Get list of repository URLs configured in Maven settings.
     * Always includes Maven Central as fallback.
     */
    public List<String> getRepositoryUrls() {
        List<String> repositories = new ArrayList<>();
        
        try {
            Settings settings = loadMavenSettings();
            
            // Get repositories from active profiles
            for (String activeProfile : settings.getActiveProfiles()) {
                Profile profile = settings.getProfilesAsMap().get(activeProfile);
                if (profile != null) {
                    for (Repository repo : profile.getRepositories()) {
                        String url = repo.getUrl();
                        if (url != null && !url.trim().isEmpty()) {
                            repositories.add(url.trim());
                        }
                    }
                }
            }
            
            // Also check default profile repositories
            for (Profile profile : settings.getProfiles()) {
                if (profile.getActivation() != null && profile.getActivation().isActiveByDefault()) {
                    for (Repository repo : profile.getRepositories()) {
                        String url = repo.getUrl();
                        if (url != null && !url.trim().isEmpty() && !repositories.contains(url.trim())) {
                            repositories.add(url.trim());
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.warning("Failed to load Maven settings: " + e.getMessage());
        }
        
        // Always ensure Maven Central is available as fallback
        if (!repositories.contains(MAVEN_CENTRAL_URL)) {
            repositories.add(MAVEN_CENTRAL_URL);
        }
        
        LOGGER.info("Configured repositories: " + repositories);
        return repositories;
    }

    /**
     * Load Maven settings from user and global settings files.
     */
    private Settings loadMavenSettings() throws Exception {
        DefaultSettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();
        SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
        
        // Set user settings file
        File userSettingsFile = new File(USER_SETTINGS_PATH);
        if (userSettingsFile.exists()) {
            request.setUserSettingsFile(userSettingsFile);
            LOGGER.info("Loading user settings from: " + USER_SETTINGS_PATH);
        }
        
        // Set global settings file
        File globalSettingsFile = new File(GLOBAL_SETTINGS_PATH);
        if (globalSettingsFile.exists()) {
            request.setGlobalSettingsFile(globalSettingsFile);
            LOGGER.info("Loading global settings from: " + GLOBAL_SETTINGS_PATH);
        }
        
        SettingsBuildingResult result = settingsBuilder.build(request);
        return result.getEffectiveSettings();
    }
}