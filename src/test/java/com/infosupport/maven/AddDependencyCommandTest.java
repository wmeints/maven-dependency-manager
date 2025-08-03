package com.infosupport.maven;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class AddDependencyCommandTest {

    private AddDependencyCommand command;

    @BeforeEach
    void setUp() {
        command = new AddDependencyCommand();
    }

    @Test
    void testParseCoordinatesWithVersion() {
        command.dependencyCoordinates = "org.springframework:spring-core:5.3.21";
        
        AddDependencyCommand.DependencyCoordinates coords = 
            command.parseDependencyCoordinates("org.springframework:spring-core:5.3.21");
        
        assertEquals("org.springframework", coords.getGroupId());
        assertEquals("spring-core", coords.getArtifactId());
        assertEquals("5.3.21", coords.getVersion());
        assertTrue(coords.hasVersion());
    }

    @Test
    void testParseCoordinatesWithoutVersion() {
        AddDependencyCommand.DependencyCoordinates coords = 
            command.parseDependencyCoordinates("org.springframework:spring-core");
        
        assertEquals("org.springframework", coords.getGroupId());
        assertEquals("spring-core", coords.getArtifactId());
        assertNull(coords.getVersion());
        assertFalse(coords.hasVersion());
    }

    @Test
    void testParseCoordinatesWithSpaces() {
        AddDependencyCommand.DependencyCoordinates coords = 
            command.parseDependencyCoordinates(" org.springframework : spring-core : 5.3.21 ");
        
        assertEquals("org.springframework", coords.getGroupId());
        assertEquals("spring-core", coords.getArtifactId());
        assertEquals("5.3.21", coords.getVersion());
    }

    @Test
    void testParseCoordinatesInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            command.parseDependencyCoordinates("invalid-format");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            command.parseDependencyCoordinates("too:many:parts:here:invalid");
        });
    }

    @Test
    void testParseCoordinatesEmptyParts() {
        assertThrows(IllegalArgumentException.class, () -> {
            command.parseDependencyCoordinates(":spring-core:5.3.21");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            command.parseDependencyCoordinates("org.springframework::5.3.21");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            command.parseDependencyCoordinates("org.springframework:spring-core:");
        });
    }

    @Test
    void testParseCoordinatesNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            command.parseDependencyCoordinates(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            command.parseDependencyCoordinates("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            command.parseDependencyCoordinates("   ");
        });
    }

    @Test
    void testDependencyCoordinatesToString() {
        AddDependencyCommand.DependencyCoordinates coordsWithVersion = 
            new AddDependencyCommand.DependencyCoordinates("org.springframework", "spring-core", "5.3.21");
        assertEquals("org.springframework:spring-core:5.3.21", coordsWithVersion.toString());
        
        AddDependencyCommand.DependencyCoordinates coordsWithoutVersion = 
            new AddDependencyCommand.DependencyCoordinates("org.springframework", "spring-core", null);
        assertEquals("org.springframework:spring-core", coordsWithoutVersion.toString());
    }
}