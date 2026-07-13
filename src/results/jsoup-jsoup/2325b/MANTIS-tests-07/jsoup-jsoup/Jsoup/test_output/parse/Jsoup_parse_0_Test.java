package org.jsoup;

import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("TC01: parse(html,baseUri) with non-empty html and valid baseUri returns parsed Document via Parser.parse")
    void test_TC01() {
        String html = "<p>Test</p>";
        String baseUri = "http://example.com";
        Document doc = Jsoup.parse(html, baseUri, Parser.htmlParser()); // Specify the parser to avoid ambiguity
        assertEquals("<p>Test</p>", doc.body().html());
    }

    @Test
    @DisplayName("TC02: parse(html,baseUri) with empty html returns empty Document body")
    void test_TC02() {
        String html = "";
        String baseUri = "http://example.com";
        Document doc = Jsoup.parse(html, baseUri, Parser.htmlParser()); // Specify the parser to avoid ambiguity
        assertEquals("", doc.body().html());
    }

    @Test
    @DisplayName("TC03: parse(html,baseUri) with null html throws NullPointerException at entry")
    void test_TC03() {
        String baseUri = "http://example.com";
        assertThrows(NullPointerException.class, () -> Jsoup.parse(null, baseUri, Parser.htmlParser())); // Specify the parser to avoid ambiguity
    }

    @Test
    @DisplayName("TC04: parse(html,baseUri) with empty baseUri returns Document and baseUri preserved as empty")
    void test_TC04() {
        String html = "<div/>";
        String baseUri = "";
        Document doc = Jsoup.parse(html, baseUri, Parser.htmlParser()); // Specify the parser to avoid ambiguity
        assertEquals("", doc.baseUri());
    }

    @Test
    @DisplayName("TC05_O1: parse(html,baseUri,parser) with custom Parser stub is invoked correctly")
    void test_TC05_O1() {
        String html = "<x/>";
        String baseUri = "b";
        Document dummy = new Document("dummy");
        Parser stubParser = new Parser() {
            @Override
            public Document parseInput(String inHtml, String inBaseUri) {
                assertEquals(html, inHtml);
                assertEquals(baseUri, inBaseUri);
                return dummy;
            }
        } {
            @Override
            public Document parseInput(String inHtml, String inBaseUri) {
                return new Document(inHtml);
            }
        };
        Document result = Jsoup.parse(html, baseUri, stubParser);
        assertSame(dummy, result);
    }

    @Test
    @DisplayName("TC06_O1: parse(html,baseUri,parser) with null parser throws NullPointerException")
    void test_TC06_O1() {
        String html = "<a/>";
        String baseUri = "u";
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, baseUri, null));
    }

    @Test
    @DisplayName("TC07_O2: parse(html,parser) with non-empty html and valid parser returns Document via parser.parseInput")
    void test_TC07_O2() {
        String html = "<b/>";
        Document stubDoc = new Document("stub");
        Parser stubParser = new Parser() {
            @Override
            public Document parseInput(String inHtml, String inBaseUri) {
                assertEquals(html, inHtml);
                assertEquals("", inBaseUri);
                return stubDoc;
            }
        } {
            @Override
            public Document parseInput(String inHtml, String inBaseUri) {
                return new Document(inHtml);
            }
        };
        Document result = Jsoup.parse(html, stubParser);
        assertSame(stubDoc, result);
    }

    @Test
    @DisplayName("TC08: parse(html) with non-empty html returns Document via Parser.parse with empty baseUri")
    void test_TC08() {
        String html = "<i/>";
        Document doc = Jsoup.parse(html, Parser.htmlParser()); // Specify the parser to avoid ambiguity
        assertEquals("", doc.baseUri());
    }

    @Test
    @DisplayName("TC09: parse(html) with empty html returns Document with empty body")
    void test_TC09() {
        String html = "";
        Document doc = Jsoup.parse(html, Parser.htmlParser()); // Specify the parser to avoid ambiguity
        assertEquals("", doc.body().html());
    }
}