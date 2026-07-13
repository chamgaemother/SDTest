package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Jsoup_parseBodyFragment_2_Test {

    @Test
    @DisplayName("TC08: one-arg overload with non-empty <base href> tag overrides empty baseUri and resolves links")
    public void test_TC08() {
        // This input contains a <base href> tag, so the one-arg overload should pick up the override instead of empty baseUri (B2: baseTagOverride)
        String bodyHtml = "<base href='http://override.com/sub/'><a href='p.html'>x</a>";
        Document doc = Jsoup.parseBodyFragment(bodyHtml);
        // Verify that the link resolves against the override base href, not the empty default
        String abs = doc.body().selectFirst("a").absUrl("href");
        assertEquals("http://override.com/sub/p.html", abs);
    }

    @Test
    @DisplayName("TC09: two-arg overload with <base> tag missing href attribute falls back to provided baseUri")
    public void test_TC09() {
        // The <base> tag has no href, so parser sees no override (B2: emptyBaseTag) and should fall back to provided baseUri
        String bodyHtml = "<base><a href='page.html'>link</a>";
        String baseUri = "https://example.com/root/";
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        // Verify fallback to the supplied baseUri
        String abs = doc.body().selectFirst("a").absUrl("href");
        assertEquals("https://example.com/root/page.html", abs);
    }

    @Test
    @DisplayName("TC10: one-arg overload with uppercase <BASE HREF> tag overrides empty baseUri case-insensitively")
    public void test_TC10() {
        // Uppercase BASE HREF should be treated the same as lowercase: override detected (B2: baseTagOverride, case-insensitive)
        String bodyHtml = "<BASE HREF='http://X.com/'><a href='r'>f</a>";
        Document doc = Jsoup.parseBodyFragment(bodyHtml);
        // Verify that uppercase BASE HREF was recognized and used for resolution
        String abs = doc.body().selectFirst("a").absUrl("href");
        assertEquals("http://X.com/r", abs);
    }
}