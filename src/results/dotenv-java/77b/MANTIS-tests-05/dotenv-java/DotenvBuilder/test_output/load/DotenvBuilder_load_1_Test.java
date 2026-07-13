package io.github.cdimascio.dotenv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Arrays;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
public class DotenvBuilder_load_1_Test {

    /**
     * Test that entries(filter) with a non-null filter returns only those entries originally read from file.
     */
    @Test
    @DisplayName("entries(filter) with non-null filter returns only file entries")
    public void test_TC07() throws Exception {
        // Arrange stub to return two entries via DotenvParserStubControl
        DotenvParserStubControl.stubList = Arrays.asList(
                new DotenvEntry("X", "1"),
                new DotenvEntry("Y", "2")
        );
        DotenvParserStubControl.toThrow = null;

        DotenvBuilder builder = new DotenvBuilder();
        Dotenv dotenv = builder.load();

        // Create a non-null filter: always true
        Dotenv.Filter alwaysTrue = Dotenv.Filter.ALWAYS_TRUE;
        // Act
        Set<DotenvEntry> filtered = dotenv.entries(alwaysTrue);

        // Assert: only the two stub entries are returned, size = 2, unmodifiable
        assertEquals(2, filtered.size(), "Should return exactly the 2 entries from file");
        assertTrue(filtered.containsAll(DotenvParserStubControl.stubList),
                "Returned set should contain all stub entries");
        // Verify unmodifiable: mutation should throw
        assertThrows(UnsupportedOperationException.class, () -> filtered.add(new DotenvEntry("Z","3")));
    }

    /**
     * Test that get(key, default) returns the user-provided default when the key is missing in both system and file.
     */
    @Test
    @DisplayName("get(key, default) returns user default when key missing in both system and file")
    public void test_TC08() throws Exception {
        // Arrange stub to return empty list
        DotenvParserStubControl.stubList = Collections.emptyList();
        DotenvParserStubControl.toThrow = null;

        // Ensure no stray system property
        System.clearProperty("MISSING");

        DotenvBuilder builder = new DotenvBuilder();
        Dotenv dotenv = builder.load();

        // Act
        String result = dotenv.get("MISSING", "DEF");

        // Assert: missing key returns provided default
        assertEquals("DEF", result,
                "Should return default value when key missing in both env and file");
    }

    /**
     * Test that get(key, default) returns the file value when key is present in file and not set as system env.
     */
    @Test
    @DisplayName("get(key, default) returns file value when key present and system getenv returns null")
    public void test_TC09() throws Exception {
        // Arrange stub to return one entry ("K","V")
        DotenvParserStubControl.stubList = Collections.singletonList(new DotenvEntry("K", "V"));
        DotenvParserStubControl.toThrow = null;

        // Ensure no real system property for K
        System.clearProperty("K");

        DotenvBuilder builder = new DotenvBuilder();
        Dotenv dotenv = builder.load();

        // Act
        String result = dotenv.get("K", "DEF");

        // Assert: file value takes precedence over default when not in system env
        assertEquals("V", result,
                "Should return the file-provided value when key present in file and not in system env");
    }
}