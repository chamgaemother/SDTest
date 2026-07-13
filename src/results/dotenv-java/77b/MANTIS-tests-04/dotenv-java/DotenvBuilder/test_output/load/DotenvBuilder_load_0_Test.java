package io.github.cdimascio.dotenv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class DotenvBuilder_load_0_Test {

    @Test
    @DisplayName("load returns DotenvImpl with one file entry when systemProperties=false (branch-false, loop-0)")
    void test_TC01() throws IOException, DotenvException {
        // GIVEN a temp directory with a .env file containing "KEY=VALUE"
        Path tmpDir = Files.createTempDirectory("dotenv_tc01");
        Path envFile = tmpDir.resolve(".env");
        Files.write(envFile, "KEY=VALUE".getBytes());

        // WHEN loading without setting systemProperties (so systemProperties=false means branch B0->B2)
        DotenvBuilder builder = new DotenvBuilder().directory(tmpDir.toString());
        Dotenv dt = builder.load();

        // THEN dt.get("KEY") == "VALUE" and no system property was set for "KEY"
        assertEquals("VALUE", dt.get("KEY"));
        assertNull(System.getProperty("KEY"));

        // cleanup
        Files.deleteIfExists(envFile);
        Files.deleteIfExists(tmpDir);
    }

    @Test
    @DisplayName("load sets system properties for multiple entries when systemProperties=true (branch-true, loop-N)")
    void test_TC02() throws IOException, DotenvException {
        // GIVEN a temp directory with .env lines "A=1" and "B=2"
        Path tmpDir = Files.createTempDirectory("dotenv_tc02");
        Path envFile = tmpDir.resolve(".env");
        Files.write(envFile, ("A=1\nB=2").getBytes());

        // Clear any existing properties
        System.clearProperty("A");
        System.clearProperty("B");

        // WHEN loading with systemProperties=true (branch B0->B1->B2)
        DotenvBuilder builder = new DotenvBuilder()
                .directory(tmpDir.toString())
                .systemProperties();
        Dotenv dt = builder.load();

        // THEN returned values match and system properties are set
        assertEquals("1", dt.get("A"));
        assertEquals("2", dt.get("B"));
        assertEquals("1", System.getProperty("A"));
        assertEquals("2", System.getProperty("B"));

        // cleanup
        System.clearProperty("A");
        System.clearProperty("B");
        Files.deleteIfExists(envFile);
        Files.deleteIfExists(tmpDir);
    }

    @Test
    @DisplayName("load throws DotenvException when .env is missing and throwIfMissing=true (exception)")
    void test_TC03() throws IOException {
        // GIVEN a temp directory without a .env file
        Path tmpDir = Files.createTempDirectory("dotenv_tc03");
        // WHEN loading with default throwIfMissing=true
        DotenvBuilder builder = new DotenvBuilder().directory(tmpDir.toString());

        // THEN a DotenvException is thrown
        assertThrows(DotenvException.class, builder::load);

        // cleanup
        Files.deleteIfExists(tmpDir);
    }

    @Test
    @DisplayName("load returns empty Dotenv when .env is missing and ignoreIfMissing() applied (branch-false, loop-0)")
    void test_TC04() throws IOException, DotenvException {
        // GIVEN a temp directory without .env
        Path tmpDir = Files.createTempDirectory("dotenv_tc04");

        // WHEN ignoreIfMissing() is applied (prevent exception) -> branch B0->B2
        DotenvBuilder builder = new DotenvBuilder()
                .directory(tmpDir.toString())
                .ignoreIfMissing();
        Dotenv dt = builder.load();

        // THEN entries() is empty
        assertTrue(dt.entries().isEmpty());

        // cleanup
        Files.deleteIfExists(tmpDir);
    }

    @Test
    @DisplayName("load throws DotenvException on malformed .env when throwIfMalformed=true (exception)")
    void test_TC05() throws IOException {
        // GIVEN a temp directory with .env containing an invalid line "INVALIDLINE"
        Path tmpDir = Files.createTempDirectory("dotenv_tc05");
        Path envFile = tmpDir.resolve(".env");
        Files.write(envFile, "INVALIDLINE".getBytes());

        // WHEN loading with default throwIfMalformed=true
        DotenvBuilder builder = new DotenvBuilder().directory(tmpDir.toString());

        // THEN a DotenvException is thrown due to malformed entry
        assertThrows(DotenvException.class, builder::load);

        // cleanup
        Files.deleteIfExists(envFile);
        Files.deleteIfExists(tmpDir);
    }

    @Test
    @DisplayName("load skips malformed entries and returns valid ones when ignoreIfMalformed() applied (branch-false, loop-0)")
    void test_TC06() throws IOException, DotenvException {
        // GIVEN a temp directory with .env containing "GOOD=1" and "BADLINE"
        Path tmpDir = Files.createTempDirectory("dotenv_tc06");
        Path envFile = tmpDir.resolve(".env");
        Files.write(envFile, ("GOOD=1\nBADLINE").getBytes());

        // WHEN ignoreIfMalformed() is applied -> malformed is skipped, branch B0->B2
        DotenvBuilder builder = new DotenvBuilder()
                .directory(tmpDir.toString())
                .ignoreIfMalformed();
        Dotenv dt = builder.load();

        // THEN only GOOD=1 is returned; BADLINE yields null
        assertEquals("1", dt.get("GOOD"));
        assertNull(dt.get("BADLINE"));

        // cleanup
        Files.deleteIfExists(envFile);
        Files.deleteIfExists(tmpDir);
    }

    @Test
    @DisplayName("load with ignoreIfMalformed() and systemProperties=true applies only valid entries to system properties (branch-true, loop-1)")
    void test_TC07() throws IOException, DotenvException {
        // GIVEN a temp directory with .env containing "X=Y" and "INVALID"
        Path tmpDir = Files.createTempDirectory("dotenv_tc07");
        Path envFile = tmpDir.resolve(".env");
        Files.write(envFile, ("X=Y\nINVALID").getBytes());

        // Clear any existing property
        System.clearProperty("X");

        // WHEN ignoreIfMalformed() and systemProperties() are applied -> branch B0->B1->B2
        DotenvBuilder builder = new DotenvBuilder()
                .directory(tmpDir.toString())
                .ignoreIfMalformed()
                .systemProperties();
        Dotenv dt = builder.load();

        // THEN only valid X=Y is in Dotenv and system property
        assertEquals("Y", dt.get("X"));
        assertEquals("Y", System.getProperty("X"));

        // cleanup
        System.clearProperty("X");
        Files.deleteIfExists(envFile);
        Files.deleteIfExists(tmpDir);
    }
}