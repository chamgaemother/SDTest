package io.github.cdimascio.dotenv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertThrows;
public class DotenvBuilder_load_2_Test {

    @Test
    @DisplayName("load() throws DotenvException when .env is malformed and throwIfMalformed=true (default)")
    public void test_TC09() throws IOException {
        // Arrange: create a temporary directory and a malformed .env file in it
        Path tempDir = Files.createTempDirectory("dotenv-test");
        Path envFile = tempDir.resolve(".env");
        // This line has no '=' sign, representing a malformed entry
        Files.write(envFile, "BAD_LINE_WITHOUT_EQUALS".getBytes(), StandardOpenOption.CREATE);

        DotenvBuilder builder = Dotenv.configure()
            .directory(tempDir.toString());
        // throwIfMalformed is true by default, so the malformed file should trigger an exception during load()

        // Act & Assert: expect DotenvException due to malformed entry before any further processing
        assertThrows(DotenvException.class,
            () -> builder.load());
    }
}