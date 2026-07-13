package io.github.cdimascio.dotenv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class DotenvBuilder_load_0_Test {

    @Test
    @DisplayName("TC01: load() returns DotenvImpl with entries when systemProperties=false (branch-false) and one entry in .env")
    void test_TC01() throws Exception {
        // GIVEN a temp dir with one-entry .env; systemProperties=false so we follow B0→B2 (no setting of system props)
        Path dir = Files.createTempDirectory("env1");
        Files.writeString(dir.resolve(".env"), "KEY=VALUE");
        DotenvBuilder builder = new DotenvBuilder().directory(dir.toString());
        // WHEN
        Dotenv dotenv = builder.load();
        // THEN
        Set<DotenvEntry> entries = dotenv.entries();
        assertTrue(entries.contains(new DotenvEntry("KEY", "VALUE")),
                   "Expected one DotenvEntry(KEY,VALUE) in entries");
        // System property should remain unset
        assertNull(System.getProperty("KEY"), "System property 'KEY' should not be set");
    }

    @Test
    @DisplayName("TC02: load() with systemProperties=true (branch-true) and empty .env yields empty entries and no properties set (loop-0)")
    void test_TC02() throws Exception {
        // GIVEN an empty .env file; systemProperties=true triggers B0→B1→B2 but no entries to loop over
        Path dir = Files.createTempDirectory("env2");
        Files.createFile(dir.resolve(".env"));
        DotenvBuilder builder = new DotenvBuilder().directory(dir.toString()).systemProperties();
        // WHEN
        Dotenv dotenv = builder.load();
        // THEN
        assertTrue(dotenv.entries().isEmpty(), "Expected no entries for empty .env");
        // Ensure no random system property "ANY" exists as result
        assertNull(System.getProperty("ANY"), "No system property should be set for empty env");
    }

    @Test
    @DisplayName("TC03: load() with systemProperties=true (branch-true) and one entry sets exactly one system property (loop-1)")
    void test_TC03() throws Exception {
        // GIVEN one-entry .env; systemProperties=true so B0→B1→B2 and one iteration sets property A
        Path dir = Files.createTempDirectory("env3");
        Files.writeString(dir.resolve(".env"), "A=1");
        DotenvBuilder builder = new DotenvBuilder().directory(dir.toString()).systemProperties();
        // Clear before test
        System.clearProperty("A");
        // WHEN
        Dotenv dotenv = builder.load();
        // THEN
        assertEquals("1", dotenv.get("A"), "Dotenv.get should return the env value");
        assertEquals("1", System.getProperty("A"), "System property 'A' should be set to '1'");
        // Clean up
        System.clearProperty("A");
    }

    @Test
    @DisplayName("TC04: load() with systemProperties=true (branch-true) and multiple entries sets all system properties (loop-N)")
    void test_TC04() throws Exception {
        // GIVEN multi-entry .env; systemProperties=true triggers B0→B1→B2 and loops twice
        Path dir = Files.createTempDirectory("env4");
        Files.writeString(dir.resolve(".env"), "X=10\nY=20");
        DotenvBuilder builder = new DotenvBuilder().directory(dir.toString()).systemProperties();
        // Clear before test
        System.clearProperty("X");
        System.clearProperty("Y");
        // WHEN
        Dotenv dotenv = builder.load();
        // THEN
        assertAll(
            () -> assertEquals("10", dotenv.get("X"), "Dotenv.get X should yield '10'"),
            () -> assertEquals("20", dotenv.get("Y"), "Dotenv.get Y should yield '20'"),
            () -> assertEquals("10", System.getProperty("X"), "System prop X set to '10'"),
            () -> assertEquals("20", System.getProperty("Y"), "System prop Y set to '20'")
        );
        // Clean up
        System.clearProperty("X");
        System.clearProperty("Y");
    }

    @Test
    @DisplayName("TC05: load() throws DotenvException when .env is missing with default throwIfMissing=true")
    void test_TC05() throws Exception {
        // GIVEN no .env file in temp dir; default throwIfMissing=true causes exception at B0 before B1/B2
        Path dir = Files.createTempDirectory("env5");
        DotenvBuilder builder = new DotenvBuilder().directory(dir.toString());
        // No system props to clear in this scenario
        // WHEN & THEN
        assertThrows(DotenvException.class,
            builder::load,
            "Expected DotenvException when .env is missing and throwIfMissing=true");
    }
}