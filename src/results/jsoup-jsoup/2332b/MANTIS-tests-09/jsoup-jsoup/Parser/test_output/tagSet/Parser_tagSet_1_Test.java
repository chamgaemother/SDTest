package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_1_Test {

    @Test
    @DisplayName("setTrackErrors with maxErrors>0 enables tracking and isTrackErrors returns true")
    public void test_TC01() {
        // GIVEN a new HTML parser, default errors tracking is disabled
        Parser parser = Parser.htmlParser();
        // WHEN enabling error tracking with maxErrors 5 (branch: maxErrors>0 path)
        parser.setTrackErrors(5);
        // THEN isTrackErrors should be true and getErrors().getMaxSize() should reflect 5
        assertTrue(parser.isTrackErrors(), "Error tracking should be enabled when maxErrors > 0");
        assertEquals(5, parser.getErrors().getMaxSize(), "Max tracked errors should equal the passed value");
    }

    @Test
    @DisplayName("setTrackErrors with maxErrors=0 disables tracking and isTrackErrors returns false")
    public void test_TC02() {
        // GIVEN a new XML parser, default errors tracking is disabled
        Parser parser = Parser.xmlParser();
        // WHEN disabling error tracking with maxErrors 0 (branch: maxErrors==0 path)
        parser.setTrackErrors(0);
        // THEN isTrackErrors should be false and getErrors().getMaxSize() should be 0
        assertFalse(parser.isTrackErrors(), "Error tracking should be disabled when maxErrors == 0");
        assertEquals(0, parser.getErrors().getMaxSize(), "Max tracked errors should be zero when disabled");
    }

    @Test
    @DisplayName("setTrackPosition(true) sets trackPosition and isTrackPosition returns true")
    public void test_TC03() {
        // GIVEN a new Parser with an HtmlTreeBuilder, default trackPosition is false
        Parser parser = new Parser(new HtmlTreeBuilder());
        // WHEN setting trackPosition to true (branch: trackPosition==true path)
        parser.setTrackPosition(true);
        // THEN isTrackPosition should return true
        assertTrue(parser.isTrackPosition(), "Position tracking should be enabled when set to true");
    }

    @Test
    @DisplayName("settings(ParseSettings) updates parser settings and settings() returns the new object")
    public void test_TC04() {
        // GIVEN a new HTML parser and a custom ParseSettings instance
        Parser parser = Parser.htmlParser();
        ParseSettings custom = new ParseSettings(true, true);
        // WHEN applying custom settings (branch: settings updated path)
        parser.settings(custom);
        // THEN settings() should return exactly the custom instance
        assertSame(custom, parser.settings(), "Parser should retain the exact settings instance passed in");
    }

    @Test
    @DisplayName("defaultNamespace on xmlParser returns XML namespace")
    public void test_TC05() {
        // GIVEN a new XML parser (uses XmlTreeBuilder whose defaultNamespace is NamespaceXml)
        Parser parser = Parser.xmlParser();
        // WHEN querying the default namespace (branch: XmlTreeBuilder path)
        String ns = parser.defaultNamespace();
        // THEN it should equal the XML namespace constant
        assertEquals(Parser.NamespaceXml, ns, "XML parser should return the XML namespace constant");
    }
}