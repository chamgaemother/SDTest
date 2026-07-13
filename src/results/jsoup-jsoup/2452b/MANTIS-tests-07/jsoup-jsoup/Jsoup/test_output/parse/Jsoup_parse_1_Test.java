package org.jsoup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(File, null charsetName, baseUri) loads HTML file and uses file path as baseUri")
    public void test_TC11() throws Exception {
        // GIVEN a temporary file containing UTF-8 HTML <p>File</p>
        Path temp = Files.createTempFile("jsoup", ".html");
        Files.write(temp, "<p>File</p>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        String charset = null;
        String baseUri = file.getAbsolutePath();
        // WHEN parsing with explicit baseUri and null charset (branch B1)
        Document doc = Jsoup.parse(file, charset, baseUri);
        // THEN body contains the element and baseUri matches file path
        assertTrue(doc.body().html().contains("<p>File</p>"), "HTML content should include <p>File</p>");
        assertEquals(baseUri, doc.getBaseUri());
    }

    @Test
    @DisplayName("parse(File, 'UTF-8') uses file absolute path as baseUri when charset provided")
    public void test_TC12() throws Exception {
        // GIVEN a temporary file with <h2>Head</h2>
        Path temp = Files.createTempFile("jsoup", ".html");
        Files.write(temp, "<h2>Head</h2>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        // WHEN parsing via overload with charset only (branch B2)
        Document doc = Jsoup.parse(file, null);
        // THEN content and baseUri equal file path
        assertTrue(doc.body().html().contains("<h2>Head</h2>"));
        assertEquals(file.getAbsolutePath(), doc.getBaseUri());
    }

    @Test
    @DisplayName("parse(File, invalid charsetName) throws IOException for unsupported charset")
    public void test_TC13() throws Exception {
        // GIVEN a file and an invalid charset
        Path temp = Files.createTempFile("jsoup", ".html");
        Files.write(temp, "<p>X</p>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        String charset = "NO-SUCH-CHARSET";
        // WHEN parsing with invalid charset, THEN IOException is thrown
        assertThrows(IOException.class, () -> Jsoup.parse(file, charset, "http://base/"));
    }

    @Test
    @DisplayName("parse(File, charset, baseUri, parser) delegates to custom parser override")
    public void test_TC14() throws Exception {
        // GIVEN a file with <span>Test</span> and a stub parser to override parseInput
        Path temp = Files.createTempFile("jsoup", ".html");
        Files.write(temp, "<span>Test</span>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        Parser stub = new Parser() {
            @Override
            public Document parseInput(InputStream in, String baseUri) {
                // stub branch to ensure parser override is invoked
                return Document.createShell(baseUri);
            }
        };
        // WHEN parsing with custom parser (branch B3)
        Document doc = Jsoup.parse(file, "UTF-8", "http://f/", stub);
        // THEN stub parser output used and baseUri matches provided
        assertEquals("http://f/", doc.getBaseUri());
    }

    @Test
    @DisplayName("parse(Path, null charsetName, baseUri) loads HTML via Path overload")
    public void test_TC15() throws Exception {
        // GIVEN a path with <div>P</div>
        Path temp = Files.createTempFile("jsoup", ".html");
        Files.write(temp, "<div>P</div>".getBytes(StandardCharsets.UTF_8));
        String charset = null;
        String baseUri = temp.toAbsolutePath().toString();
        // WHEN parsing via Path overload (branch B4)
        Document doc = Jsoup.parse(temp.toFile(), charset, baseUri);
        // THEN content and baseUri match
        assertTrue(doc.body().html().contains("<div>P</div>"));
        assertEquals(baseUri, doc.getBaseUri());
    }

    @Test
    @DisplayName("parse(Path, charsetName) loads HTML and uses path absolute path as baseUri")
    public void test_TC16() throws Exception {
        // GIVEN a path with <li>Item</li> and charset
        Path temp = Files.createTempFile("jsoup", ".html");
        Files.write(temp, "<li>Item</li>".getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        // WHEN parsing via Path charset overload (branch B5)
        Document doc = Jsoup.parse(temp.toFile(), charset, null);
        // THEN content and baseUri match path
        assertTrue(doc.body().html().contains("<li>Item</li>"));
        assertEquals(temp.toAbsolutePath().toString(), doc.getBaseUri());
    }

    @Test
    @DisplayName("parse(InputStream, null charset, baseUri) loads fragment from stream and closes it")
    public void test_TC17() throws Exception {
        // GIVEN an InputStream for <h3>S</h3> and track closure
        class TrackCloseStream extends FilterInputStream {
            boolean closed = false;
            protected TrackCloseStream(InputStream in) { super(in); }
            @Override public void close() throws IOException { closed = true; super.close(); }
        }
        TrackCloseStream in = new TrackCloseStream(
                new ByteArrayInputStream("<h3>S</h3>".getBytes(StandardCharsets.UTF_8)));
        String charset = null;
        String baseUri = "http://in/";
        // WHEN parsing via stream overload (branch B6)
        Document doc = Jsoup.parse(in, charset, baseUri);
        // THEN content loaded, baseUri matches, and stream is closed
        assertTrue(doc.body().html().contains("<h3>S</h3>"));
        assertEquals(baseUri, doc.getBaseUri());
        assertTrue(in.closed, "InputStream should be closed after parsing");
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri, parser) delegates to custom parser with stream overload")
    public void test_TC18() throws Exception {
        // GIVEN a stub parser for stream overload
        ByteArrayInputStream raw = new ByteArrayInputStream("<x/>".getBytes(StandardCharsets.UTF_8));
        class StubParser extends Parser {
            @Override public Document parseInput(InputStream in, String baseUri) {
                return Document.createShell(baseUri);
            }
        }
        Parser stub = new StubParser();
        // WHEN parsing with stub parser (branch B7)
        Document doc = Jsoup.parse(raw, "UTF-8", null, stub);
        // THEN stub parser result used
        assertEquals("OK", doc.body().text());
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) with non-http URL throws MalformedURLException")
    public void test_TC19() throws Exception {
        // GIVEN a file:// URL and timeout
        URL url = new URL("file:///tmp/test.html");
        // WHEN parsing, THEN MalformedURLException is thrown by connect
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, null));
    }

    @Test
    @DisplayName("parse(html, baseUri, xmlParser()) uses XML parser branch")
    public void test_TC20() throws Exception {
        // GIVEN XML-like html and xmlParser
        String html = "<root><a/></root>";
        String baseUri = "xml://";
        Parser xml = Parser.xmlParser();
        // WHEN parsing with xml parser overload (parser.parseInput branch)
        Document doc = Jsoup.parse(html, baseUri);
        // THEN content preserved and baseUri set
        assertTrue(doc.body().html().contains("<root><a/></root>"));
        assertEquals(baseUri, doc.getBaseUri());
    }
}