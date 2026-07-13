package io.github.cdimascio.dotenv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DotenvBuilder_load_1_Test {

    @Test
    @DisplayName("load() with missing .env file and default throwIfMissing=true throws DotenvException")
    void test_TC06() throws IOException {
        // Arrange: create an empty temporary directory with no .env file
        Path tempDir = Files.createTempDirectory("dotenv_tc06");
        String dir = tempDir.toString();
        // Act & Assert: calling load() without ignoreIfMissing should throw DotenvException
        DotenvBuilder builder = Dotenv.configure().directory(dir);
        assertThrows(DotenvException.class, () -> {
            // Reflection is not needed as load() is public
            builder.load();
        });
    }

    @Test
    @DisplayName("load() with missing .env file and ignoreIfMissing() returns empty Dotenv without exception")
    void test_TC07() throws IOException, DotenvException {
        // Arrange: empty temp directory without .env file, set ignoreIfMissing to avoid exception
        Path tempDir = Files.createTempDirectory("dotenv_tc07");
        String dir = tempDir.toString();
        DotenvBuilder builder = Dotenv.configure().directory(dir).ignoreIfMissing();
        // Act: load should not throw and should return a Dotenv
        Dotenv dotenv = builder.load();
        // Assert: file-based entries (using non-null filter) should be empty, demonstrating no .env entries
        Dotenv.Filter dummyFilter = entry -> true; // Changed to lambda expression
        assertTrue(dotenv.entries(dummyFilter).isEmpty(), "Expected no file entries when .env is missing and ignoreIfMissing is set");
    }

    @Test
    @DisplayName("load() with malformed and valid lines and ignoreIfMalformed() loads only valid entries")
    void test_TC08() throws IOException, DotenvException {
        // Arrange: write one malformed line and one valid line to .env
        Path tempDir = Files.createTempDirectory("dotenv_tc08");
        Path envFile = tempDir.resolve(".env");
        Files.write(envFile, List.of("BADLINE", "A=1")); // BADLINE is malformed, A=1 is valid
        String dir = tempDir.toString();
        DotenvBuilder builder = Dotenv.configure()
                                      .directory(dir)
                                      .ignoreIfMalformed(); // ignore malformed lines
        // Act: load should parse and skip malformed, include only A=1
        Dotenv dotenv = builder.load();
        // Assert: valid key 'A' returns "1" and malformed key returns null
        assertEquals("1", dotenv.get("A"), "Valid entry A=1 should be loaded");
        assertNull(dotenv.get("BADLINE"), "Malformed entry should not be loaded when ignoreIfMalformed is set");
    }

    @Test
    @DisplayName("load() with malformed line and default throwIfMalformed=true throws DotenvException")
    void test_TC09() throws IOException {
        // Arrange: write a single malformed line to .env, default builder throws on malformed
        Path tempDir = Files.createTempDirectory("dotenv_tc09");
        Path envFile = tempDir.resolve(".env");
        Files.write(envFile, List.of("INVALID")); // malformed line
        String dir = tempDir.toString();
        DotenvBuilder builder = Dotenv.configure().directory(dir);
        // Act & Assert: load should throw DotenvException due to malformed line
        assertThrows(DotenvException.class, builder::load);
    }
}