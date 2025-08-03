package nl.fizzylogic.maven.dependencymanager.model;

/** Value class representing a resolved dependency with its version. */
public class ResolvedDependency {
  private final String groupId;
  private final String artifactId;
  private final String version;

  public ResolvedDependency(String groupId, String artifactId, String version) {
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

  @Override
  public String toString() {
    return groupId + ":" + artifactId + ":" + version;
  }
}
