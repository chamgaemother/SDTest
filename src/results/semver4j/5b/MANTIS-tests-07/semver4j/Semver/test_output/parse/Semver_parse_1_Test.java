package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Collections;
import org.semver4j.Semver;
public class Semver_parse_1_Test {

    @Test
    @DisplayName("parse(\"\") returns null when input is empty after trim (r0 == null branch)")
    void test_TC05() {
        // Given an empty string which becomes empty after trim, triggering the null return in parse
        String version = "";
        // When
        Semver result = Semver.parse(version);
        // Then
        assertNull(result, "Expected parse(\"\") to return null for empty input");
    }

    @Test
    @DisplayName("parse(\"1.2.3-alpha\") returns Semver with only pre-release tokens and no build")
    void test_TC06() {
        // Given a version containing a prerelease part but no build part
        String version = "1.2.3-alpha";
        // When
        Semver result = Semver.parse(version);
        // Then
        assertNotNull(result, "Expected parse(\"1.2.3-alpha\") to return non-null Semver");
        assertEquals("1.2.3-alpha", result.getVersion(), "Version string should include prerelease but no build");
        assertEquals(Collections.singletonList("alpha"), result.getPreRelease(),
                     "PreRelease list should contain exactly 'alpha'");
        assertTrue(result.getBuild().isEmpty(), "Build list should be empty when no build token is present");
    }

    @Test
    @DisplayName("parse(\"1.2.3+build123\") returns Semver with only build tokens and no pre-release")
    void test_TC07() {
        // Given a version containing a build part but no prerelease part
        String version = "1.2.3+build123";
        // When
        Semver result = Semver.parse(version);
        // Then
        assertNotNull(result, "Expected parse(\"1.2.3+build123\") to return non-null Semver");
        assertEquals("1.2.3+build123", result.getVersion(), "Version string should include build but no prerelease");
        assertEquals(Collections.singletonList("build123"), result.getBuild(),
                     "Build list should contain exactly 'build123'");
        assertTrue(result.getPreRelease().isEmpty(), "PreRelease list should be empty when no prerelease token is present");
    }
}