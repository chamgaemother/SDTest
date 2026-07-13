package org.jsoup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.safety.Safelist;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC18: parseBodyFragment(String bodyHtml, String baseUri) returns a Document with resolved baseUri")
    public void test_TC18() {
        // Inline comment: We exercise the branch where a non-empty baseUri is provided so B2 path is taken.
        String bodyHtml = "<div>X</div>";
        String baseUri = "https://site/";
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        // Expect the fragment to appear in the body
        assertTrue(doc.body().html().contains("<div>X</div>"), "Body HTML should contain the original fragment");
        // Expect the document's baseUri to be set to the provided non-empty value
        assertEquals(baseUri, doc.baseUri(), "Base URI should match the provided value");
    }

    @Test
    @DisplayName("TC19: parseBodyFragment(String bodyHtml) returns a Document with empty baseUri")
    public void test_TC19() {
        // Inline comment: We exercise the branch where no baseUri is provided (empty string), taking path B3.
        String bodyHtml = "<span>Y</span>";
        Document doc = Jsoup.parseBodyFragment(bodyHtml);
        // Expect the fragment to appear in the body
        assertTrue(doc.body().html().contains("<span>Y</span>"), "Body HTML should contain the original fragment");
        // Expect the document's baseUri to default to empty string
        assertEquals("", doc.baseUri(), "Base URI should be empty when none is provided");
    }

    @Test
    @DisplayName("TC20: clean(String html, String baseUri, Safelist) sets DummyUri when baseUri empty and preserveRelativeLinks true")
    public void test_TC20() {
        // Inline comment: baseUri is empty and safelist.preserveRelativeLinks(true) triggers DummyUri assignment branch.
        String html = "<a href=\"p.png\">link</a>";
        String baseUri = "";
        Safelist safelist = Safelist.simpleText().preserveRelativeLinks(true);
        String out = Jsoup.clean(html, baseUri, safelist);
        // Expect the cleaned HTML to retain the relative link, using DummyUri internally
        assertTrue(out.contains("<a href=\"p.png\">link</a>"),
                   "Cleaned HTML should contain the allowed <a> tag with relative link preserved");
    }

    @Test
    @DisplayName("TC21: clean(String html, String baseUri, Safelist) does not set DummyUri when preserveRelativeLinks false")
    public void test_TC21() {
        // Inline comment: baseUri is empty but safelist.none() leaves preserveRelativeLinks false, so DummyUri should not be used.
        String html = "<b>Z</b>";
        String baseUri = "";
        Safelist safelist = Safelist.none();
        String out = Jsoup.clean(html, baseUri, safelist);
        // Expect only the text content "Z" since <b> is disallowed by none()
        assertEquals("Z", out, "Cleaned HTML with no allowed tags should yield just the text");
    }

    @Test
    @DisplayName("TC22: clean(String html, String baseUri, Safelist, OutputSettings) applies custom output settings")
    public void test_TC22() {
        // Inline comment: Using overload with OutputSettings, exercising the outputSettings application branch.
        String html = "<p>Hi</p>";
        String baseUri = "";
        Safelist safelist = Safelist.basic();
        OutputSettings settings = new OutputSettings().prettyPrint(false);
        String out = Jsoup.clean(html, baseUri, safelist, settings);
        // Expect the cleaned HTML to include <p>Hi</p> and not be pretty-printed (no extra whitespace)
        assertTrue(out.contains("<p>Hi</p>"),
                   "Cleaned HTML should contain <p>Hi</p> respecting prettyPrint(false)");
    }

    @Test
    @DisplayName("TC23: isValid(String bodyHtml, Safelist) returns false for disallowed tags")
    public void test_TC23() {
        // Inline comment: The input contains a <script> tag which is not allowed by the relaxed safelist, exercising isValidBodyHtml false branch.
        String html = "<script>alert(1)</script>";
        Safelist safelist = Safelist.relaxed();
        boolean valid = Jsoup.isValid(html, safelist);
        assertFalse(valid, "isValid should return false when disallowed tags are present");
    }
}