package org.semver4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Semver_parse_1_Test {

    @Test
    @DisplayName("parse(\"1.2.3-beta.4+sha98450956\") returns non-null Semver covering version!=null and no-exception path with pre-release and build tokens")
    void test_TC04() {
        // Given: a valid semver string containing pre-release and build metadata
        String version = "1.2.3-beta.4+sha98450956";
        // When: parsing the version should follow path B0 (input non-null) -> B2 (no exception thrown)
        Semver result = Semver.parse(version);
        // Then: result is not null and its version matches the original, covering B3 return branch
        assertNotNull(result, "Expected non-null Semver object for a valid version string");
        assertEquals("1.2.3-beta.4+sha98450956", result.getVersion(),
                "Parsed Semver version should preserve pre-release and build metadata");
    }
}