package org.jsoup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parseBodyFragment_2_Test {

    @Test
    @DisplayName("parseBodyFragment(String) with null html throws NullPointerException")
    public void test_TC11() {
        // Given a null HTML input, parsing should immediately fail with NPE (branch B2 for null check).
        assertThrows(NullPointerException.class, () -> {
            Jsoup.parseBodyFragment((String) null);
        });
    }

    @Test
    @DisplayName("parseBodyFragment(String) sets baseUri to \"\" and retains body content")
    public void test_TC12() {
        // Given simple body HTML without a <base> tag, the baseUri remains empty (B3→B5) and body content is unchanged.
        String html = "Sample";
        Document doc = Jsoup.parseBodyFragment(html);
        assertAll("Validate baseUri is empty and content retained",
            () -> assertEquals("", doc.baseUri(), "Expected baseUri to be empty when no base tag provided"),
            () -> assertEquals("Sample", doc.body().html(), "Expected body HTML to match input")
        );
    }

    @Test
    @DisplayName("parseBodyFragment(String) with <base> tag resets document baseUri and resolves relative links")
    public void test_TC13() {
        // Given body HTML with a <base> tag, parser should set document baseUri to that href (B6) and resolve relative URLs (B8).
        String html = "<base href='http://x.com/sub/'/><a href='page.html'>link</a>";
        Document doc = Jsoup.parseBodyFragment(html);
        Element link = doc.body().selectFirst("a");
        assertNotNull(link, "Expected an <a> element in the parsed body");
        String resolved = link.absUrl("href");
        assertEquals("http://x.com/sub/page.html", resolved,
            "Expected relative href to resolve against the <base> href");
    }
}