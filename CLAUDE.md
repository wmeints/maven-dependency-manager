# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in
this repository.

## Project Overview

This is a Quarkus-based command-line application for Maven dependency management. 
The application uses Picocli for command-line interface and provides functionality to
add dependencies and search for them in Maven repositories.

**Key Technologies:**

- Quarkus 3.25.0 (Supersonic Subatomic Java Framework)
- Java 21
- Picocli for CLI commands
- Apache Maven for dependency management and parsing
- Maven Wrapper (mvnw) for build consistency

## Common Development Commands

### Running the Application

```bash
# Run in development mode with live coding
./mvnw quarkus:dev

# Run with command-line arguments in dev mode (examples)
./mvnw quarkus:dev -Dquarkus.args='search jackson'
./mvnw quarkus:dev -Dquarkus.args='search org.springframework:spring-core'
./mvnw quarkus:dev -Dquarkus.args='--help'
```

### Building and Packaging
```bash
# Standard build
./mvnw package

# Build uber-jar
./mvnw package -Dquarkus.package.jar.type=uber-jar

# Create native executable
./mvnw package -Dnative

# Native build using container (if GraalVM not installed locally)
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

### Testing
```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw integration-test

# Run all tests including integration tests
./mvnw verify
```

### Running the Packaged Application
```bash
# Run regular JAR
java -jar target/quarkus-app/quarkus-run.jar

# Run uber-jar
java -jar target/*-runner.jar

# Run native executable
./target/dependencymanager-1.0.0-SNAPSHOT-runner
```

## Architecture and Code Structure

### Command Structure
The application follows a hierarchical command structure using Picocli:

- **RootCommand**: Top-level command with help options and subcommands
- **AddDependencyCommand**: Handles adding dependencies (format: `groupId:artifactId:version`)
- **SearchCommand**: Searches for dependencies in Maven repositories

### Package Organization

- `com.infosupport.maven`: Main package containing all CLI commands
- Commands are implemented as separate classes implementing `Runnable`
- Uses Picocli annotations for command configuration and parameter binding

### Key Dependencies

- `quarkus-picocli`: CLI framework integration
- `quarkus-arc`: Dependency injection
- `maven-model`, `maven-settings`: Maven configuration and model parsing
- `quarkus-junit5`: Testing framework

### Development Notes

- The application is designed for live coding with Quarkus dev mode
- Dev UI available at http://localhost:8080/q/dev/ when running in dev mode
- Native compilation supported for faster startup and lower memory usage
- Uses Maven Wrapper to ensure consistent build environment

### Current Implementation Status

- **SearchCommand**: Fully implemented with Maven Central Search API integration
  - Supports both keyword searches (`search jackson`) and exact searches (`search org.springframework:spring-core`)
  - Displays results in formatted table with GroupId, ArtifactId, and Latest version
  - Includes comprehensive error handling and user-friendly messages
  - Uses Maven Central Search API for dependency discovery
- **AddDependencyCommand**: Has placeholder implementation - needs to be implemented

### Search Command Usage

The search command supports two search patterns:

1. **Keyword Search**: `search <keywords>`
   - Example: `search jackson` - finds all dependencies with "jackson" in their metadata
   
2. **Exact Search**: `search <groupId>:<artifactId>`
   - Example: `search org.springframework:spring-core` - finds exact match for the specified dependency

The command will display results in a formatted table showing:
- GroupId: The Maven groupId
- ArtifactId: The Maven artifactId  
- Latest version: The most recent version available

### Docker Support
Multiple Dockerfile variants available in `src/main/docker/`:
- `Dockerfile.jvm`: Regular JVM-based image
- `Dockerfile.native`: Native executable image
- `Dockerfile.native-micro`: Minimal native image
- `Dockerfile.legacy-jar`: Legacy JAR-based image