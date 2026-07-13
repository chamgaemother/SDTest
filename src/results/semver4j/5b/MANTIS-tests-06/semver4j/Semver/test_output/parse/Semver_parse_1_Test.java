package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.semver4j.Semver;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Semver_parse_1_Test {

    @Test
    @DisplayName("parse accepts a version string with pre-release and build metadata")
    void test_TC05() {
        // The input contains both pre-release and build metadata, so parse should follow B0→B2 (non-null) →B3 (no exception) →B4 (set preRelease) →B5 (set build)
        String version = "1.2.3-beta.4+meta.5";

        Semver result = Semver.parse(version);

        // We expect a non-null Semver object because the version string is well-formed
        assertNotNull(result, "Expected parse to return a Semver instance for a valid version string with metadata");

        // getVersion should reconstruct the original version including pre-release and build
        assertEquals("1.2.3-beta.4+meta.5", result.getVersion(),
                "The Semver#getVersion() should include both pre-release and build metadata");

        // getPreRelease should return the tokens ["beta","4"]
        List<String> expectedPre = Arrays.asList("beta", "4");
        assertEquals(expectedPre, result.getPreRelease(),
                "The pre-release tokens should match ['beta','4']");

        // getBuild should return the tokens ["meta","5"]
        List<String> expectedBuild = Arrays.asList("meta", "5");
        assertEquals(expectedBuild, result.getBuild(),
                "The build metadata tokens should match ['meta','5']");
    }
}