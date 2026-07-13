package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Semver_parse_2_Test {

    @Test
    @DisplayName("parse(\"1.2\") returns null when version has too few numeric identifiers")
    void test_TC13() {
        // GIVEN a version string with only major and minor components (missing patch)
        String version = "1.2";
        // WHEN attempting to parse
        Semver result = Semver.parse(version);
        // THEN result should be null because the patch component is missing (too few segments)
        assertNull(result, "Expected null when version has too few numeric identifiers");
    }

    @Test
    @DisplayName("parse(\"1.2.3.4\") returns null when version has too many numeric identifiers")
    void test_TC14() {
        // GIVEN a version string with major, minor, patch and an extra segment
        String version = "1.2.3.4";
        // WHEN attempting to parse
        Semver result = Semver.parse(version);
        // THEN result should be null because there are too many numeric identifiers (extra segment)
        assertNull(result, "Expected null when version has too many numeric identifiers");
    }
}