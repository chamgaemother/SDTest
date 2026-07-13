package org.jsoup;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parseBodyFragment_0_Test {

    @Test
    @DisplayName("TC01: parseBodyFragment(html,baseUri) with non-empty bodyHtml and absolute baseUri returns fragment wrapped and unchanged")
    void test_TC01() {
        // branch-baseUri-nonempty: baseUri is non-empty so absolute resolution path
        String html = "<p>text</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // expect the body HTML unchanged as a fragment
        assertEquals("<p>text</p>", doc.body().html());
    }

    @Test
    @DisplayName("TC02: parseBodyFragment(html) uses empty baseUri alias overload and returns fragment unchanged")
    void test_TC02() {
        // branch-baseUri-empty: single-arg overload uses empty string baseUri
        String html = "<span>ok</span>";
        Document doc = Jsoup.parseBodyFragment(html);
        // expect unchanged fragment
        assertEquals("<span>ok</span>", doc.body().html());
    }

    @Test
    @DisplayName("TC03: parseBodyFragment(html,baseUri) resolves relative link to absolute URL when baseUri non-empty")
    void test_TC03() {
        // branch-resolve-relative: href is relative and baseUri non-empty, should resolve
        String html = "<a href='page.html'>link</a>";
        String baseUri = "http://ex.com/";
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // expect the link resolved to absolute URL
        assertEquals("<a href=\"http://ex.com/page.html\">link</a>", doc.body().html());
    }

    @Test
    @DisplayName("TC04: parseBodyFragment(html,baseUri) retains relative link when baseUri is empty string")
    void test_TC04() {
        // branch-baseUri-empty: explicit empty baseUri, so relative href remains unchanged
        String html = "<a href='page.html'>link</a>";
        String baseUri = "";
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // expect the link remains relative
        assertEquals("<a href=\"page.html\">link</a>", doc.body().html());
    }

    @Test
    @DisplayName("TC05: parseBodyFragment(emptyBody,baseUri) returns empty body for empty input string")
    void test_TC05() {
        // branch-empty-body: html is empty string, so no tags
        String html = "";
        String baseUri = "http://x/";
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // expect empty body content
        assertEquals("", doc.body().html());
    }

    @Test
    @DisplayName("TC06: parseBodyFragment(unclosedTag,baseUri) closes unclosed tags in output fragment")
    void test_TC06() {
        // branch-unclosed-tag: html with unclosed <div> should auto-close tag
        String html = "<div>foo";
        String baseUri = "";
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // expect auto-closed div tag
        assertEquals("<div>foo</div>", doc.body().html());
    }

    @Test
    @DisplayName("TC07: parseBodyFragment(null,baseUri) throws NullPointerException for null html parameter")
    void test_TC07() {
        // branch-null-html: passing null html should NPE
        String html = null;
        String baseUri = "http://ex/";
        assertThrows(NullPointerException.class, () -> Jsoup.parseBodyFragment(html, baseUri));
    }

    @Test
    @DisplayName("TC08: parseBodyFragment(html,null) throws NullPointerException for null baseUri parameter")
    void test_TC08() {
        // branch-null-baseUri: passing null baseUri should NPE
        String html = "<p>x</p>";
        String baseUri = null;
        assertThrows(NullPointerException.class, () -> Jsoup.parseBodyFragment(html, baseUri));
    }

    @Test
    @DisplayName("TC09: parseBodyFragment(null) throws NullPointerException for null html in single-arg overload")
    void test_TC09() {
        // branch-null-html single-arg overload: null html should NPE
        String html = null;
        assertThrows(NullPointerException.class, () -> Jsoup.parseBodyFragment(html));
    }
}