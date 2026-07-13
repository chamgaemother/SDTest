package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.jsoup.nodes.Document.OutputSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("TC18: clean(String, String, Safelist) with empty baseUri and safelist.preserveRelativeLinks=true retains relative href using DummyUri branch")
    void test_TC18() {
        String html = "<a href=\"page.html\">link</a>";
        String baseUri = "";
        Safelist safelist = Safelist.relaxed().preserveRelativeLinks(true);
        String result = Jsoup.clean(html, baseUri, safelist);
        String expectedPartial = "href=\"" + org.jsoup.internal.SharedConstants.DummyUri + "page.html\"";
        assertTrue(result.contains(expectedPartial),
                "Expected relative link preserved with DummyUri prefix");
    }

    @Test
    @DisplayName("TC19: clean(String, Safelist) default preserveRelativeLinks removes relative href attribute branch")
    void test_TC19() {
        String html = "<a href=\"other.html\">x</a>";
        Safelist safelist = Safelist.basic();
        String result = Jsoup.clean(html, safelist);
        assertFalse(result.contains("href"),
                "Expected relative href attribute removed when preserveRelativeLinks is false");
    }

    @Test
    @DisplayName("TC20: clean(String, String, Safelist, OutputSettings) applies custom OutputSettings branch")
    void test_TC20() {
        String html = "<p>1 &lt; 2</p>";
        String baseUri = "";
        Safelist safelist = Safelist.none();
        OutputSettings settings = new OutputSettings().prettyPrint(false);
        String result = Jsoup.clean(html, baseUri, safelist, settings);
        assertAll("OutputSettings effects",
                () -> assertFalse(result.contains("\n"), "Expected no pretty-print newlines"),
                () -> assertTrue(result.contains("&lt;"), "Expected raw entity &lt; preserved"));
    }

    @Test
    @DisplayName("TC21: isValid(String, Safelist) returns true for HTML matching safelist branch")
    void test_TC21() {
        String html = "<p>ok</p>";
        Safelist safelist = Safelist.basic();
        boolean valid = Jsoup.isValid(html, safelist);
        assertTrue(valid, "Expected isValid to return true for allowed <p> tag");
    }

    @Test
    @DisplayName("TC22: isValid(String, Safelist) returns false when input has disallowed tags branch")
    void test_TC22() {
        String html = "<script>alert()</script>";
        Safelist safelist = Safelist.basic();
        boolean valid = Jsoup.isValid(html, safelist);
        assertFalse(valid, "Expected isValid to return false for disallowed <script> tag");
    }

    @Test
    @DisplayName("TC23: parse(File, String, String) throws IOException for invalid charsetName branch")
    void test_TC23() throws IOException {
        Path temp = Files.createTempFile("f", ".html");
        File file = temp.toFile();
        Files.write(temp, "<p>x</p>".getBytes());
        String charsetName = "BAD-CHARSET";
        String baseUri = "http://u";
        assertThrows(IOException.class,
                () -> Jsoup.parse(file, charsetName, baseUri),
                "Expected IOException for unsupported charset");
    }

    @Test
    @DisplayName("TC24: parse(File) reads .gz file and parses gzipped HTML branch")
    void test_TC24() throws IOException {
        Path p = Files.createTempFile("t", ".html.gz");
        try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(p))) {
            out.write("<i>GZ</i>".getBytes("UTF-8"));
        }
        File gz = p.toFile();
        Document doc = Jsoup.parse(gz);
        assertEquals("<i>GZ</i>", doc.body().html(),
                "Expected gzipped HTML to be uncompressed and parsed");
    }

    @Test
    @DisplayName("TC25: parseBodyFragment(String, String) sets baseUri and wraps fragment in body branch")
    void test_TC25() {
        String fragment = "<span>F</span>";
        String baseUri = "http://x/";
        Document doc = Jsoup.parseBodyFragment(fragment, baseUri);
        assertAll("BodyFragment with baseUri",
                () -> assertEquals(fragment, doc.body().html(), "Expected body HTML equals fragment"),
                () -> assertEquals(baseUri, doc.baseUri(), "Expected baseUri preserved"));
    }

    @Test
    @DisplayName("TC26: parseBodyFragment(String) uses empty baseUri branch")
    void test_TC26() {
        String fragment = "<u>U2</u>";
        Document doc = Jsoup.parseBodyFragment(fragment);
        assertAll("BodyFragment default baseUri",
                () -> assertEquals("", doc.baseUri(), "Expected default baseUri is empty"),
                () -> assertEquals(fragment, doc.body().html(), "Expected body HTML equals fragment"));
    }
}