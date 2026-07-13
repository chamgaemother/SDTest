package org.jsoup.parser;

import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
public class Tag_isFormSubmittable_2_Test {

    @Test
    @DisplayName("TC07: valueOf(null, namespace) throws IllegalArgumentException on null tagName")
    void test_TC07() {
        // GIVEN a null tagName to drive the Validate.notNull(tagName) path (B0→B1)
        String tagName = null;
        ParseSettings settings = ParseSettings.preserveRelativeLinks; // Corrected to use ParseSettings

        // WHEN / THEN expect IllegalArgumentException due to null tagName validation
        assertThrows(IllegalArgumentException.class, () -> {
            Tag.valueOf(tagName, settings);
        });
    }

    @Test
    @DisplayName("TC08: valueOf(\"  \", settings) throws IllegalArgumentException on empty tagName after trim")
    void test_TC08() {
        // GIVEN a blank tagName ("  ") to pass notNull but fail notEmpty after trim (B0→B1→B2)
        String tagName = "  ";
        ParseSettings settings = ParseSettings.preserveRelativeLinks; // Corrected to use ParseSettings

        // WHEN / THEN expect IllegalArgumentException due to empty tagName validation
        assertThrows(IllegalArgumentException.class, () -> {
            Tag.valueOf(tagName, settings);
        });
    }
}