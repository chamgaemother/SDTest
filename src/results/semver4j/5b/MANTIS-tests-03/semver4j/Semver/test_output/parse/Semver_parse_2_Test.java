package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Semver_parse_2_Test {

    @Test
    @DisplayName("parse rejects pre-release numeric identifier with leading zeros causing StrictParser exception")
    void test_TC15() {
        // Input has pre-release numeric identifier "01" with leading zero, which is invalid per semver spec.
        String version = "1.2.3-alpha.01";
        Semver result = Semver.parse(version);
        // Expect null because numeric pre-release segments must not have leading zeros.
        assertNull(result, "Expected parse to return null for pre-release numeric identifier with leading zeros");
    }

    @Test
    @DisplayName("parse rejects version with trailing plus and no build metadata")
    void test_TC16() {
        // Input ends with '+', but has no build metadata tokens after it, invalid per semver spec.
        String version = "1.2.3+";
        Semver result = Semver.parse(version);
        // Expect null because there are no build tokens following the '+'.
        assertNull(result, "Expected parse to return null for trailing '+' with no build metadata");
    }

    @Test
    @DisplayName("parse rejects version with trailing hyphen and no pre-release metadata")
    void test_TC17() {
        // Input ends with '-', but no pre-release identifiers follow, invalid per semver spec.
        String version = "1.2.3-";
        Semver result = Semver.parse(version);
        // Expect null because there are no pre-release tokens after the hyphen.
        assertNull(result, "Expected parse to return null for trailing '-' with no pre-release metadata");
    }
}