package io.github.cdimascio.dotenv;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.github.cdimascio.dotenv.DotenvException;
import io.github.cdimascio.dotenv.DotenvEntry;
import io.github.cdimascio.dotenv.internal.DotenvParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for DotenvBuilder.load() covering all specified scenarios.
 */
public class DotenvBuilder_load_0_Test {

    @Test
    @DisplayName("TC01: load() with default settings and empty .env yields empty Dotenv without setting any system properties")
    void test_TC01(@TempDir Path tempDir) throws Exception {
        // Design: systemProperties=false so branch into B1, loop-0 because file is empty.
        // Create empty .env file
        Path envFile = tempDir.resolve(".env");
        Files.createFile(envFile);

        // Build and load
        DotenvBuilder builder = Dotenv.configure().directory(tempDir.toString());
        Dotenv dotenv = builder.load();

        // Use entries(filter) with non-null to get only file entries (setInFile)
        Set<DotenvEntry> inFile = dotenv.entries(entry -> true);
        assertTrue(inFile.isEmpty(), "Expected no entries from empty .env file");

        // No system property should have been set for any key from file
        // Pick arbitrary key, ensure System.getProperty remains null
        assertNull(System.getProperty("NON_EXISTENT_KEY"));
    }

    @Test
    @DisplayName("TC02: load() with default settings and one entry sets that single entry as system property and returns it")
    void test_TC02(@TempDir Path tempDir) throws Exception {
        // Design: systemProperties=false => B1 branch, loop-1 over one entry
        Path envFile = tempDir.resolve(".env");
        Files.write(envFile, Collections.singletonList("KEY=VALUE"));

        DotenvBuilder builder = Dotenv.configure().directory(tempDir.toString());
        Dotenv dotenv = builder.load();

        // Check returned value
        assertEquals("VALUE", dotenv.get("KEY"),
                "Expected dotenv.get(\"KEY\") to return the value from .env");

        // And as systemProperties=false, B1 sets property
        assertEquals("VALUE", System.getProperty("KEY"),
                "Expected system property KEY to be set to VALUE");
    }

    @Test
    @DisplayName("TC03: load() with default settings and multiple entries sets all entries as system properties and returns full set")
    void test_TC03(@TempDir Path tempDir) throws Exception {
        // Design: systemProperties=false => B1, loop-N over three entries
        Path envFile = tempDir.resolve(".env");
        Files.write(envFile, List.of("A=1", "B=2", "C=3"));

        DotenvBuilder builder = Dotenv.configure().directory(tempDir.toString());
        Dotenv dotenv = builder.load();

        // The set of returned file entries
        Set<DotenvEntry> inFile = dotenv.entries(entry -> true);
        assertEquals(3, inFile.size(), "Expected exactly three entries from .env file");
        assertTrue(inFile.contains(new DotenvEntry("A", "1")));
        assertTrue(inFile.contains(new DotenvEntry("B", "2")));
        assertTrue(inFile.contains(new DotenvEntry("C", "3")));

        // And system properties should be set accordingly
        assertEquals("1", System.getProperty("A"));
        assertEquals("2", System.getProperty("B"));
        assertEquals("3", System.getProperty("C"));
    }

    @Test
    @DisplayName("TC04: load() with systemProperties enabled skips setting any system properties but returns all entries")
    void test_TC04(@TempDir Path tempDir) throws Exception {
        // Design: systemProperties=true => B2 branch, skip loop-0
        Path envFile = tempDir.resolve(".env");
        Files.write(envFile, Collections.singletonList("X=Y"));

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .systemProperties(); // enable skip of automatic setting

        Dotenv dotenv = builder.load();

        // Should still return the entry
        assertEquals("Y", dotenv.get("X"),
                "Expected dotenv.get(\"X\") to return the value from .env");

        // But should NOT have set system property
        assertNull(System.getProperty("X"),
                "Expected system property X to remain null when systemProperties() is enabled");
    }

    @Test
    @DisplayName("TC05: load() throws DotenvException when parser.parse() fails")
    void test_TC05() throws Exception {
        // Design: simulate DotenvParser.parse() throwing => B0 only
        // Use Mockito to intercept the construction of DotenvParser
        try (MockedConstruction<DotenvParser> mocked = Mockito.mockConstruction(
                DotenvParser.class,
                (mock, context) -> when(mock.parse()).thenThrow(new DotenvException("fail")))) {

            DotenvBuilder builder = io.github.cdimascio.dotenv.Dotenv.configure();
            DotenvException ex = assertThrows(DotenvException.class, builder::load,
                    "Expected load() to throw DotenvException when parser.parse() fails");
            assertEquals("fail", ex.getMessage());
        }

        // Ensure no spurious system properties were set
        // We verify a random key is still null
        assertNull(System.getProperty("ANY_KEY_THAT_DOES_NOT_EXIST"));
    }
}