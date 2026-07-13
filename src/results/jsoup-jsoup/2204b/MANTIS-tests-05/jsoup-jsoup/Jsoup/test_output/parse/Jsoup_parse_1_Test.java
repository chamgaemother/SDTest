package org.jsoup;

import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(String html, String baseUri, Parser parser) with valid HTML and custom parser returns parsed Document with correct syntax and baseUri")
    public void test_TC10() {
        // Given: valid XML-like HTML and an explicit XML parser (branch B7→B8 via custom parser path)
        String html = "<node attr='v'>text</node>";
        String baseUri = "http://example.org/";
        // Corrected: directly use Parser.xmlParser()
        Document doc = Jsoup.parse(html, baseUri, Parser.xmlParser());
        // Then: syntax should be xml, baseUri preserved, attribute and element parsed
        assertEquals(Parser.Syntax.xml, doc.outputSettings().syntax(),
            "Expected document to use XML syntax when xmlParser is provided");
        assertEquals(baseUri, doc.baseUri(),
            "Expected baseUri to be preserved from input");
        assertNotNull(doc.select("node").first(),
            "Expected element <node> to be present in the parsed document");
        assertEquals("v", doc.select("node").first().attr("attr"),
            "Expected attribute 'attr' on <node> to equal 'v'");
    }

    @Test
    @DisplayName("parse(String html, String baseUri, Parser parser) with null parser throws IllegalArgumentException")
    public void test_TC11() {
        // Given: non-null html and baseUri, but null parser to exercise exception path (B6)
        String html = "<p>x</p>";
        String baseUri = "http://x/";
        Parser parser = null;
        // When & Then: expect IllegalArgumentException due to null parser
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parse(html, baseUri, parser);
        });
        // Message may indicate parser cannot be null
        String msg = ex.getMessage();
        assertTrue(msg != null && !msg.isEmpty(),
            "Expected exception message to indicate parser null argument");
    }

    @Test
    @DisplayName("parse(String html) with empty html returns empty Document body and empty baseUri")
    public void test_TC12() {
        // Given: empty HTML to trigger base-case parse(html) (B4→B5)
        String html = "";
        // When: calling single-arg parse
        Document doc = Jsoup.parse(html);
        // Then: body should be empty and baseUri defaulted to empty string
        assertEquals("", doc.body().html(),
            "Expected an empty body HTML when input HTML is empty");
        assertEquals("", doc.baseUri(),
            "Expected baseUri to be empty string by default");
    }

    @Test
    @DisplayName("parse(URL url, int timeoutMillis) with non-HTTP URL throws MalformedURLException")
    public void test_TC13() throws Exception {
        // Given: a URL with unsupported protocol to trigger protocol validation exception (B2→B6)
        URL url = new URL("ftp://example.com/");
        int timeout = 1000;
        // When & Then: expect MalformedURLException due to non-HTTP/HTTPS protocol
        assertThrows(MalformedURLException.class, () -> {
            Jsoup.parse(url, timeout);
        });
    }

    @Test
    @DisplayName("parseBodyFragment(String bodyHtml, String baseUri) with empty fragment returns empty body and preserves baseUri")
    public void test_TC14() {
        // Given: empty body fragment and a custom baseUri to exercise parseBodyFragment overload (B5→B6)
        String bodyHtml = "";
        String baseUri = "https://test/";
        // When: parsing body fragment
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        // Then: body HTML should be empty and baseUri preserved
        assertEquals("", doc.body().html(),
            "Expected empty body HTML for empty fragment");
        assertEquals(baseUri, doc.baseUri(),
            "Expected baseUri to match the provided value");
    }
}