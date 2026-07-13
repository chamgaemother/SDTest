package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parseBodyFragment_2_Test {

    @Test
    @DisplayName("parseBodyFragment retains comment nodes in the body fragment")
    public void test_TC13() {
        // Branch B0->B1->B2: entering parseBodyFragment with non-empty baseUri and HTML containing comment
        String fragment = "<!-- note --><p>Text</p>";
        String baseUri = "http://example.com/";

        // WHEN: parse the fragment including a comment and an element
        Document doc = Jsoup.parseBodyFragment(fragment, baseUri);

        // THEN: comment node should be present in the body HTML
        String bodyHtml = doc.body().html();
        assertTrue(bodyHtml.contains("<!-- note -->"),
                "Expected the comment node to be preserved in the body HTML");

        // AND: the <p> element should be parsed correctly
        Elements paragraphs = doc.body().select("p");
        assertEquals(1, paragraphs.size(), "Expected exactly one <p> element");
        assertEquals("<p>Text</p>", paragraphs.first().outerHtml(),
                "Expected the paragraph outer HTML to match the input content");
    }

    @Test
    @DisplayName("parseBodyFragment preserves whitespace-only fragment as text node")
    public void test_TC14() {
        // Branch B0->B1->B2: entering parseBodyFragment with empty baseUri and fragment of only whitespace
        String fragment = "   \n  ";
        String baseUri = "";

        // WHEN: parse the whitespace-only fragment
        Document doc = Jsoup.parseBodyFragment(fragment, baseUri);

        // THEN: the body HTML should be exactly the whitespace text, preserving whitespace-only text nodes
        String bodyHtml = doc.body().html();
        assertEquals(fragment, bodyHtml,
                "Expected whitespace-only fragment to be preserved exactly as body HTML");
    }
}