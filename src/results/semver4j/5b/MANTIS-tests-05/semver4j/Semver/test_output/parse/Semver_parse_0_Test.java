package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
public class Semver_parse_0_Test {

    @Test
    @DisplayName("parse(null) returns null when input is null (r0 == null branch)")
    void test_TC01() {
        // Input is null to cover the branch where version == null and returns null
        String version = null;
        Semver result = Semver.parse(version);
        assertNull(result, "Expected null when parsing a null input");
    }

    @Test
    @DisplayName("parse(\"\") returns null when trimmed input is empty and invalid format triggers exception")
    void test_TC02() {
        // Input is empty string to cover branch where version.trim() yields empty and StrictParser.parse throws
        String version = "";
        Semver result = Semver.parse(version);
        assertNull(result, "Expected null when parsing an empty string");
    }

    @Test
    @DisplayName("parse(\"1.2.3\") returns Semver instance for valid simple version")
    void test_TC03() {
        // Input is a valid simple semver to cover normal parsing path without exception
        String version = "1.2.3";
        Semver result = Semver.parse(version);
        assertNotNull(result, "Expected non-null Semver for a valid version");
        assertEquals("1.2.3", result.getVersion(), "Parsed version must match the input without whitespace");
    }

    @Test
    @DisplayName("parse(\" 1.2.3 \u0020\") trims whitespace and returns valid Semver")
    void test_TC04() {
        // Input contains leading/trailing spaces to cover trimming logic before parsing
        String version = " 1.2.3 ";
        Semver result = Semver.parse(version);
        assertNotNull(result, "Expected non-null Semver after trimming whitespace");
        assertEquals("1.2.3", result.getVersion(), "Parsed version must ignore surrounding whitespace");
    }

    @Test
    @DisplayName("parse(\"1.2.3-alpha.1+build.5\") returns Semver with pre-release and build tokens")
    void test_TC05() {
        // Input has pre-release and build metadata to cover both lists being populated
        String version = "1.2.3-alpha.1+build.5";
        Semver result = Semver.parse(version);
        assertNotNull(result, "Expected non-null Semver for version with pre-release and build");
        assertEquals(Arrays.asList("alpha", "1"), result.getPreRelease(), "Pre-release tokens must match the input");
        assertEquals(Arrays.asList("build", "5"), result.getBuild(), "Build metadata tokens must match the input");
    }

    @Test
    @DisplayName("parse(\"1.2\") returns null on invalid semver format (missing patch)")
    void test_TC06() {
        // Input missing patch number to cover exception path in StrictParser.parse
        String version = "1.2";
        Semver result = Semver.parse(version);
        assertNull(result, "Expected null when parsing a version missing the patch component");
    }
}