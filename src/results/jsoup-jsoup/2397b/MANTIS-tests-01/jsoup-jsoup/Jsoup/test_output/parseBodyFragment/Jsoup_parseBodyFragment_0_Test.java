package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parseBodyFragment_0_Test {

    @Test
    @DisplayName("TC01_O1: parseBodyFragment(String, String) with simple text and non-empty baseUri yields a body containing the text")
    void test_TC01_O1() {
        // Input is simple text, baseUri non-empty to force absolute URI base assignment path
        String html = "Hello world";
        String baseUri = "http://example.com/";
        Document result = Jsoup.parseBodyFragment(html, baseUri);
        // result.body() should contain the same content
        assertEquals("Hello world", result.body().html());
        // baseUri should be recorded on the Document
        assertEquals("http://example.com/", result.baseUri());
    }

    @Test
    @DisplayName("TC02_O1: parseBodyFragment(String, String) with nested tags and empty baseUri preserves nested structure")
    void test_TC02_O1() {
        // Input has nested tags; empty baseUri triggers branch where no abs resolution is done
        String html = "<div><span>Text</span></div>";
        String baseUri = "";
        Document result = Jsoup.parseBodyFragment(html, baseUri);
        Element body = result.body();
        // First child should be <div>
        Element div = body.child(0);
        assertEquals("div", div.tagName());
        // Its first child should be <span>
        Element span = div.child(0);
        assertEquals("span", span.tagName());
    }

    @Test
    @DisplayName("TC03_O1: parseBodyFragment(String, String) with mismatched tags auto-balances the HTML tree")
    void test_TC03_O1() {
        // Input has an unclosed <p>, triggers parser auto-balancing path
        String html = "<p>Unclosed";
        String baseUri = "";
        Document result = Jsoup.parseBodyFragment(html, baseUri);
        // The parser should close the <p> tag
        assertEquals("<p>Unclosed</p>", result.body().html());
    }

    @Test
    @DisplayName("TC04_O2: parseBodyFragment(String) overload yields same as parseBodyFragment(html,\"\")")
    void test_TC04_O2() {
        // Using single-arg overload, baseUri implicitly "" branch
        String html = "A";
        Document r1 = Jsoup.parseBodyFragment(html, "");
        Document r2 = Jsoup.parseBodyFragment(html);
        // The body HTML from both calls should match exactly
        assertEquals(r1.body().html(), r2.body().html());
    }

    @Test
    @DisplayName("TC05_O1: parseBodyFragment(String, String) resolves relative link when baseUri is non-empty")
    void test_TC05_O1() {
        // Input has <a href="page.html">, non-empty baseUri triggers absUrl resolution
        String html = "<a href=\"page.html\">link</a>";
        String baseUri = "http://example.com/";
        Document result = Jsoup.parseBodyFragment(html, baseUri);
        Element a = result.body().selectFirst("a");
        // absUrl should resolve to the full URL
        assertEquals("http://example.com/page.html", a.absUrl("href"));
    }

    @Test
    @DisplayName("TC06_O2: parseBodyFragment(String) leaves relative link unresolved when baseUri is empty")
    void test_TC06_O2() {
        // Single-arg overload implies baseUri = "" branch, so no abs resolution
        String html = "<a href=\"page.html\">link</a>";
        Document result = Jsoup.parseBodyFragment(html);
        Element a = result.body().selectFirst("a");
        // attr href remains unchanged
        assertEquals("page.html", a.attr("href"));
        // absUrl should be empty since no baseUri was set
        assertTrue(a.absUrl("href").isEmpty());
    }

    @Test
    @DisplayName("TC07_O1: parseBodyFragment(String, String) throws NullPointerException when html is null")
    void test_TC07_O1() {
        // Null html triggers immediate NPE branch before parsing
        String html = null;
        String baseUri = "";
        assertThrows(NullPointerException.class, () -> {
            Jsoup.parseBodyFragment(html, baseUri);
        });
    }
}