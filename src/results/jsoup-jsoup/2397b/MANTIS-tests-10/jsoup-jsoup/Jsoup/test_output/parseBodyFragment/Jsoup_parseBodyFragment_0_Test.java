package org.jsoup;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parseBodyFragment_0_Test {

    @Test
    @DisplayName("parseBodyFragment with non-empty baseUri returns Document containing body fragment")
    public void test_TC01_O1() {
        // Branch B0->B1: baseUri is non-empty, so no DummyUri substitution
        String bodyHtml = "<p>Test</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        // Expect the fragment to appear unchanged inside body
        assertEquals("<p>Test</p>", doc.body().html());
    }

    @Test
    @DisplayName("parseBodyFragment with empty baseUri preserves relative links in fragment")
    public void test_TC02_O1() {
        // Branch B0->B1: baseUri is empty string, so preserve relative links
        String bodyHtml = "<a href=\"path/page.html\">Link</a>";
        String baseUri = "";
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        // The relative link href should remain unchanged in the output HTML
        String out = doc.body().html();
        assertTrue(out.contains("<a href=\"path/page.html\">Link</a>"), 
            "Expected relative link to be preserved when baseUri is empty");
    }

    @Test
    @DisplayName("parseBodyFragment overload without baseUri returns Document with html fragment and default baseUri")
    public void test_TC03_O2() {
        // Branch B0->B1: using overload without baseUri, default baseUri is empty
        String bodyHtml = "<div>Content</div>";
        Document doc = Jsoup.parseBodyFragment(bodyHtml);
        // Expect the fragment content in body unchanged
        assertEquals("<div>Content</div>", doc.body().html());
    }

    @Test
    @DisplayName("parseBodyFragment throws NullPointerException when html is null for overload with baseUri")
    public void test_TC04_O1() {
        // Branch B0->B2: html is null, should throw NPE before parsing
        String bodyHtml = null;
        String baseUri = "http://example.com/";
        assertThrows(NullPointerException.class, () -> {
            Jsoup.parseBodyFragment(bodyHtml, baseUri);
        });
    }

    @Test
    @DisplayName("parseBodyFragment throws NullPointerException when html is null for overload without baseUri")
    public void test_TC05_O2() {
        // Branch B0->B2: html is null, using default-baseUri overload
        String bodyHtml = null;
        assertThrows(NullPointerException.class, () -> {
            Jsoup.parseBodyFragment(bodyHtml);
        });
    }
}