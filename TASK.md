# Removing dependencies from the current project

I want to be able to remove dependencies with the command `remove <groupId>:<artifactId>`. The tool should check if the dependency is in the pom.xml in the current directory and remove it if it exists. If the dependency isn't available in the project, it should notify the user of this.