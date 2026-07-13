package io.github.cdimascio.dotenv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class DotenvBuilder_load_1_Test {

    @Test
    @DisplayName("load() with a valid .env file and systemProperties=false returns DotenvImpl without setting any System properties")
    void test_TC06() throws IOException, DotenvException {
        // Arrange: create a .env file with two entries A=1 and B=2
        Path envPath = Paths.get(".env");
        Files.write(envPath, java.util.Arrays.asList("A=1", "B=2"));
        // Ensure systemProperties flag is false by default and clear any existing props
        System.clearProperty("A");
        System.clearProperty("B");
        // DotenvBuilder.systemProperties defaults to false, so the branch for skipping setting System properties is taken
        DotenvBuilder builder = Dotenv.configure();

        // Act
        Dotenv dotenv = builder.load();

        // Assert
        // entries(filter) should yield exactly the two entries from the file
        Set<DotenvEntry> entries = dotenv.entries((DotenvEntry entry) -> true);
        assertEquals(2, entries.size(), "Expected two entries from .env file");
        // System properties A and B should remain unset when systemProperties=false
        assertNull(System.getProperty("A"), "System property 'A' should not be set");
        assertNull(System.getProperty("B"), "System property 'B' should not be set");

        // Cleanup
        Files.deleteIfExists(envPath);
    }

    @Test
    @DisplayName("load() with missing .env file and ignoreIfMissing() returns empty Dotenv without exception")
    void test_TC07() throws IOException, DotenvException {
        // Arrange: ensure no .env file exists; ignore missing file should suppress exception
        Path envPath = Paths.get(".env");
        Files.deleteIfExists(envPath);
        DotenvBuilder builder = Dotenv.configure().ignoreIfMissing();

        // Act
        Dotenv dotenv = builder.load();

        // Assert: entries(filter) should be empty when file is missing and ignoreIfMissing applied
        Set<DotenvEntry> entries = dotenv.entries((DotenvEntry entry) -> true);
        assertTrue(entries.isEmpty(), "Expected no entries when .env file is missing and ignoreIfMissing() is used");
    }

    @Test
    @DisplayName("load() with a malformed .env entry and ignoreIfMalformed() returns DotenvImpl containing only well-formed entries")
    void test_TC08() throws IOException, DotenvException {
        // Arrange: create .env with one malformed line and one valid entry; ignoreIfMalformed should drop malformed lines
        Path envPath = Paths.get(".env");
        Files.write(envPath, java.util.Arrays.asList("BADLINE", "KEY=VAL"));
        DotenvBuilder builder = Dotenv.configure().ignoreIfMalformed();

        // Act
        Dotenv dotenv = builder.load();

        // Assert: only the well-formed KEY=VAL entry should be present
        Set<DotenvEntry> entries = dotenv.entries((DotenvEntry entry) -> true);
        assertEquals(1, entries.size(), "Expected only one well-formed entry");
        assertTrue(
            entries.stream().anyMatch(e -> "KEY".equals(e.getKey()) && "VAL".equals(e.getValue())),
            "Expected entry KEY=VAL to be present"
        );

        // Cleanup
        Files.deleteIfExists(envPath);
    }
}