package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParserException;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(html, baseUri) with non-empty html and non-empty baseUri returns Document with correct baseUri")
    public void test_TC01() {
        String html = "<p>Hello</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("http://example.com/", doc.getBaseUri(), "Base URI should match the provided non-empty baseUri");
        assertTrue(doc.body().html().contains("<p>Hello</p>"), "Body HTML should contain the parsed paragraph");
    }

    @Test
    @DisplayName("parse(html, \"\" ) with empty baseUri returns Document with empty baseUri")
    public void test_TC02() {
        String html = "<h1>Title</h1>";
        String baseUri = "";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("", doc.getBaseUri(), "Base URI should be empty when provided as empty string");
        assertTrue(doc.body().html().contains("<h1>Title</h1>"), "Body HTML should contain the parsed heading");
    }

    @Test
    @DisplayName("parse(html) calls Parser.parse(html, \"") and returns Document with empty baseUri")
    public void test_TC03() {
        String html = "<div>Empty</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.getBaseUri(), "Default baseUri should be empty string");
        assertTrue(doc.body().html().contains("<div>Empty</div>"), "Body HTML should contain the parsed div");
    }

    @Test
    @DisplayName("parse(html, baseUri, parser) uses provided Parser to parse and returns Document with correct baseUri")
    public void test_TC04() {
        String html = "<span>Stub</span>";
        String baseUri = "https://test/";
        Parser stubParser = new Parser() {
            @Override
            public Document parseInput(String inputHtml, String inputBaseUri) {
                Document d = Document.createShell(inputBaseUri);
                d.body().append(inputHtml);
                return d;
            }
        };
        Document doc = Jsoup.parse(html, baseUri, stubParser);
        assertEquals("https://test/", doc.getBaseUri(), "Custom parser should set the provided baseUri");
        assertTrue(doc.body().html().contains("<span>Stub</span>"), "Body HTML should contain stub content from parser");
    }

    @Test
    @DisplayName("parse(html, parser) with empty baseUri uses provided Parser with empty baseUri")
    public void test_TC05() {
        String html = "<li>Item</li>";
        Parser stubParser = new Parser() {
            @Override
            public Document parseInput(String inputHtml, String inputBaseUri) {
                Document d = Document.createShell(inputBaseUri);
                d.body().append(inputHtml);
                return d;
            }
        };
        Document doc = Jsoup.parse(html, stubParser);
        assertEquals("", doc.getBaseUri(), "When using two-arg overload, baseUri passed to parser is empty string");
        assertTrue(doc.body().html().contains("<li>Item</li>"), "Body HTML should contain stubbed list item");
    }

    @Test
    @DisplayName("parse(null, baseUri) throws IllegalArgumentException for null html")
    public void test_TC06() {
        String baseUri = "http://any/";
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse((String) null, baseUri),
            "Passing null html should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("parse(html, null) returns Document with null baseUri allowed by Parser")
    public void test_TC07() {
        String html = "<em>Test</em>";
        String baseUri = null;
        Document doc = Jsoup.parse(html, baseUri);
        assertNull(doc.getBaseUri(), "Base URI should be null when provided as null");
        assertTrue(doc.body().html().contains("<em>Test</em>"), "Body HTML should contain the parsed emphasis tag");
    }

    @Test
    @DisplayName("parse(\"") returns Document with empty body for empty html")
    public void test_TC08() {
        String html = "";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.body().html(), "Body HTML should be empty for empty input");
        assertEquals("", doc.getBaseUri(), "Default baseUri should be empty string");
    }

    @Test
    @DisplayName("parse(html with base tag, \"\" ) returns Document resolving relative URLs from base tag")
    public void test_TC09() {
        String html = "<base href=\"https://site/\"><a href=\"rel\">link</a>";
        Document doc = Jsoup.parse(html, "");
        String abs = doc.select("a").first().absUrl("href");
        assertEquals("https://site/rel", abs, "Relative href should resolve against base tag href");
    }

    @Test
    @DisplayName("parse(html) with malformed tag throws ParserException for invalid HTML")
    public void test_TC10() {
        String html = "<unclosed><p>Bad";
        assertThrows(org.jsoup.parser.ParserException.class,
            () -> Jsoup.parse(html),
            "Malformed HTML should cause a ParserException");
    }
}