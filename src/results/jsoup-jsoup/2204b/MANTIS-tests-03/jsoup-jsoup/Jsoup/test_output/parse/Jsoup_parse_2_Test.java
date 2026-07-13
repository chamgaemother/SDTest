package org.jsoup;

import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

// Added import for Connection class
import org.jsoup.Connection;

public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC18: parseBodyFragment(html, baseUri) preserves body fragment and sets baseUri")
    void test_TC18() {
        String fragment = "<p>F</p>";
        String baseUri = "http://f.test/";
        Document doc = Jsoup.parseBodyFragment(fragment, baseUri);
        assertTrue(doc.body().html().contains("<p>F</p>"));
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("TC19: parseBodyFragment(html) uses empty baseUri by default")
    void test_TC19() {
        String fragment = "<div>X</div>";
        Document doc = Jsoup.parseBodyFragment(fragment);
        assertTrue(doc.body().html().contains("<div>X</div>"));
        assertEquals("", doc.baseUri());
    }

    @Test
    @DisplayName("TC20: clean(html, baseUri, safelist) removes disallowed tags and preserves allowed ones")
    void test_TC20() {
        String dirty = "<b>Ok</b><script>alert()</script>";
        Safelist safelist = Safelist.basic();
        String clean = Jsoup.clean(dirty, safelist);
        assertTrue(clean.contains("<b>Ok</b>"));
        assertFalse(clean.toLowerCase().contains("script"));
    }

    @Test
    @DisplayName("TC21: clean(html, safelist) delegates to clean(html, safelist)")
    void test_TC21() {
        String dirty = "<i>Ital</i><img src='/r.png'>";
        Safelist safelist = Safelist.simpleText();
        String clean = Jsoup.clean(dirty, safelist);
        assertTrue(clean.contains("<i>Ital</i>"));
        assertFalse(clean.toLowerCase().contains("img"));
    }

    @Test
    @DisplayName("TC22: clean(html, baseUri, safelist, outputSettings) applies custom outputSettings")
    void test_TC22() {
        String dirty = "<div> A </div>";
        String baseUri = "buri";
        Safelist safelist = Safelist.none();
        Document.OutputSettings os = new Document.OutputSettings().prettyPrint(false);
        String clean = Jsoup.clean(dirty, safelist, os);
        assertEquals("A", clean);
    }

    @Test
    @DisplayName("TC23: isValid(html, safelist) returns true when content conforms")
    void test_TC23() {
        String safe = "<p>Text</p>";
        Safelist safelist = Safelist.basic();
        boolean ok = Jsoup.isValid(safe, safelist);
        assertTrue(ok);
    }

    @Test
    @DisplayName("TC24: isValid(html, safelist) returns false when disallowed tags present")
    void test_TC24() {
        String unsafe = "<iframe src=\"x\"></iframe>";
        Safelist safelist = Safelist.basic();
        boolean ok = Jsoup.isValid(unsafe, safelist);
        assertFalse(ok);
    }

    @Test
    @DisplayName("TC25: parse(File, invalid charset) throws IOException on bad charsetName")
    void test_TC25() throws IOException {
        File f = File.createTempFile("badcs", ".html");
        f.deleteOnExit();
        Files.write(f.toPath(), "<p>X</p>".getBytes("UTF-8"));
        String badCs = "UNKNOWN_CHARSET";
        assertThrows(IOException.class, () -> Jsoup.parse(f, badCs));
    }

    @Test
    @DisplayName("TC26: connect(http URL) returns a HttpConnection instance")
    void test_TC26() {
        String url = "http://example.com";
        Connection con = Jsoup.connect(url);
        assertTrue(con instanceof HttpConnection);
        HttpConnection hcon = (HttpConnection) con;
        assertEquals(url, hcon.request().url().toExternalForm());
    }

    @Test
    @DisplayName("TC27: newSession() returns independent HttpConnection session")
    void test_TC27() {
        Connection s1 = Jsoup.newSession();
        Connection s2 = Jsoup.newSession();
        s1.userAgent("A").timeout(100);
        s2.userAgent("B").timeout(200);
        assertNotEquals(s1.request().userAgent(), s2.request().userAgent());
        assertEquals(100, s1.request().timeoutMillis());
        assertEquals(200, s2.request().timeoutMillis());
    }
}