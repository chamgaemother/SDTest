package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parseBodyFragment_0_Test {

    @Test
    @DisplayName("TC01_O1: parseBodyFragment(String bodyHtml, String baseUri) with non-empty HTML and non-empty baseUri returns Document with matching body HTML")
    public void test_TC01_O1() {
        // Given non-empty HTML and non-empty baseUri to follow the normal parse path (B0→B1→B2→B3→B4)
        String bodyHtml = "<p>Hello</p>";
        String baseUri = "http://example.com";
        // When
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        // Then
        assertEquals("<p>Hello</p>", doc.body().html(),
            "Expected the body HTML to match the input fragment unchanged");
    }

    @Test
    @DisplayName("TC02_O1: parseBodyFragment(String bodyHtml, String baseUri) with empty HTML returns Document with empty body")
    public void test_TC02_O1() {
        // Given empty HTML to test empty-body boundary (still B0→B1→B2→B3→B4)
        String bodyHtml = "";
        String baseUri = "http://example.com";
        // When
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        // Then
        assertEquals("", doc.body().html(),
            "Expected an empty input fragment to produce an empty body");
    }

    @Test
    @DisplayName("TC03_O1: parseBodyFragment(String bodyHtml, String baseUri) with null bodyHtml throws NullPointerException")
    public void test_TC03_O1() {
        // Given null bodyHtml to trigger NPE at entry (path B0→B1→B5)
        String bodyHtml = null;
        String baseUri = "http://example.com";
        // Then
        assertThrows(NullPointerException.class, () -> {
            // When
            Jsoup.parseBodyFragment(bodyHtml, baseUri);
        }, "Expected a NullPointerException when bodyHtml is null");
    }

    @Test
    @DisplayName("TC04_O1: parseBodyFragment(String bodyHtml, String baseUri) resolves relative links against baseUri")
    public void test_TC04_O1() {
        // Given a relative link in fragment and non-empty baseUri to ensure resolution branch (B0→B1→B2→B3→B4)
        String bodyHtml = "<a href=\"page.html\">link</a>";
        String baseUri = "http://example.com";
        // When
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        Elements links = doc.body().select("a");
        // Then
        assertEquals("http://example.com/page.html", links.attr("abs:href"),
            "Expected the relative link to be resolved against the provided baseUri");
    }

    @Test
    @DisplayName("TC05_O2: parseBodyFragment(String bodyHtml) with non-empty HTML returns Document with matching body HTML")
    public void test_TC05_O2() {
        // Given non-empty HTML and default baseUri (empty) to exercise overload2 B0→B1→B2→B3→B4
        String bodyHtml = "<div>Test</div>";
        // When
        Document doc = Jsoup.parseBodyFragment(bodyHtml);
        // Then
        assertEquals("<div>Test</div>", doc.body().html(),
            "Expected the body HTML to match the input fragment unchanged with default baseUri");
    }

    @Test
    @DisplayName("TC06_O2: parseBodyFragment(String bodyHtml) with empty HTML returns Document with empty body")
    public void test_TC06_O2() {
        // Given empty HTML and default baseUri to test empty-body in overload2 (B0→B1→B2→B3→B4)
        String bodyHtml = "";
        // When
        Document doc = Jsoup.parseBodyFragment(bodyHtml);
        // Then
        assertEquals("", doc.body().html(),
            "Expected an empty input fragment to produce an empty body with default baseUri");
    }

    @Test
    @DisplayName("TC07_O2: parseBodyFragment(String bodyHtml) with null bodyHtml throws NullPointerException")
    public void test_TC07_O2() {
        // Given null bodyHtml on overload2 to trigger NPE (path B0→B1→B5)
        String bodyHtml = null;
        // Then
        assertThrows(NullPointerException.class, () -> {
            // When
            Jsoup.parseBodyFragment(bodyHtml);
        }, "Expected a NullPointerException when bodyHtml is null in overload2");
    }

    @Test
    @DisplayName("TC08_O1: parseBodyFragment(String bodyHtml, String baseUri) with nested tags retains full nested structure")
    public void test_TC08_O1() {
        // Given nested tags and empty baseUri to test nesting retention branch (B0→B1→B2→B3→B4)
        String bodyHtml = "<ul><li>One</li><li>Two</li></ul>";
        String baseUri = "";
        // When
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        // Then
        assertEquals("<ul><li>One</li><li>Two</li></ul>", doc.body().html(),
            "Expected nested list structure to be preserved exactly");
    }

    @Test
    @DisplayName("TC09_O1: parseBodyFragment(String bodyHtml, String baseUri) with HTML entities encodes them properly")
    public void test_TC09_O1() {
        // Given text with entities and empty baseUri to verify entity branch (B0→B1→B2→B3→B4)
        String bodyHtml = "&amp; &lt; &gt;";
        String baseUri = "";
        // When
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        // Then
        assertEquals("&amp; &lt; &gt;", doc.body().html(),
            "Expected HTML entities to remain properly encoded in the body output");
    }

    @Test
    @DisplayName("TC10_O2: parseBodyFragment(String bodyHtml) resolves no links when default baseUri is empty")
    public void test_TC10_O2() {
        // Given a relative link and default empty baseUri to test no-resolution branch of overload2 (B0→B1→B2→B3→B4)
        String bodyHtml = "<a href=\"sub.html\">x</a>";
        // When
        Document doc = Jsoup.parseBodyFragment(bodyHtml);
        Elements links = doc.body().select("a");
        // Then
        assertEquals("", links.attr("abs:href"),
            "Expected no absolute href when default baseUri is empty");
    }
}