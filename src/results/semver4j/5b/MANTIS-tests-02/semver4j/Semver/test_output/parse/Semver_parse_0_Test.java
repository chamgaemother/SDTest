package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Semver.parse based on specified scenarios.
 */
public class Semver_parse_0_Test {

    @Test
    @DisplayName("parse(null) returns null when version is null (r0 == null)")
    public void test_TC01() {
        // null input triggers the initial null check branch (B0→B1→B2)
        String version = null;
        Semver result = Semver.parse(version);
        assertNull(result, "Expected null when parsing a null version");
    }

    @Test
    @DisplayName("parse(\"\") returns null when trimmed string is empty and parser throws")
    public void test_TC02() {
        // empty string trimmed to empty -> parser should throw and catch returns null (B0→B1→B3→B6→B7)
        String version = "";
        Semver result = Semver.parse(version);
        assertNull(result, "Expected null when parsing an empty string");
    }

    @Test
    @DisplayName("parse(\"  \" ) returns null when trimmed whitespace yields empty string and parser throws")
    public void test_TC03() {
        // whitespace-only trimmed to empty triggers parser exception path (B0→B1→B3→B6→B7)
        String version = "   ";
        Semver result = Semver.parse(version);
        assertNull(result, "Expected null when parsing a whitespace-only string");
    }

    @Test
    @DisplayName("parse(\"1.2\") returns null when version missing patch and parser throws")
    public void test_TC04() {
        // invalid format missing patch -> parser should throw and return null (B0→B1→B3→B6→B7)
        String version = "1.2";
        Semver result = Semver.parse(version);
        assertNull(result, "Expected null when parsing an invalid semver without patch");
    }

    @Test
    @DisplayName("parse(\"1.2.3\") returns a Semver object for a valid simple version")
    public void test_TC05() {
        // valid simple version -> successful parse path (B0→B1→B3→B4→B5)
        String version = "1.2.3";
        Semver result = Semver.parse(version);
        assertNotNull(result, "Expected non-null Semver for valid version");
        assertEquals("1.2.3", result.getVersion(), "Parsed version should match original without extra delimiters");
    }

    @Test
    @DisplayName("parse(\"  1.2.3-beta.4+exp.sha  \" ) returns Semver with pre-release and build after trimming")
    public void test_TC06() {
        // input with whitespace, pre-release and build should be trimmed and parsed (B0→B1→B3→B4→B5)
        String version = "  1.2.3-beta.4+exp.sha  ";
        Semver result = Semver.parse(version);
        assertNotNull(result, "Expected non-null Semver for complex version with pre-release and build");
        assertEquals("1.2.3-beta.4+exp.sha", result.getVersion(), "Parsed version should trim whitespace and preserve prerelease and build");
    }
}