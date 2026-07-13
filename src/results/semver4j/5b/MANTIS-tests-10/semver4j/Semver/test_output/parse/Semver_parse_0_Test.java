package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.semver4j.Semver;

import static org.junit.jupiter.api.Assertions.*;
public class Semver_parse_0_Test {

    @Test
    @DisplayName("parse(null) returns null when input version is null (branch version==null)")
    void test_TC01() {
        // GIVEN a null version input -> triggers the version==null branch
        String version = null;
        // WHEN parsing
        Semver result = Semver.parse(version);
        // THEN expect null result
        assertNull(result, "Expected parse(null) to return null when version is null");
    }

    @Test
    @DisplayName("parse(\" 1.2.3 \") returns a Semver when input is valid after trim (branch version!=null, no exception)")
    void test_TC02() {
        // GIVEN a version string with surrounding whitespace -> version!=null, Trim removes spaces, StrictParser should accept "1.2.3"
        String version = " 1.2.3 ";
        // WHEN parsing
        Semver result = Semver.parse(version);
        // THEN expect non-null Semver and version normalized to "1.2.3"
        assertNotNull(result, "Expected parse to return a non-null Semver for a valid version string after trim");
        assertEquals("1.2.3", result.getVersion(), "Expected normalized version to be '1.2.3'");
    }

    @Test
    @DisplayName("parse(\"not-a-version\") returns null when StrictParser.parse throws (branch version!=null, exception path)")
    void test_TC03() {
        // GIVEN an invalid version string -> version!=null, StrictParser.parse should throw and be caught
        String version = "not-a-version";
        // WHEN parsing
        Semver result = Semver.parse(version);
        // THEN expect null result since parsing fails
        assertNull(result, "Expected parse to return null for an invalid version string");
    }
}