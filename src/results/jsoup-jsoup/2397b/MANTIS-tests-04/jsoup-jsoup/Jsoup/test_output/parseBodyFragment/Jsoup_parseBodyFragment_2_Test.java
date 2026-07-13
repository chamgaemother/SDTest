package org.jsoup;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parseBodyFragment_2_Test {

    @Test
    @DisplayName("Multiple top-level siblings are parsed in sequence (iteration>1)")
    public void test_TC16() {
        // Design: html has multiple top-level sibling nodes (h1, p, and text), exercising the loop over siblings more than once.
        String html = "<h1>One</h1><p>Two</p>Three";
        String baseUri = "http://example.com/";
        // WHEN
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // THEN: the body HTML should preserve the three siblings in the original order
        assertEquals("<h1>One</h1><p>Two</p>Three", doc.body().html());
    }

    @Test
    @DisplayName("Null baseUri throws NullPointerException before parsing")
    public void test_TC17() {
        // Design: passing null baseUri should trigger a precondition null check (path B5).
        String html = "<div>Hi</div>";
        String baseUri = null;
        // WHEN & THEN: expect NullPointerException
        assertThrows(NullPointerException.class, () -> Jsoup.parseBodyFragment(html, baseUri));
    }

    @Test
    @DisplayName("Whitespace-only fragment yields whitespace text node preserved")
    public void test_TC18() {
        // Design: html consists only of whitespace and newline, tests that a single iteration over one text node preserves exact whitespace.
        String html = "   \n  ";
        String baseUri = "";
        // WHEN
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // THEN: body HTML equals exactly the whitespace content
        assertEquals("   \n  ", doc.body().html());
    }
}