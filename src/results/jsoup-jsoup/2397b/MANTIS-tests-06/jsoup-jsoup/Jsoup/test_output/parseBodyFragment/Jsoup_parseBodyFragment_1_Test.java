package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Jsoup_parseBodyFragment_1_Test {

    @Test
    @DisplayName("parseBodyFragment preserves absolute URLs unchanged when resolving href")
    void test_TC10() {
        // Covers path B0→B1→B3→B5: baseUri non-empty, href is absolute so no resolution branch
        String html = "<a href='https://example.com/page.html'>link</a>";
        String baseUri = "http://ignored.com/"; // non-empty so skip base-tag addition
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // Expect the absolute URL to remain unchanged
        assertEquals("<a href=\"https://example.com/page.html\">link</a>", doc.body().html());
    }

    @Test
    @DisplayName("parseBodyFragment leaves protocol-relative URLs unchanged regardless of baseUri")
    void test_TC11() {
        // Covers path B0→B1→B2→B5: baseUri non-empty, src protocol-relative so resolution skipped
        String html = "<img src='//cdn.com/img.png'>";
        String baseUri = "https://example.org/base/";
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // Expect the protocol-relative URL to remain as-is
        assertEquals("<img src=\"//cdn.com/img.png\">", doc.body().html());
    }

    @Test
    @DisplayName("parseBodyFragment uses <base href> tag to resolve relative URLs when baseUri empty")
    void test_TC12() {
        // Covers path B0→B1→B4→B6→B8: baseUri empty, base tag present triggers override & resolution
        String html = "<base href='http://override.com/sub/'><a href='item.html'>name</a>";
        String baseUri = ""; // empty triggers base-tag branch
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // Expect relative URL resolved against base tag URL
        assertEquals(
            "<base href=\"http://override.com/sub/\">" +
            "<a href=\"http://override.com/sub/item.html\">name</a>",
            doc.body().html()
        );
    }

    @Test
    @DisplayName("parseBodyFragment resolves ‘..’ segments against baseUri for nested relative paths")
    void test_TC13() {
        // Covers path B0→B1→B2(loop×1)→B5: baseUri non-empty, one '..' segment resolution iteration
        String html = "<a href='../a/b.html'>back</a>";
        String baseUri = "http://ex.com/x/y/"; // resolution will process '..' to go up one level
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // Expect '../a/b.html' to resolve to 'http://ex.com/x/a/b.html'
        assertEquals("<a href=\"http://ex.com/x/a/b.html\">back</a>", doc.body().html());
    }
}