package io.github.cdimascio.dotenv;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.github.cdimascio.dotenv.DotenvException;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DotenvBuilder_load_0_Test {

    @Test
    @DisplayName("load() with empty .env file and default systemProperties=false processes zero entries and returns DotenvImpl")
    void test_TC01() throws IOException {
        Path tempDir = Files.createTempDirectory("dotenv_test_tc01");
        Files.write(tempDir.resolve(".env"), new byte[0]); 

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env");
        Dotenv result = builder.load();

        Set<DotenvEntry> fileEntries = result.entries(entry -> true);
        assertEquals(0, fileEntries.size());
    }

    @Test
    @DisplayName("load() with one valid entry in .env and default systemProperties=false processes one entry")
    void test_TC02() throws IOException {
        Path tempDir = Files.createTempDirectory("dotenv_test_tc02");
        Files.write(tempDir.resolve(".env"), "KEY=VALUE\n".getBytes(StandardCharsets.UTF_8));

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env");
        Dotenv result = builder.load();

        assertEquals("VALUE", result.get("KEY"));
        Set<DotenvEntry> fileEntries = result.entries(entry -> true);
        assertEquals(1, fileEntries.size());
    }

    @Test
    @DisplayName("load() with multiple entries in .env and default systemProperties=false processes multiple entries")
    void test_TC03() throws IOException {
        Path tempDir = Files.createTempDirectory("dotenv_test_tc03");
        String content = "A=1\nB=2\nC=3\n";
        Files.write(tempDir.resolve(".env"), content.getBytes(StandardCharsets.UTF_8));

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env");
        Dotenv result = builder.load();

        Set<DotenvEntry> fileEntries = result.entries(entry -> true);
        assertEquals(3, fileEntries.size());
        assertTrue(fileEntries.stream().anyMatch(e -> e.getKey().equals("A") && e.getValue().equals("1")));
        assertTrue(fileEntries.stream().anyMatch(e -> e.getKey().equals("B") && e.getValue().equals("2")));
        assertTrue(fileEntries.stream().anyMatch(e -> e.getKey().equals("C") && e.getValue().equals("3")));
    }

    @Test
    @DisplayName("load() with systemProperties=true skips setting System properties in builder and returns normally")
    void test_TC04() throws IOException {
        Path tempDir = Files.createTempDirectory("dotenv_test_tc04");
        Files.write(tempDir.resolve(".env"), "KEY=VAL\n".getBytes(StandardCharsets.UTF_8));

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env")
                .systemProperties();
        System.clearProperty("KEY");

        Dotenv result = builder.load();
        assertEquals("VAL", result.get("KEY"));
        assertNull(System.getProperty("KEY"));
    }

    @Test
    @DisplayName("load() with missing .env file and default throwIfMissing=true throws DotenvException")
    void test_TC05() throws IOException {
        Path tempDir = Files.createTempDirectory("dotenv_test_tc05");

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env");
        assertThrows(DotenvException.class, builder::load);
    }

    @Test
    @DisplayName("load() with missing .env file and ignoreIfMissing() suppresses exception and returns empty Dotenv")
    void test_TC06() throws IOException {
        Path tempDir = Files.createTempDirectory("dotenv_test_tc06");

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env")
                .ignoreIfMissing();
        Dotenv result = builder.load();

        Set<DotenvEntry> fileEntries = result.entries(entry -> true);
        assertEquals(0, fileEntries.size());
    }

    @Test
    @DisplayName("load() with malformed line in .env and ignoreIfMalformed() suppresses exception and skips malformed entries")
    void test_TC07() throws IOException {
        Path tempDir = Files.createTempDirectory("dotenv_test_tc07");
        String content = "VALID=good\nMALFORMEDLINE\n";
        Files.write(tempDir.resolve(".env"), content.getBytes(StandardCharsets.UTF_8));

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env")
                .ignoreIfMalformed();
        Dotenv result = builder.load();

        Set<DotenvEntry> fileEntries = result.entries(entry -> true);
        assertEquals(1, fileEntries.size());
        assertTrue(fileEntries.stream().anyMatch(e -> e.getKey().equals("VALID") && e.getValue().equals("good")));
    }
}