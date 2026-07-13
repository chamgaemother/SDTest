package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Semver_parse_2_Test {

    @Test
    @DisplayName("parse(\"1.2.3-alpha.beta.gamma+build.meta.data\") returns Semver with multiple prerelease and build tokens")
    void test_TC11() {
        // This input includes both prerelease (with multiple dot-separated tokens) and build metadata.
        String version = "1.2.3-alpha.beta.gamma+build.meta.data";
        Semver result = Semver.parse(version);

        // parse should succeed (not return null) taking the prerelease and build sections
        assertNotNull(result, "Expected non-null Semver for valid version string");
        // Expecting prerelease tokens ["alpha", "beta", "gamma"]
        List<String> expectedPre = Arrays.asList("alpha", "beta", "gamma");
        assertEquals(expectedPre, result.getPreRelease(),
                "Prerelease tokens should be split on dots into " + expectedPre);
        // Expecting build tokens ["build", "meta", "data"]
        List<String> expectedBuild = Arrays.asList("build", "meta", "data");
        assertEquals(expectedBuild, result.getBuild(),
                "Build tokens should be split on dots into " + expectedBuild);
    }

    @Test
    @DisplayName("parse(\"1.2.3+\") returns Semver with empty prerelease and single empty build token")
    void test_TC12() {
        // The version has a trailing '+' with no build data: yields one empty build token per spec.
        String version = "1.2.3+";
        Semver result = Semver.parse(version);

        // parse should succeed (not return null) even with empty build section
        assertNotNull(result, "Expected non-null Semver even when build is empty");
        // No prerelease section present => empty list
        assertTrue(result.getPreRelease().isEmpty(),
                "Prerelease list should be empty when no prerelease section");
        // Build with single empty token: [""] 
        List<String> expectedBuild = Collections.singletonList("");
        assertEquals(expectedBuild, result.getBuild(),
                "Build list should contain a single empty string token");
    }
}