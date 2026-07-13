package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Semver_parse_2_Test {

    @Test
    @DisplayName("parse(\"01.002.0003\") normalizes leading zeros and returns Semver(1.2.3)")
    void test_TC08() {
        // Given: version string with leading zeros triggers normalization branch
        String version = "01.002.0003";
        // When: parsing valid semver with leading zeros
        Semver result = Semver.parse(version);
        // Then: result should not be null and normalized to "1.2.3"
        assertNotNull(result, "Expected non-null Semver object for normalized version");
        assertEquals("1.2.3", result.getVersion(), "Version string should be normalized without leading zeros");
        assertEquals(1, result.getMajor(), "Major version should be 1 after normalization");
        assertEquals(2, result.getMinor(), "Minor version should be 2 after normalization");
        assertEquals(3, result.getPatch(), "Patch version should be 3 after normalization");
    }

    @Test
    @DisplayName("parse(\"v1.2.3\") returns null when non-numeric prefix causes parse exception")
    void test_TC09() {
        // Given: version string with non-numeric prefix triggers exception in StrictParser.parse
        String version = "v1.2.3";
        // When: parsing invalid semver string
        Semver result = Semver.parse(version);
        // Then: result should be null due to parse failure
        assertNull(result, "Expected null for version strings with non-numeric prefix");
    }
}