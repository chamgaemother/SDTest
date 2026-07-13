package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Generated JUnit 5 tests for org.jsoup.Jsoup.parse and related methods.
 * Each test is self-contained and uses reflection or static mocking as needed.
 */
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC17 parse(File, charsetName, baseUri, Parser) with xmlParser preserves tag case from file")
    void test_TC17() throws IOException {
        // prepare a temp file containing mixed-case tag <Tag>Value</Tag>
        File temp = File.createTempFile("JsoupTest", ".html");
        temp.deleteOnExit();
        try (FileWriter fw = new FileWriter(temp)) {
            fw.write("<Tag>Value</Tag>");
        }
        String charset = "UTF-8";
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        // WHEN
        Document doc = Jsoup.parse(temp, charset, baseUri, parser);
        // THEN: body html should preserve case
        String bodyHtml = doc.body().html();
        assertTrue(bodyHtml.contains("<Tag>Value</Tag>"),
            "Expected body HTML to contain the exact case-preserved tag");
    }

    @Test
    @DisplayName("TC18 parse(Path, charsetName, baseUri, Parser) with xmlParser preserves tag case from path")
    void test_TC18(@TempDir Path tempDir) throws IOException {
        // prepare a temp Path file containing mixed-case tag <X>1</X>
        Path file = Files.createTempFile(tempDir, "JsoupTestPath", ".html");
        Files.write(file, "<X>1</X>".getBytes("UTF-8"));
        String charset = "UTF-8";
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        // WHEN
        Document doc = Jsoup.parse(file, charset, baseUri, parser);
        // THEN: body html should preserve case <X>1</X>
        String bodyHtml = doc.body().html();
        assertTrue(bodyHtml.contains("<X>1</X>"),
            "Expected body HTML to contain the exact case-preserved tag");
    }

    @Test
    @DisplayName("TC19 parseBodyFragment(String) with default baseUri yields correct body element")
    void test_TC19() {
        // fragment parsing without baseUri should return the fragment as body HTML
        String html = "<div>f</div>";
        Document doc = Jsoup.parseBodyFragment(html);
        // the body html equals the input fragment
        assertEquals("<div>f</div>", doc.body().html(),
            "Expected body fragment to match input exactly");
    }

    @Test
    @DisplayName("TC20 parseBodyFragment(String, baseUri) resolves relative links in fragment")
    void test_TC20() {
        // relative link should be resolved against provided baseUri
        String html = "<a href=\"p.html\">p</a>";
        String baseUri = "http://host/";
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        String abs = doc.select("a").first().absUrl("href");
        assertEquals("http://host/p.html", abs,
            "Expected href to resolve against baseUri");
    }

    @Test
    @DisplayName("TC21 clean(String, Safelist.none()) escapes HTML entities and removes tags")
    void test_TC21() {
        // entity &lt; should become &amp;lt; and <p> removed
        String html = "<p>5 &lt; 6</p>";
        Safelist safelist = Safelist.none();
        String out = Jsoup.clean(html, safelist);
        assertAll(
            () -> assertTrue(out.contains("5 &amp;lt; 6"), "Expected entity to be escaped"),
            () -> assertFalse(out.contains("<p>"), "Expected tags to be stripped")
        );
    }

    @Test
    @DisplayName("TC22 clean(String, baseUri, Safelist, OutputSettings) applies output settings prettyPrint=false")
    void test_TC22() {
        // prettyPrint(false) should produce no newline in output
        String html = "<div>a</div>";
        String baseUri = "";
        Safelist safelist = Safelist.basic();
        Document.OutputSettings os = new Document.OutputSettings().prettyPrint(false);
        String out = Jsoup.clean(html, baseUri, safelist, os);
        assertFalse(out.contains("\n"), "Expected no newline when prettyPrint is false");
    }

    @Test
    @DisplayName("TC23 isValid(String, Safelist) returns false for disallowed tags and true for safe content")
    void test_TC23() {
        // <script> is not allowed in basic safelist, <p> is allowed
        String bad = "<script>alert(1)</script>";
        String good = "<p>ok</p>";
        Safelist safelist = Safelist.basic();
        boolean vBad = Jsoup.isValid(bad, safelist);
        boolean vGood = Jsoup.isValid(good, safelist);
        assertAll(
            () -> assertFalse(vBad, "Expected script tag to be invalid"),
            () -> assertTrue(vGood, "Expected p tag to be valid")
        );
    }

    @Test
    @DisplayName("TC24 parse(File, charsetName, baseUri) with invalid charsetName throws IOException")
    void test_TC24() throws IOException {
        // unsupported charset should trigger IOException from DataUtil.load
        File temp = File.createTempFile("JsoupTestInvalidCharset", ".html");
        temp.deleteOnExit();
        try (FileWriter fw = new FileWriter(temp)) {
            fw.write("<div>ok</div>");
        }
        String invalid = "INVALID-CHARSET";
        String baseUri = "";
        assertThrows(IOException.class, () -> Jsoup.parse(temp, invalid, baseUri),
            "Expected IOException for invalid charset name");
    }

    @Test
    @DisplayName("TC25 parse(URL, timeoutMillis) happy path uses HttpConnection stub to return parsed Document")
    void test_TC25() throws Exception {
        // use Mockito to stub HttpConnection.connect and its get() method
        URL url = new URL("http://example.com/");
        Document stubDoc = Parser.parse("<b>ok</b>", "");
        try (MockedStatic<HttpConnection> httpMock = Mockito.mockStatic(HttpConnection.class)) {
            HttpConnection.Connection stubConn = Mockito.mock(HttpConnection.Connection.class);
            httpMock.when(() -> HttpConnection.connect(url)).thenReturn(stubConn);
            Mockito.when(stubConn.timeout(5000)).thenReturn(stubConn);
            Mockito.when(stubConn.get()).thenReturn(stubDoc);
            // WHEN
            Document doc = Jsoup.parse(url, 5000);
            // THEN: returned document matches stub body html
            assertEquals("<b>ok</b>", doc.body().html(),
                "Expected stubbed Document body HTML");
        }
    }
}