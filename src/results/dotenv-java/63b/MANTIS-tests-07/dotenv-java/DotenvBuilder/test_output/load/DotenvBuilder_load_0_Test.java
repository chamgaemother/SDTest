package io.github.cdimascio.dotenv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class DotenvBuilder_load_0_Test {

    @Test
    @DisplayName("TC01: When systemProperties is false and .env file is empty, no entries loop executed and returns empty Dotenv")
    void test_TC01() throws IOException {
        // Design: systemProperties=false by default triggers B1 path with zero loop iterations
        Path tempDir = Files.createTempDirectory("dotenv_test_tc01");
        Path envFile = tempDir.resolve(".env");
        Files.createFile(envFile); // empty file

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env");
        Dotenv result = builder.load();

        // Expect no entries and no system props set
        assertTrue(result.entries().isEmpty(), "Expected no entries for empty .env");
        assertNull(System.getProperty("ANY_KEY"), "No system property should be set for ANY_KEY");
    }

    @Test
    @DisplayName("TC02: When systemProperties is false and .env file has one entry, loop executes once and returns single entry")
    void test_TC02() throws IOException {
        // Design: systemProperties=false triggers B1; one line => one iteration
        Path tempDir = Files.createTempDirectory("dotenv_test_tc02");
        Path envFile = tempDir.resolve(".env");
        Files.write(envFile, "KEY=VALUE\n".getBytes());

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env");
        Dotenv result = builder.load();

        Set<DotenvEntry> entries = result.entries();
        assertEquals(1, entries.size(), "Expected exactly one entry parsed");
        assertTrue(entries.contains(new DotenvEntry("KEY", "VALUE")),
                "Expected entry KEY=VALUE in result");
        assertNull(System.getProperty("KEY"), "System property should not be set for KEY");
    }

    @Test
    @DisplayName("TC03: When systemProperties is false and .env file has multiple entries, loop executes multiple times and returns all entries")
    void test_TC03() throws IOException {
        // Design: systemProperties=false triggers B1; multiple lines => multiple iterations
        Path tempDir = Files.createTempDirectory("dotenv_test_tc03");
        Path envFile = tempDir.resolve(".env");
        Files.write(envFile, "A=1\nB=2\nC=3\n".getBytes());

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env");
        Dotenv result = builder.load();

        Set<DotenvEntry> entries = result.entries();
        assertEquals(3, entries.size(), "Expected three entries parsed");
        assertTrue(entries.contains(new DotenvEntry("A", "1")), "Expected entry A=1");
        assertTrue(entries.contains(new DotenvEntry("B", "2")), "Expected entry B=2");
        assertTrue(entries.contains(new DotenvEntry("C", "3")), "Expected entry C=3");
        assertNull(System.getProperty("A"), "System property should not be set for A");
        assertNull(System.getProperty("B"), "System property should not be set for B");
        assertNull(System.getProperty("C"), "System property should not be set for C");
    }

    @Test
    @DisplayName("TC04: When systemProperties is true and .env has one entry, branch-true path sets system property then returns entry")
    void test_TC04() throws IOException {
        // Design: systemProperties=true triggers B2 directly, but side-effect is setting system props
        Path tempDir = Files.createTempDirectory("dotenv_test_tc04");
        Path envFile = tempDir.resolve(".env");
        Files.write(envFile, "X=Y\n".getBytes());

        // Clear before
        System.clearProperty("X");

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env")
                .systemProperties();
        Dotenv result = builder.load();

        // Expect system property set
        assertEquals("Y", System.getProperty("X"), "Expected system property X set to Y");
        Set<DotenvEntry> entries = result.entries();
        assertTrue(entries.contains(new DotenvEntry("X", "Y")), "Expected entry X=Y in result");

        // Clean up
        System.clearProperty("X");
    }

    @Test
    @DisplayName("TC05: When systemProperties is true and .env is empty, branch-true path executes no loop and returns empty Dotenv")
    void test_TC05() throws IOException {
        // Design: systemProperties=true triggers B2; empty file causes no loop side-effects
        Path tempDir = Files.createTempDirectory("dotenv_test_tc05");
        Path envFile = tempDir.resolve(".env");
        Files.createFile(envFile);

        // Clear any potential prop
        System.clearProperty("ANY");

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env")
                .systemProperties();
        Dotenv result = builder.load();

        assertTrue(result.entries().isEmpty(), "Expected no entries for empty .env");
        assertNull(System.getProperty("ANY"), "No system property should be set for ANY");
    }

    @Test
    @DisplayName("TC06: When .env file is malformed, parser.parse throws DotenvException and load propagates it")
    void test_TC06() throws IOException {
        // Design: invalid syntax triggers exception before any branch on systemProperties
        Path tempDir = Files.createTempDirectory("dotenv_test_tc06");
        Path envFile = tempDir.resolve(".env");
        // Malformed line (no equals)
        Files.write(envFile, "MALFORMED_LINE\n".getBytes());

        DotenvBuilder builder = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env");

        // Expect DotenvException
        assertThrows(DotenvException.class, builder::load,
                "Expected DotenvException for malformed .env content");
    }
}