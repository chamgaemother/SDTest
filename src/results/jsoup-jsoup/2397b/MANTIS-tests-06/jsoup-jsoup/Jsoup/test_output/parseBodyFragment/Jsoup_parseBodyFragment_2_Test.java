package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parseBodyFragment_2_Test {

    @Test
    @DisplayName("parseBodyFragment(html,baseUri) ignores <base> tag for resolution when baseUri is non-empty")
    public void test_TC14() {
        // This input includes a <base> tag but supplies a non-empty baseUri, hitting branch B1→B3 to ignore <base> for resolution.
        String html = "<base href='http://ignore.com/sub/'><a href='rel.html'>link</a>";
        String baseUri = "http://use.me/";
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        String output = doc.body().html();
        // The <base> tag should still appear in the output
        assertTrue(output.contains("<base href=\"http://ignore.com/sub/\">"),
            "Expected base tag to be retained but not used for resolution");
        // The <a> href should be resolved against provided baseUri, not the <base> tag
        assertTrue(output.contains("<a href=\"http://use.me/rel.html\">link</a>"),
            "Expected link href to be resolved against provided baseUri");
    }

    @Test
    @DisplayName("parseBodyFragment(html,baseUri) resolves multiple '..' segments with iteration count >1")
    public void test_TC15() {
        // This input uses '../../dir/page.html', causing two iterations of '..' resolution (branch B2 loop×2).
        String html = "<a href='../../dir/page.html'>back</a>";
        String baseUri = "http://ex.com/a/b/c/";
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // Expect navigation up two levels: from /a/b/c/ → /a/ then append dir/page.html
        assertEquals("<a href=\"http://ex.com/a/dir/page.html\">back</a>",
                     doc.body().html(),
                     "Expected '../..' to traverse up two directories correctly");
    }

    @Test
    @DisplayName("parseBodyFragment(html) single-arg overload disallows resolution and leaves relative src unchanged when baseUri is empty")
    public void test_TC16() {
        // Single-arg overload uses empty baseUri, hitting branch B4→B8 and should leave relative/src and protocol-relative unchanged.
        String html = "<img src='img.png'><video src='//cdn/video.mp4'></video>";
        Document doc = Jsoup.parseBodyFragment(html);
        // Since no baseUri provided and no <base> tag, relative paths remain as-is
        assertEquals("<img src=\"img.png\"><video src=\"//cdn/video.mp4\"></video>",
                     doc.body().html(),
                     "Expected relative and protocol-relative src attributes to remain unchanged");
    }
}