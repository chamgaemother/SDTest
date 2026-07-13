package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.semver4j.Semver;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
public class Semver_parse_1_Test {

    @Test
    @DisplayName("parse(\"a.b.c\") returns null when non-numeric segments cause StrictParser to throw")
    void test_TC07() {
        // The input "a.b.c" has non-numeric segments, triggering a parse exception in StrictParser path B2→B4
        String version = "a.b.c";
        Semver result = Semver.parse(version);
        // Expect null due to invalid numeric format
        assertNull(result, "Expected null when parsing non-numeric version segments");
    }

    @Test
    @DisplayName("parse(\"1.2.3+build\") returns Semver with empty prerelease and single build token")
    void test_TC08() {
        // The input "1.2.3+build" follows valid semver, no prerelease, build metadata present, covers path B2→B3
        String version = "1.2.3+build";
        Semver result = Semver.parse(version);
        assertNotNull(result, "Expected non-null Semver for valid version with build metadata");
        // Pre-release list should be empty
        assertTrue(result.getPreRelease().isEmpty(), "Pre-release list should be empty for build-only version");
        // Build list should contain exactly one token: "build"
        assertEquals(Collections.singletonList("build"), result.getBuild(), "Build list should contain the single token 'build'");
    }

    @Test
    @DisplayName("parse(\"1.2.3-beta\") returns Semver with single prerelease and empty build")
    void test_TC09() {
        // The input "1.2.3-beta" has valid semantic version with one prerelease token and no build metadata, covers path B2→B3
        String version = "1.2.3-beta";
        Semver result = Semver.parse(version);
        assertNotNull(result, "Expected non-null Semver for valid version with prerelease metadata");
        // Pre-release list should contain exactly one token: "beta"
        assertEquals(Collections.singletonList("beta"), result.getPreRelease(), "Pre-release list should contain the single token 'beta'");
        // Build list should be empty
        assertTrue(result.getBuild().isEmpty(), "Build list should be empty for prerelease-only version");
    }

    @Test
    @DisplayName("parse(\"-1.2.3\") returns null when major version negative triggers parse exception")
    void test_TC10() {
        // The input "-1.2.3" has a negative major version, triggering StrictParser exception path B2→B4
        String version = "-1.2.3";
        Semver result = Semver.parse(version);
        // Expect null due to invalid negative major version
        assertNull(result, "Expected null when parsing version with negative major number");
    }
}