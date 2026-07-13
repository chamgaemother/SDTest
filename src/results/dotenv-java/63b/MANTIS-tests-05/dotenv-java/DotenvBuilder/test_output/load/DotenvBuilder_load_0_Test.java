package io.github.cdimascio.dotenv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class DotenvBuilder_load_0_Test {

    @Test
    @DisplayName("TC01: default load with empty .env file does not throw and returns Dotenv with only system env entries (branch-true for systemProperties=false, loop-0 entries)")
    void test_TC01(@TempDir Path tempDir) throws IOException {
        // Given an empty .env file -> loop-0, systemProperties=false so branch-true enters forEach but no iterations
        Path envFile = tempDir.resolve(".env");
        Files.createFile(envFile);
        DotenvBuilder builder = new DotenvBuilder().directory(tempDir.toString()).filename(".env");
        // When
        Dotenv dotenv = builder.load();
        // Then entries() equals system env entries
        Set<DotenvEntry> expected = System.getenv().entrySet().stream()
                .map(e -> new DotenvEntry(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
        assertEquals(expected, dotenv.entries());
        // And entries(filter) with non-null filter returns empty set of file entries
        Set<DotenvEntry> fileEntries = dotenv.entries(entry -> true);
        assertTrue(fileEntries.isEmpty());
    }

    @Test
    @DisplayName("TC02: load with one valid entry returns DotenvImpl with one file entry and include system env (branch-true, loop-1 entries)")
    void test_TC02(@TempDir Path tempDir) throws IOException {
        // Given .env with one valid line -> loop-1, systemProperties=false so branch-true
        Path envFile = tempDir.resolve(".env");
        try (PrintWriter pw = new PrintWriter(envFile.toFile())) {
            pw.println("KEY1=VALUE1");
        }
        DotenvBuilder builder = new DotenvBuilder().directory(tempDir.toString()).filename(".env");
        // When
        Dotenv dotenv = builder.load();
        // Then file entry is present and get returns it
        assertEquals("VALUE1", dotenv.get("KEY1"));
        // And entries(filter) returns only that file entry
        Set<DotenvEntry> fileEntries = dotenv.entries(entry -> true);
        assertEquals(1, fileEntries.size());
        assertTrue(fileEntries.contains(new DotenvEntry("KEY1", "VALUE1")));
    }

    @Test
    @DisplayName("TC03: load with two valid entries returns DotenvImpl with two file entries and include system env (branch-true, loop-N entries)")
    void test_TC03(@TempDir Path tempDir) throws IOException {
        // Given .env with two valid lines -> loop-N >1
        Path envFile = tempDir.resolve(".env");
        try (PrintWriter pw = new PrintWriter(envFile.toFile())) {
            pw.println("A=1");
            pw.println("B=2");
        }
        DotenvBuilder builder = new DotenvBuilder().directory(tempDir.toString()).filename(".env");
        // When
        Dotenv dotenv = builder.load();
        // Then get returns correct values
        assertEquals("1", dotenv.get("A"));
        assertEquals("2", dotenv.get("B"));
        // entries(filter) returns exactly two file entries
        Set<DotenvEntry> fileEntries = dotenv.entries(entry -> true);
        Set<DotenvEntry> expectedFile = new HashSet<>(Arrays.asList(
                new DotenvEntry("A", "1"),
                new DotenvEntry("B", "2")
        ));
        assertEquals(expectedFile, fileEntries);
    }

    @Test
    @DisplayName("TC04: load with systemProperties enabled sets each variable as System property (branch-false skip-forEach in code, intended branch-true), loop-1 entry")
    void test_TC04(@TempDir Path tempDir) throws IOException {
        // Given .env with one entry and systemProperties=true -> skip forEach, branch-false, but parser still applied in constructor of DotenvImpl
        Path envFile = tempDir.resolve(".env");
        try (PrintWriter pw = new PrintWriter(envFile.toFile())) {
            pw.println("X=Y");
        }
        // Clear any existing property
        System.clearProperty("X");
        DotenvBuilder builder = new DotenvBuilder()
                .directory(tempDir.toString())
                .filename(".env")
                .systemProperties();
        // When
        Dotenv dotenv = builder.load();
        // Then system property X is set
        assertEquals("Y", System.getProperty("X"));
        // And get returns the system property or file value
        assertEquals("Y", dotenv.get("X"));
    }

    @Test
    @DisplayName("TC05: load throws DotenvException if .env file missing and throwIfMissing true (default) triggers exception")
    void test_TC05(@TempDir Path tempDir) {
        // Given no .env file and default throwIfMissing=true -> immediate exception at parse
        DotenvBuilder builder = new DotenvBuilder().directory(tempDir.toString()).filename(".env");
        // When/Then
        assertThrows(DotenvException.class, builder::load);
        // And no system properties set for missing file
        // (no key to check since file missing)
    }

    @Test
    @DisplayName("TC06: load does not throw when .env missing if ignoreIfMissing() is set (branch-true bypass exception)")
    void test_TC06(@TempDir Path tempDir) {
        // Given no .env file and ignoreIfMissing() disables missing-file exception -> branch-true
        DotenvBuilder builder = new DotenvBuilder()
                .directory(tempDir.toString())
                .filename(".env")
                .ignoreIfMissing();
        // When
        Dotenv dotenv = builder.load(); 
        // Then entries() matches system env entries
        Set<DotenvEntry> expected = System.getenv().entrySet().stream()
                .map(e -> new DotenvEntry(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
        assertEquals(expected.size(), dotenv.entries().size());
    }

    @Test
    @DisplayName("TC07: load throws DotenvException on malformed line and throwIfMalformed true (default)")
    void test_TC07(@TempDir Path tempDir) throws IOException {
        // Given .env with malformed line (no '=') and default throwIfMalformed=true -> exception
        Path envFile = tempDir.resolve(".env");
        try (PrintWriter pw = new PrintWriter(envFile.toFile())) {
            pw.println("NOEQUALSIGN");
        }
        DotenvBuilder builder = new DotenvBuilder().directory(tempDir.toString()).filename(".env");
        // When/Then
        assertThrows(DotenvException.class, builder::load);
    }

    @Test
    @DisplayName("TC08: load does not throw on malformed line when ignoreIfMalformed() is set and returns valid entries (branch-true skip exception)")
    void test_TC08(@TempDir Path tempDir) throws IOException {
        // Given .env with one good and one malformed, ignoreIfMalformed() removes exception -> branch-true
        Path envFile = tempDir.resolve(".env");
        try (PrintWriter pw = new PrintWriter(envFile.toFile())) {
            pw.println("good=val");
            pw.println("BADLINE");
        }
        DotenvBuilder builder = new DotenvBuilder()
                .directory(tempDir.toString())
                .filename(".env")
                .ignoreIfMalformed();
        // When
        Dotenv dotenv = builder.load();
        // Then good entry is present, malformed ignored
        assertEquals("val", dotenv.get("good"));
        Set<DotenvEntry> fileEntries = dotenv.entries(entry -> true);
        assertEquals(1, fileEntries.size());
        assertTrue(fileEntries.contains(new DotenvEntry("good", "val")));
    }

    @Test
    @DisplayName("TC09: load with entries(filter) null returns same as entries() (filter-null branch)")
    void test_TC09(@TempDir Path tempDir) throws IOException {
        // Given .env with one entry -> loop-1, filter passed null triggers filter-null branch
        Path envFile = tempDir.resolve(".env");
        try (PrintWriter pw = new PrintWriter(envFile.toFile())) {
            pw.println("KEY=V");
        }
        DotenvBuilder builder = new DotenvBuilder().directory(tempDir.toString()).filename(".env");
        // When
        Dotenv dotenv = builder.load();
        // Then entries(null) equals entries()
        assertEquals(dotenv.entries(), dotenv.entries(null));
    }

    @Test
    @DisplayName("TC10: load.get(key,default) returns default when key absent (boundary default-value)")
    void test_TC10(@TempDir Path tempDir) throws IOException {
        // Given .env without MISSING key -> get(key,default) should return default
        Path envFile = tempDir.resolve(".env");
        try (PrintWriter pw = new PrintWriter(envFile.toFile())) {
            pw.println("OTHER=O");
        }
        DotenvBuilder builder = new DotenvBuilder().directory(tempDir.toString()).filename(".env");
        // When
        Dotenv dotenv = builder.load();
        // Then get returns default for missing
        assertEquals("def", dotenv.get("MISSING", "def"));
    }
}