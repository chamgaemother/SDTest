package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for Jsoup.parseBodyFragment overloads.
 */
public class Jsoup_parseBodyFragment_1_Test {

    @Test
    @DisplayName("parseBodyFragment(bodyHtml) with null bodyHtml throws IllegalArgumentException on one-arg overload")
    void test_TC05() {
        // We supply a null bodyHtml to exercise the validation branch for null input in the one-arg overload
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parseBodyFragment((String) null);
        });
    }

    @Test
    @DisplayName("parseBodyFragment(bodyHtml, baseUri) with empty bodyHtml and valid baseUri returns empty body")
    void test_TC06() {
        // Empty bodyHtml triggers the branch that creates an empty body in the two-arg overload
        String bodyHtml = "";
        String baseUri = "https://example.com/";
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        // The resulting document should have an empty body HTML
        assertEquals("", doc.body().html());
    }
}