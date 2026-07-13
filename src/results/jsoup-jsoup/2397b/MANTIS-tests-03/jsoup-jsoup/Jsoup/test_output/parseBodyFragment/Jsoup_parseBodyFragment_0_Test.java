package org.jsoup;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Jsoup.parseBodyFragment overloads based on provided scenarios.
 */
public class Jsoup_parseBodyFragment_0_Test {

    @Test
    @DisplayName("parseBodyFragment(bodyHtml, baseUri) with non-empty bodyHtml and non-empty baseUri returns a Document with correct body content")
    public void test_TC01_O1() {
        // Branch B2 taken: two-arg overload with non-empty inputs
        String bodyHtml = "<p>Hello</p>";
        String baseUri = "https://example.com/";
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        // Expect the parsed document body to exactly match the input fragment
        assertEquals("<p>Hello</p>", doc.body().html());
    }

    @Test
    @DisplayName("parseBodyFragment(bodyHtml) with non-empty bodyHtml uses empty baseUri and returns Document with correct body content")
    public void test_TC02_O2() {
        // Branch B3 taken: one-arg overload uses empty baseUri internally
        String bodyHtml = "<div>Test</div>";
        Document doc = Jsoup.parseBodyFragment(bodyHtml);
        // Even with empty baseUri, fragment content should be preserved
        assertEquals("<div>Test</div>", doc.body().html());
    }

    @Test
    @DisplayName("parseBodyFragment(bodyHtml, baseUri) with null bodyHtml throws IllegalArgumentException")
    public void test_TC03_O1() {
        // Branch EX on B2: bodyHtml null should trigger IllegalArgumentException
        String bodyHtml = null;
        String baseUri = "https://example.com/";
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parseBodyFragment(bodyHtml, baseUri);
        });
    }

    @Test
    @DisplayName("parseBodyFragment(bodyHtml, baseUri) with null baseUri throws IllegalArgumentException")
    public void test_TC04_O1() {
        // Branch EX on B2: baseUri null should trigger IllegalArgumentException
        String bodyHtml = "<span>OK</span>";
        String baseUri = null;
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parseBodyFragment(bodyHtml, baseUri);
        });
    }
}