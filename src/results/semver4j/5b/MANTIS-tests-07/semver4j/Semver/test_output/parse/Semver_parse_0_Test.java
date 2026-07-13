package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
public class Semver_parse_0_Test {

    @Test
    @DisplayName("parse(null) returns null when input is null (r0 == null branch)")
    void test_TC01() {
        // Input is null to trigger the initial null check branch (B0→B1→B7)
        String version = null;
        Semver result = Semver.parse(version);
        // Expect null as per specification when input is null
        assertNull(result, "parse(null) should return null");
    }

    @Test
    @DisplayName("parse(\"1.2.3\") returns Semver instance for valid semantic version (no exception)")
    void test_TC02() {
        // Simple well-formed version triggers successful parsing branch (B0→B2→B3→B4→B6)
        String version = "1.2.3";
        Semver result = Semver.parse(version);
        // Expect non-null Semver with correct parsed fields
        assertNotNull(result, "parse(\"1.2.3\") should not return null");
        assertEquals("1.2.3", result.getVersion(), "Version string should match input");
        assertEquals(1, result.getMajor(), "Major version should be 1");
        assertEquals(2, result.getMinor(), "Minor version should be 2");
        assertEquals(3, result.getPatch(), "Patch version should be 3");
        assertTrue(result.getPreRelease().isEmpty(), "Pre-release list should be empty");
        assertTrue(result.getBuild().isEmpty(), "Build list should be empty");
    }

    @Test
    @DisplayName("parse(\" 1.2.3-beta.4+build.5 \") returns Semver with pre-release and build tokens")
    void test_TC03() {
        // Leading/trailing whitespace triggers trim and successful parse (B0→B2→B3→B4→B6)
        String version = " 1.2.3-beta.4+build.5 ";
        Semver result = Semver.parse(version);
        // Expect non-null and correct decomposition of pre-release and build tokens
        assertNotNull(result, "parse trimmed version should not return null");
        assertEquals("1.2.3-beta.4+build.5", result.getVersion(), "Trimmed and formatted version string mismatch");
        assertEquals(
            Arrays.asList("beta", "4"),
            result.getPreRelease(),
            "Pre-release tokens should be [beta, 4]"
        );
        assertEquals(
            Arrays.asList("build", "5"),
            result.getBuild(),
            "Build tokens should be [build, 5]"
        );
    }

    @Test
    @DisplayName("parse(\"invalid-version\") returns null when parsing throws exception in constructor")
    void test_TC04() {
        // Malformed version triggers exception in constructor and fallback return null (B0→B2→B5→B7)
        String version = "invalid-version";
        Semver result = Semver.parse(version);
        // Expect null as per catch block when parsing fails
        assertNull(result, "parse of invalid format should return null");
    }
}