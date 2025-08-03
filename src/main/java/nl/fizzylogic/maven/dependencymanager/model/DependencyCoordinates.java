package nl.fizzylogic.maven.dependencymanager.model;

/** Simple value class to hold dependency coordinates */
public class DependencyCoordinates {
  private final String groupId;
  private final String artifactId;
  private final String version;

  public DependencyCoordinates(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  public boolean hasVersion() {
    return version != null;
  }

  @Override
  public String toString() {
    return groupId + ":" + artifactId + (version != null ? ":" + version : "");
  }
}
