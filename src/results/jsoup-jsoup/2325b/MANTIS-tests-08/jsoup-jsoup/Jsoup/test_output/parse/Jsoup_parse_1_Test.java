package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for org.jsoup.Jsoup.parse(String html, String baseUri)
 */
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(html, baseUri) with relative link and non-empty baseUri resolves to absolute URL")
    public void test_TC11() {
        // Given a simple anchor with a relative href and a non-empty baseUri,
        // so branch B2 (non-empty html) is taken and branch B5 resolves relative URLs
        String html = "<a href=\"/test\">x</a>";
        String baseUri = "https://site.com";
        // When: parsing
        Document doc = Jsoup.parse(html, baseUri);
        // Then: the relative link should be resolved to absolute with baseUri prefix
        String result = doc.body().html();
        assertEquals("<a href=\"https://site.com/test\">x</a>", result,
            "Expected the relative href '/test' to be resolved to absolute URL with baseUri");
    }

    @Test
    @DisplayName("parse(html, baseUri) with empty html returns empty body")
    public void test_TC12() {
        // Given an empty html string and a baseUri,
        // branch B3 (html.isEmpty()) is taken leading to empty body
        String html = "";
        String baseUri = "http://any/";
        // When: parsing empty content
        Document doc = Jsoup.parse(html, baseUri);
        // Then: body HTML should be empty
        String result = doc.body().html();
        assertEquals("", result,
            "Expected empty html input to produce an empty body content");
    }

    @Test
    @DisplayName("parse(html, baseUri) with null html throws NullPointerException before parsing")
    public void test_TC13() {
        // Given a null html value,
        // branch B4 (html == null) should be detected, throwing NullPointerException
        String html = null;
        String baseUri = "http://x/";
        // When & Then: parsing null should throw NPE immediately
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, baseUri),
            "Expected NullPointerException when html is null");
    }
}