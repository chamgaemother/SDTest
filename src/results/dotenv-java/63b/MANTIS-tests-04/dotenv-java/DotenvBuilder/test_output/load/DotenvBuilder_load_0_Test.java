package io.github.cdimascio.dotenv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
public class DotenvBuilder_load_0_Test {

    @Test
    @DisplayName("TC01: load() with no env entries and systemProperties disabled exercises empty list loop (loop-0, branch-true)")
    void test_TC01() throws IOException, DotenvException {
        Path dir = Files.createTempDirectory("emptyEnvDir");
        Files.createFile(dir.resolve(".env"));
        DotenvBuilder builder = new DotenvBuilder().directory(dir.toString());
        io.github.cdimascio.dotenv.Dotenv d = builder.load(); // Fixed import
        assertTrue(d.entries().isEmpty(), "Expected no entries when .env is empty");
    }

    @Test
    @DisplayName("TC02: load() with one env entry and systemProperties disabled exercises single-element loop (loop-1, branch-true)")
    void test_TC02() throws IOException, DotenvException {
        Path dir = Files.createTempDirectory("singleEntryEnvDir");
        Path env = dir.resolve(".env");
        Files.write(env, "ONE=1\n".getBytes());
        DotenvBuilder builder = new DotenvBuilder().directory(dir.toString());
        io.github.cdimascio.dotenv.Dotenv d = builder.load(); // Fixed import
        Set<io.github.cdimascio.dotenv.DotenvEntry> entries = d.entries();
        assertEquals(1, entries.size(), "Expected exactly one entry");
        io.github.cdimascio.dotenv.DotenvEntry e = entries.iterator().next(); // Fixed import
        assertEquals("ONE", e.getKey());
        assertEquals("1", e.getValue());
    }

    @Test
    @DisplayName("TC03: load() with multiple env entries and systemProperties disabled exercises multi-element loop (loop-N, branch-true)")
    void test_TC03() throws IOException, DotenvException {
        Path dir = Files.createTempDirectory("multiEntryEnvDir");
        Path env = dir.resolve(".env");
        String content = String.join("\n", "A=alpha", "B=bravo", "C=charlie");
        Files.write(env, content.getBytes());
        DotenvBuilder builder = new DotenvBuilder().directory(dir.toString());
        io.github.cdimascio.dotenv.Dotenv d = builder.load(); // Fixed import
        Set<io.github.cdimascio.dotenv.DotenvEntry> entries = d.entries();
        assertEquals(3, entries.size(), "Expected three entries");
        assertTrue(entries.stream().anyMatch(e -> e.getKey().equals("A") && e.getValue().equals("alpha")));
        assertTrue(entries.stream().anyMatch(e -> e.getKey().equals("B") && e.getValue().equals("bravo")));
        assertTrue(entries.stream().anyMatch(e -> e.getKey().equals("C") && e.getValue().equals("charlie")));
    }

    @Test
    @DisplayName("TC04: load() with no env entries and systemProperties enabled skips loop (loop-0, branch-false)")
    void test_TC04() throws IOException, DotenvException {
        Path dir = Files.createTempDirectory("emptyEnvSysDir");
        Files.createFile(dir.resolve(".env"));
        DotenvBuilder builder = new DotenvBuilder().directory(dir.toString()).systemProperties();
        io.github.cdimascio.dotenv.Dotenv d = builder.load(); // Fixed import
        assertTrue(d.entries().isEmpty(), "Expected no entries even with systemProperties enabled");
    }

    @Test
    @DisplayName("TC05: load() with multiple env entries and systemProperties enabled skips loop but uses system env override (branch-false, loop skipped)")
    void test_TC05() throws IOException, DotenvException {
        String osKey = "PATH";
        String osVal = System.getenv(osKey);
        assumeTrue(osVal != null, "Environment must define PATH for this test");
        Path dir = Files.createTempDirectory("multiEnvSysDir");
        Path env = dir.resolve(".env");
        String content = osKey + "=FILE_VALUE" + "\n" + "X=Y";
        Files.write(env, content.getBytes());
        DotenvBuilder builder = new DotenvBuilder().directory(dir.toString()).systemProperties();
        io.github.cdimascio.dotenv.Dotenv d = builder.load(); // Fixed import
        assertEquals(osVal, d.get(osKey), "Expected get() to return actual system env override");
        assertEquals("Y", d.get("X"));
    }

    @Test
    @DisplayName("TC06: load() with missing .env and default throwIfMissing=true throws DotenvException (exception)")
    void test_TC06() {
        String nonExist = "nonexistent_dir_" + System.currentTimeMillis();
        DotenvBuilder builder = new DotenvBuilder().directory(nonExist);
        assertThrows(DotenvException.class,
            builder::load,
            "Expected DotenvException when .env file is missing");
    }

    @Test
    @DisplayName("TC07: load() with malformed .env and default throwIfMalformed=true throws DotenvException (exception)")
    void test_TC07() throws IOException {
        Path dir = Files.createTempDirectory("malformedEnvDir");
        Path env = dir.resolve(".env");
        Files.write(env, "BADLINE_WITHOUT_EQUALS".getBytes());
        DotenvBuilder builder = new DotenvBuilder().directory(dir.toString());
        assertThrows(DotenvException.class,
            builder::load,
            "Expected DotenvException when .env file is malformed");
    }
}