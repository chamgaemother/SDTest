package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(String, String, Parser) throws NullPointerException when parser is null")
    public void test_TC22() {
        // Given a null parser to the parse(html, baseUri, parser) overload -> should throw NPE before parsing
        String html = "<p>X</p>";
        String baseUri = "http://a/";
        Parser parser = null;
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, baseUri, parser));
    }

    @Test
    @DisplayName("parse(String, Parser) throws NullPointerException when parser is null")
    public void test_TC23() {
        // Given a null parser to the parse(html, parser) overload -> should throw NPE before invoking parseInput
        String html = "<b>Y</b>";
        Parser parser = null;
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, parser));
    }

    @Test
    @DisplayName("clean(bodyHtml, Safelist) returns escaped HTML when preserveRelativeLinks is false")
    public void test_TC24() {
        // Using Safelist.none() which escapes entities, verify '<' is escaped to '&lt;'
        String html = "<p>5 is < 6</p>";
        Safelist safelist = Safelist.none();
        String result = Jsoup.clean(html, safelist);
        assertTrue(result.contains("5 is &lt; 6"), "Expected escaped '<' entity in result");
    }

    @Test
    @DisplayName("clean(bodyHtml, \"\", Safelist) returns unmodified HTML when preserveRelativeLinks is true and baseUri empty")
    public void test_TC25() {
        // preserveRelativeLinks(true) with empty baseUri should keep href exactly as 'x.html'
        String html = "<a href='x.html'>Link</a>";
        Safelist safelist = Safelist.relaxed().preserveRelativeLinks(true);
        String result = Jsoup.clean(html, "", safelist);
        assertTrue(result.contains("href=\"x.html\""), "Expected relative link to be preserved");
    }

    @Test
    @DisplayName("clean(bodyHtml, baseUri, Safelist, OutputSettings) applies outputSettings changes")
    public void test_TC26() {
        // Using prettyPrint(false) to prevent formatting changes; expect exact HTML fragment returned
        String html = "<p>Text</p>";
        Safelist safelist = Safelist.simpleText();
        Document.OutputSettings os = new Document.OutputSettings().prettyPrint(false);
        String result = Jsoup.clean(html, "http://u/", safelist, os);
        assertEquals("<p>Text</p>", result, "Expected output settings to preserve original HTML without pretty print");
    }

    @Test
    @DisplayName("isValid returns true when input HTML conforms to the safelist")
    public void test_TC27() {
        // Basic safelist allows <p> tags, so this HTML should be valid
        String html = "<p>Okay</p>";
        Safelist safelist = Safelist.basic();
        boolean ok = Jsoup.isValid(html, safelist);
        assertTrue(ok, "Expected HTML conforming to basic safelist to be valid");
    }

    @Test
    @DisplayName("isValid returns false when input HTML has disallowed tags")
    public void test_TC28() {
        // Basic safelist disallows <script>, so validity should be false
        String html = "<script>alert(1)</script>";
        Safelist safelist = Safelist.basic();
        boolean ok = Jsoup.isValid(html, safelist);
        assertFalse(ok, "Expected HTML with script tag to be invalid against basic safelist");
    }
}