package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.HtmlTreeBuilder;
import org.jsoup.parser.XmlTreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_1_Test {

    @Test
    @DisplayName("TC01: settings(ParseSettings) accepts a non‐default ParseSettings and returns this instance")
    public void test_TC01() {
        // GIVEN: a new HTML parser with default settings from HtmlTreeBuilder
        Parser parser = Parser.htmlParser();
        ParseSettings custom = new ParseSettings(false, false);
        // WHEN: setting a non-default custom settings should update and return same parser
        Parser returned = parser.settings(custom);
        // THEN: returned parser is same instance, and settings() returns the exact custom object
        assertSame(parser, returned, "settings(ParseSettings) should return the same parser instance for chaining");
        assertSame(custom, parser.settings(), "settings() should return the custom settings instance that was set");
    }

    @Test
    @DisplayName("TC02: settings(ParseSettings) accepts null and the parser.settings() returns null")
    public void test_TC02() {
        // GIVEN: a new XML parser
        Parser parser = Parser.xmlParser();
        // WHEN: setting null should be allowed without exception
        Parser returned = parser.settings(null);
        // THEN: returned parser is same instance, and settings() returns null
        assertSame(parser, returned, "settings(null) should return the same parser instance");
        assertNull(parser.settings(), "settings() should return null when null is set");
    }

    @Test
    @DisplayName("TC03: settings() returns initial default settings from constructor")
    public void test_TC03() {
        // GIVEN: a fresh Parser constructed with a fresh HtmlTreeBuilder
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        // WHEN: we retrieve the default settings without setting anything
        ParseSettings initial = parser.settings();
        // THEN: initial settings should equal the default settings from the provided tree builder
        assertEquals(tb.defaultSettings(), initial, "settings() should return the builder's default settings");
    }
}