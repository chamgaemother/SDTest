package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Semver_parse_1_Test {

    @Test
    @DisplayName("parse(\"abc\") returns null when input is non–numeric and fails StrictParser")
    void test_TC05() {
        // Input "abc" has no numeric segments → hits StrictParser exception path → parse should return null
        String version = "abc";
        Semver result = Semver.parse(version);
        assertNull(result, "Expected null when parsing non-numeric version");
    }

    @Test
    @DisplayName("parse(\"1.2\") returns null when missing patch component")
    void test_TC06() {
        // Input "1.2" has only major and minor, missing patch → StrictParser should throw → parse returns null
        String version = "1.2";
        Semver result = Semver.parse(version);
        assertNull(result, "Expected null when parsing version missing patch");
    }

    @Test
    @DisplayName("parse(\"1.2.3.4\") returns null when too many numeric segments")
    void test_TC07() {
        // Input "1.2.3.4" has four numeric segments rather than three → StrictParser should throw → parse returns null
        String version = "1.2.3.4";
        Semver result = Semver.parse(version);
        assertNull(result, "Expected null when parsing version with too many segments");
    }
}