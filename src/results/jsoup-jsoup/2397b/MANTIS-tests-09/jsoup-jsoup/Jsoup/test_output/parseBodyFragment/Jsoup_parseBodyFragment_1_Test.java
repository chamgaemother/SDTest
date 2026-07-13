package org.jsoup;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Jsoup_parseBodyFragment_1_Test {

    @Test
    @DisplayName("TC11: parseBodyFragment(String, String) applies <base> tag to override provided baseUri and resolves relative links accordingly")
    public void test_TC11() {
        // Inline comment: including a <base> tag in the fragment should trigger branch B1 and B2 where baseUri is overridden
        String fragment = "<base href=\"http://foo.com/\"/><a href=\"bar.html\">link</a>";
        String providedBaseUri = "http://original.com/";
        // WHEN: parseBodyFragment is called with both fragment and providedBaseUri
        Document doc = Jsoup.parseBodyFragment(fragment, providedBaseUri);
        // THEN: the <base> tag in the fragment governs resolution, so href remains as-is (relative) under the new base
        String resolved = doc.body().select("a").first().attr("href");
        assertEquals(
            "bar.html",
            resolved,
            "Expected the <base> tag to override the provided baseUri and keep the relative href unchanged"
        );
    }

    @Test
    @DisplayName("TC12: parseBodyFragment(String) auto-balances unclosed tags in HTML fragment (tag-soup correction)")
    public void test_TC12() {
        // Inline comment: a fragment with unclosed <div> and <p> should trigger auto-correction branch B2 (auto-balancing)
        String fragment = "<div><p>Test";
        // WHEN: parseBodyFragment is called with only the fragment (empty baseUri)
        Document doc = Jsoup.parseBodyFragment(fragment);
        // THEN: parser should auto-close missing tags, yielding well-formed nested structure
        String bodyHtml = doc.body().html();
        assertEquals(
            "<div><p>Test</p></div>",
            bodyHtml,
            "Expected unclosed tags to be auto-closed forming <div><p>Test</p></div>"
        );
    }
}