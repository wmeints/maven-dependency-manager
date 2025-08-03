# Implementation Plan: Remove Dependencies Command

## Overview
Implement the `remove` command for the Maven dependency manager CLI application that can remove dependencies from a project's `pom.xml` file using proper Maven APIs.

## Requirements Analysis

### Core Functionality
1. **Command Structure**: Implement `remove <groupId>:<artifactId>` command
2. **Dependency Detection**: Check if dependency exists in current project's pom.xml
3. **POM Modification**: Use Maven APIs to modify `pom.xml` (not raw XML editing)
4. **User Feedback**: Notify user of success or if dependency is not found

### Input Format
- `groupId:artifactId` - Remove dependency with exact groupId and artifactId match

## Implementation Steps

### 1. Dependencies and Configuration
- **Utilize Existing Maven Dependencies**: Use already available Maven API dependencies
  - `maven-model` (already present)
  - `maven-model-builder` for POM manipulation
  - Leverage existing infrastructure from SearchCommand and AddDependencyCommand

### 2. Core Components

#### 2.1 RemoveDependencyCommand Creation
- **Command Class**: Create new `RemoveDependencyCommand.java` implementing `Runnable`
- **Input Parsing**: Parse dependency coordinates in `groupId:artifactId` format
- **Validation**: Validate coordinate format and required parameters
- **Integration**: Connect with Maven configuration and POM manipulation services

#### 2.2 Maven Configuration Service (Reuse Existing)
- **Settings Loading**: Reuse existing Maven settings infrastructure
- **POM Location**: Verify `pom.xml` exists in current working directory

#### 2.3 Dependency Detection Service
- **POM Parsing**: Load and parse existing `pom.xml` in current directory
- **Dependency Search**: Search for matching dependency by groupId and artifactId
- **Exact Matching**: Implement precise coordinate matching logic

#### 2.4 POM Manipulation Service (Enhanced)
- **POM Reading**: Load and parse existing `pom.xml` in current directory
- **Dependency Removal**: Remove matching dependency from dependencies section
- **POM Writing**: Save modified POM back to file system
- **Backup Strategy**: Consider creating backup before modification

### 3. Error Handling and User Experience

#### 3.1 Validation and Errors
- **Coordinate Format**: Validate `groupId:artifactId` format (no version needed for removal)
- **POM Existence**: Verify `pom.xml` exists in current directory
- **File Permissions**: Handle read/write permission issues
- **Dependency Not Found**: Clear error messages when dependency doesn't exist in project

#### 3.2 User Feedback
- **Success Messages**: Confirm dependency removal with coordinate information
- **Not Found Messages**: Clear notification when dependency is not present in project
- **Error Messages**: Detailed feedback for file system and parsing errors

### 4. Technical Implementation Details

#### 4.1 Maven API Integration
```java
// Key Maven components to use:
- MavenProject for POM representation
- ModelReader/ModelWriter for file I/O
- DefaultModelBuilder for POM building
- Model for dependency manipulation
```

#### 4.2 Service Architecture
- **MavenConfigurationService**: Handle settings and POM location (reuse existing)
- **DependencyDetectionService**: Find matching dependencies in current POM
- **PomManipulationService**: Read, modify, and write POM files (enhance existing)
- **RemoveDependencyCommand**: Orchestrate the removal workflow

#### 4.3 Data Flow
1. Parse command arguments → dependency coordinates (groupId:artifactId)
2. Verify POM exists in current directory → file validation
3. Load current POM → model object
4. Search for matching dependency → found/not found result
5. If found: Remove dependency from model → modified model
6. Write POM back to file → success confirmation
7. If not found: Display appropriate error message

### 5. Testing Strategy

#### 5.1 Unit Tests
- **Coordinate Parsing**: Test various input formats for `groupId:artifactId`
- **Dependency Detection**: Test finding dependencies in sample POMs
- **POM Manipulation**: Test dependency removal from sample POMs
- **Error Scenarios**: Test invalid inputs and dependencies not found

#### 5.2 Integration Tests
- **End-to-End Workflow**: Test complete remove command flow
- **File System Operations**: Test POM reading and writing
- **Command Integration**: Test command registration and execution

#### 5.3 Test Data
- **Sample POMs**: Various POM structures with different dependency configurations
- **Test Dependencies**: Known dependencies for removal testing
- **Edge Cases**: POMs without dependencies, empty POMs, malformed POMs

### 6. Implementation Order

1. **Phase 1**: Basic structure and coordinate parsing
   - Create RemoveDependencyCommand class with Picocli annotations
   - Implement coordinate validation for `groupId:artifactId` format
   - Add basic error handling and user feedback

2. **Phase 2**: Command integration
   - Register RemoveDependencyCommand as subcommand in RootCommand
   - Implement help text and command description
   - Test basic command structure

3. **Phase 3**: POM detection and parsing
   - Implement POM file detection in current directory
   - Add POM parsing using Maven APIs
   - Handle file not found and permission errors

4. **Phase 4**: Dependency detection and removal
   - Implement dependency search logic in POM model
   - Add dependency removal from model
   - Test with various POM structures

5. **Phase 5**: Integration and testing
   - Connect all components in complete workflow
   - Comprehensive testing with real POM files
   - Error handling refinement and user experience improvement

### 7. Success Criteria

- ✅ Command accepts `groupId:artifactId` format for dependency removal
- ✅ Successfully removes existing dependencies from project POM
- ✅ Provides clear notification when dependency is not found in project
- ✅ Modifies `pom.xml` using Maven APIs (not raw XML)
- ✅ Provides clear error messages for various failure scenarios
- ✅ Maintains POM formatting and structure
- ✅ Works with existing Maven project structure
- ✅ Integrates seamlessly with existing command architecture

### 8. Risk Mitigation

- **POM Corruption**: Always validate POM before writing changes
- **File System Errors**: Handle permission issues and file locking gracefully
- **Dependency Matching**: Ensure exact matching to avoid removing wrong dependencies
- **Backup Strategy**: Consider creating backup before modification to prevent data loss

## Next Steps

1. Review current codebase structure and existing command implementations
2. Implement Phase 1 with RemoveDependencyCommand creation and coordinate parsing
3. Iteratively build out remaining phases with testing at each step
4. Integration testing with real Maven projects and POM files
5. Ensure consistency with existing SearchCommand and AddDependencyCommand patterns