package org.jsoup.parser;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.StringReader;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_1_Test {

    @Test
    @DisplayName("setTrackErrors enables tracking when maxErrors > 0 and isTrackErrors returns true")
    public void test_TC01() {
        // GIVEN a fresh HTML parser with default (no error tracking)
        Parser parser = Parser.htmlParser();
        // WHEN enabling error tracking with maxErrors=5 (>0 triggers tracking)
        parser.setTrackErrors(5);
        // THEN isTrackErrors should be true and getErrors().getMaxSize() should report 5
        assertTrue(parser.isTrackErrors(), "Expected tracking enabled when maxErrors>0");
        assertEquals(5, parser.getErrors().getMaxSize(), "Expected error list max size to match argument");
    }

    @Test
    @DisplayName("setTrackErrors disables tracking when maxErrors == 0 and isTrackErrors returns false")
    public void test_TC02() {
        // GIVEN a fresh XML parser with default (no error tracking)
        Parser parser = Parser.xmlParser();
        // WHEN disabling error tracking with maxErrors=0 (zero disables)
        parser.setTrackErrors(0);
        // THEN isTrackErrors should be false and getErrors().getMaxSize() should be 0
        assertFalse(parser.isTrackErrors(), "Expected tracking disabled when maxErrors==0");
        assertEquals(0, parser.getErrors().getMaxSize(), "Expected error list max size 0 when tracking off");
    }

    @Test
    @DisplayName("setTrackPosition toggles on when argument true and isTrackPosition returns true")
    public void test_TC03() {
        // GIVEN a fresh HTML parser with trackPosition default false
        Parser parser = Parser.htmlParser();
        // WHEN turning on position tracking
        parser.setTrackPosition(true);
        // THEN isTrackPosition should return true
        assertTrue(parser.isTrackPosition(), "Expected position tracking enabled when setTrackPosition(true)");
    }

    @Test
    @DisplayName("setTrackPosition toggles off when argument false and isTrackPosition returns false")
    public void test_TC04() {
        // GIVEN an HTML parser with position tracking already on
        Parser parser = Parser.htmlParser().setTrackPosition(true);
        // WHEN turning off position tracking
        parser.setTrackPosition(false);
        // THEN isTrackPosition should return false
        assertFalse(parser.isTrackPosition(), "Expected position tracking disabled when setTrackPosition(false)");
    }

    @Test
    @DisplayName("newInstance returns deep copy: changing original settings does not affect clone")
    public void test_TC05() {
        // GIVEN an HTML parser with errors tracking=3 and position tracking=true
        Parser orig = Parser.htmlParser().setTrackErrors(3).setTrackPosition(true);
        // WHEN creating a new instance and then mutating the original
        Parser clone = orig.newInstance();
        orig.setTrackErrors(1).setTrackPosition(false);
        // THEN the clone retains the settings at time of cloning (3 errors, position true)
        assertTrue(clone.isTrackErrors(), "Clone should retain original tracking enabled");
        assertEquals(3, clone.getErrors().getMaxSize(), "Clone should retain original error max size");
        assertTrue(clone.isTrackPosition(), "Clone should retain original position tracking state");
    }

    @Test
    @DisplayName("defaultNamespace returns HTML namespace for htmlParser")
    public void test_TC06() {
        // GIVEN an HTML parser
        Parser parser = Parser.htmlParser();
        // WHEN querying default namespace
        String ns = parser.defaultNamespace();
        // THEN should match the well-known HTML namespace
        assertEquals(Parser.NamespaceHtml, ns, "Expected HTML parser to return the HTML namespace");
    }

    @Test
    @DisplayName("defaultNamespace returns XML namespace for xmlParser")
    public void test_TC07() {
        // GIVEN an XML parser
        Parser parser = Parser.xmlParser();
        // WHEN querying default namespace
        String ns = parser.defaultNamespace();
        // THEN should match the well-known XML namespace
        assertEquals(Parser.NamespaceXml, ns, "Expected XML parser to return the XML namespace");
    }

    @Test
    @DisplayName("isContentForTagData returns true for 'script' and false for 'div' with HtmlTreeBuilder")
    public void test_TC08() {
        // GIVEN an HTML parser
        Parser parser = Parser.htmlParser();
        // WHEN checking content-for-tag-data for 'script' (should be treated as data) and 'div' (not data)
        boolean b1 = parser.isContentForTagData("script"); // script is in tagDataContent set
        boolean b2 = parser.isContentForTagData("div");    // div is normal tag
        // THEN script returns true, div returns false
        assertTrue(b1, "Expected 'script' to be treated as tag data");
        assertFalse(b2, "Expected 'div' not to be treated as tag data");
    }
}