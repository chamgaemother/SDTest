package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("TC11: parse(String html, String baseUri, Parser parser) uses provided parser and preserves baseUri")
    public void test_TC11() {
        // Design: using xmlParser ensures branch to Parser.parseInput(html, baseUri)
        String html = "<a href=\"x\">link</a>";
        String base = "http://test/";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, base, parser);
        // Oracle: baseUri preserved and one <a> element parsed by provided parser
        assertEquals(base, doc.baseUri(), "Base URI should be preserved");
        assertEquals(1, doc.getElementsByTag("a").size(), "XML parser should parse one <a> tag");
    }

    @Test
    @DisplayName("TC12: parse(String html, String baseUri, null) throws IllegalArgumentException for null parser")
    public void test_TC12() {
        // Design: null parser triggers argument check path
        String html = "<p>test</p>";
        String base = "";
        Parser parser = null;
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parse(html, base, parser);
        }, "Null parser should cause IllegalArgumentException");
    }

    @Test
    @DisplayName("TC13: parse(String html) returns Document with empty baseUri and correct body content")
    public void test_TC13() {
        // Design: no baseUri overload calls Parser.parse(html, ""), testing default branch
        String html = "<div>X</div>";
        Document doc = Jsoup.parse(html);
        // Oracle: default baseUri is empty, and body html matches input fragment
        assertEquals("", doc.baseUri(), "Default baseUri should be empty string");
        assertEquals("<div>X</div>", doc.body().html(), "Body HTML should exactly match input fragment");
    }

    @Test
    @DisplayName("TC14: parseBodyFragment(String bodyHtml, String baseUri) wraps fragment in body and resolves relative links")
    public void test_TC14() {
        // Design: parseBodyFragment with non-empty baseUri triggers resolution of relative URLs
        String fragment = "<img src=\"img.png\">";
        String base = "http://site.com/path/";
        Document doc = Jsoup.parseBodyFragment(fragment, base);
        String resolved = doc.body().html();
        // Oracle: src attribute should be fully resolved against baseUri
        assertTrue(resolved.contains("<img src=\"http://site.com/path/img.png\">"),
                "Relative URL should be resolved to absolute URL using baseUri");
    }

    @Test
    @DisplayName("TC15: parseBodyFragment(String bodyHtml) uses empty baseUri and no URL resolution")
    public void test_TC15() {
        // Design: parseBodyFragment with empty default baseUri (\"\"), no resolution branch
        String fragment = "<img src=\"a.png\">";
        Document doc = Jsoup.parseBodyFragment(fragment);
        String body = doc.body().html();
        // Oracle: src remains unchanged as relative resolution is disabled
        assertTrue(body.contains("src=\"a.png\""),
                "Relative URL should remain unchanged when baseUri is empty");
    }
}