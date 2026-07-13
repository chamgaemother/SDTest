package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_2_Test {

    @Test
    @DisplayName("htmlParser().tagSet() returns default lowercase HTML tag names (HTML branch)")
    public void test_TC01() {
        // GIVEN a HTML parser to exercise the HTML branch (path B0→B1→B3→B5)
        Parser parser = Parser.htmlParser();
        // WHEN retrieving its tagSet
        Set<String> tags = parser.tagSet(); // Changed back to tagSet()
        // THEN we expect standard HTML tags in lowercase
        assertTrue(tags.contains("div"), "Expected 'div' in default HTML tag set");
        assertTrue(tags.contains("span"), "Expected 'span' in default HTML tag set");
        assertTrue(tags.stream().allMatch(s -> s.equals(s.toLowerCase())),
                "All tags should be lowercase to satisfy HTML branch output");
    }

    @Test
    @DisplayName("xmlParser().tagSet() returns an empty set (XML branch)")
    public void test_TC02() {
        // GIVEN an XML parser to exercise the XML branch (path B0→B2→B4→B5)
        Parser parser = Parser.xmlParser();
        // WHEN retrieving its tagSet
        Set<String> tags = parser.tagSet(); // Changed back to tagSet()
        // THEN we expect no tags, because XML parser does not maintain a default HTML tag set
        assertNotNull(tags, "tagSet should not be null even for XML parser");
        assertTrue(tags.isEmpty(), "Expected empty tagSet for XML parser");
    }

    @Test
    @DisplayName("Custom case-sensitive settings preserve uppercase tag names in tagSet (settings branch)")
    public void test_TC03() {
        // GIVEN a HTML parser with custom case-preserve settings (path B0→B1→B3→B6→B5)
        Parser parser = Parser.htmlParser().settings(ParseSettings.custom()); // Changed to use custom settings
        // Manually add an uppercase tag to settings to verify preservation
        parser.settings().tagSet().add("MyTag");
        // WHEN retrieving its tagSet
        Set<String> tags = parser.tagSet(); // Changed back to tagSet()
        // THEN we expect the manually added uppercase tag to appear
        assertTrue(tags.contains("MyTag"), "Expected 'MyTag' preserved in tagSet under preserveTagCase settings");
    }

    @Test
    @DisplayName("Clone of parser retains its tagSet settings (clone/ newInstance branch)")
    public void test_TC04() {
        // GIVEN an HTML parser with a custom tag added (path B0→B1→B3→B6→B7→B5)
        Parser original = Parser.htmlParser();
        original.settings().tagSet().add("custom");
        // WHEN cloning the parser
        Parser clone = original.clone();
        Set<String> tags = clone.tagSet(); // Changed back to tagSet()
        // THEN the cloned parser should retain the custom tag and match the original
        assertTrue(tags.contains("custom"), "Cloned parser should contain the custom tag");
        assertEquals(original.tagSet(), tags, "Cloned parser tagSet should be equal to the original's tagSet"); // Changed back to tagSet()
    }
}