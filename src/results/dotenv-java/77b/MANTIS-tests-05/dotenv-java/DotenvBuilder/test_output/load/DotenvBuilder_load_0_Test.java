package io.github.cdimascio.dotenv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

// Added import for DotenvEntry class
import io.github.cdimascio.dotenv.DotenvEntry;
// Added import for DotenvParser class
import io.github.cdimascio.dotenv.DotenvParser;

public class DotenvBuilder_load_0_Test {

    private static class DotenvParserStubControl {
        static List<DotenvEntry> stubList;
        static RuntimeException toThrow;
    }

    static {
        try {
            Method realParse = DotenvParser.class.getDeclaredMethod("parse");
            realParse.setAccessible(true);
        } catch (NoSuchMethodException e) {
            // ignore
        }
    }

    @Test
    @DisplayName("TC01: load() with default systemProperties=false and empty env list invokes no iterations and returns DotenvImpl")
    void test_TC01() throws Exception {
        DotenvParserStubControl.stubList = Collections.emptyList();
        DotenvParserStubControl.toThrow = null;
        DotenvBuilder builder = new DotenvBuilder();
        Dotenv result = builder.load();
        assertTrue(result.entries().isEmpty(), "Expected no entries on empty stub list");
        assertTrue(result.entries(null).isEmpty(), "entries(null) should also be empty");
        assertNull(System.getProperty("ANY"), "No system property should be set");
    }

    @Test
    @DisplayName("TC02: load() with default systemProperties=false and single entry invokes one iteration for systemProperties=false")
    void test_TC02() throws Exception {
        DotenvEntry single = new DotenvEntry("KEY", "VALUE");
        DotenvParserStubControl.stubList = Collections.singletonList(single);
        DotenvParserStubControl.toThrow = null;
        DotenvBuilder builder = new DotenvBuilder();
        Dotenv result = builder.load();
        Set<DotenvEntry> entries = result.entries();
        assertEquals(1, entries.size(), "Expected exactly one entry");
        assertTrue(entries.contains(single), "Entry KEY=VALUE should be present");
        assertNull(System.getProperty("KEY"), "System property should not be set when systemProperties=false");
    }

    @Test
    @DisplayName("TC03: load() with default systemProperties=false and multiple entries invokes multiple iterations")
    void test_TC03() throws Exception {
        DotenvEntry e1 = new DotenvEntry("A", "1");
        DotenvEntry e2 = new DotenvEntry("B", "2");
        DotenvEntry e3 = new DotenvEntry("C", "3");
        DotenvParserStubControl.stubList = Arrays.asList(e1, e2, e3);
        DotenvParserStubControl.toThrow = null;
        DotenvBuilder builder = new DotenvBuilder();
        Dotenv result = builder.load();
        Set<DotenvEntry> entries = result.entries();
        assertEquals(3, entries.size(), "Expected three entries from stub list");
        assertTrue(entries.containsAll(Arrays.asList(e1, e2, e3)), "All stub entries should be present");
    }

    @Test
    @DisplayName("TC04: load() with systemProperties=true and empty env list skips forEach and returns DotenvImpl")
    void test_TC04() throws Exception {
        DotenvParserStubControl.stubList = Collections.emptyList();
        DotenvParserStubControl.toThrow = null;
        DotenvBuilder builder = new DotenvBuilder().systemProperties();
        Dotenv result = builder.load();
        assertTrue(result.entries().isEmpty(), "Expected no entries on empty stub list");
        assertNull(System.getProperty("ANY"), "No system property should be set even if systemProperties=true with empty list");
    }

    @Test
    @DisplayName("TC05: load() with systemProperties=true and one entry sets system property")
    void test_TC05() throws Exception {
        DotenvEntry entry = new DotenvEntry("A", "1");
        DotenvParserStubControl.stubList = Collections.singletonList(entry);
        DotenvParserStubControl.toThrow = null;
        DotenvBuilder builder = new DotenvBuilder().systemProperties();
        Dotenv result = builder.load();
        assertEquals(1, result.entries().size(), "Expected one entry in result");
        assertEquals("1", System.getProperty("A"), "System property 'A' should be set to '1'");
    }

    @Test
    @DisplayName("TC06: load() propagates DotenvException when parse() fails")
    void test_TC06() {
        DotenvParserStubControl.stubList = null;
        DotenvParserStubControl.toThrow = new DotenvException("fail");
        DotenvBuilder builder = new DotenvBuilder();
        DotenvException ex = assertThrows(DotenvException.class, builder::load, "Expected DotenvException on parse failure");
        assertEquals("fail", ex.getMessage(), "Exception message should propagate the stubbed message");
    }
}