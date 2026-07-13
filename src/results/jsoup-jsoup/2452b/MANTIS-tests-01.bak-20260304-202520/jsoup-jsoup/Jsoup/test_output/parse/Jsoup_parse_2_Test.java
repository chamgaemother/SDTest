package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(File, charsetName, baseUri) throws IOException on invalid charsetName")
    public void test_TC20() throws IOException {
        // Create a temporary HTML file to satisfy precondition; content is valid HTML
        File tmp = File.createTempFile("tc20", ".html");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "<p>X</p>".getBytes());
        String badCharset = "INVALID-CHARSET";
        String baseUri = "http://example.com/";
        // Expect an IOException because the charset is unsupported (boundary path B1→B3→B6)
        assertThrows(IOException.class, () -> Jsoup.parse(tmp, badCharset, baseUri));
    }

    @Test
    @DisplayName("parse(File, charsetName) throws IOException on invalid charsetName")
    public void test_TC21() throws IOException {
        // Create a temporary HTML file to satisfy precondition; content is valid HTML
        File tmp = File.createTempFile("tc21", ".html");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "<span>Y</span>".getBytes());
        String badCharset = "NO-SUCH-ENCODING";
        // Default baseUri is the file's absolute path; charset invalid triggers exception
        assertThrows(IOException.class, () -> Jsoup.parse(tmp, badCharset));
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri) throws IOException on invalid charsetName")
    public void test_TC22() throws IOException {
        // Create a temporary HTML file and use its Path; valid HTML content
        Path p = Files.createTempFile("tc22", ".html");
        p.toFile().deleteOnExit();
        Files.write(p, "<p>Z</p>".getBytes());
        String badCharset = "UNKNOWN-ENC";
        String baseUri = "http://host/";
        // Unsupported charset on Path-based overload should throw IOException
        assertThrows(IOException.class, () -> Jsoup.parse(p, badCharset, baseUri));
    }

    @Test
    @DisplayName("parse(Path, charsetName) throws IOException on invalid charsetName")
    public void test_TC23() throws IOException {
        // Create a temporary HTML file and use its Path; valid HTML content
        Path p = Files.createTempFile("tc23", ".html");
        p.toFile().deleteOnExit();
        Files.write(p, "<h1>H</h1>".getBytes());
        String badCharset = "BAD-CHARSET";
        // Default baseUri is path.toAbsolutePath().toString(); invalid charset should fail
        assertThrows(IOException.class, () -> Jsoup.parse(p, badCharset));
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) throws IOException on invalid charsetName")
    public void test_TC24() {
        // InputStream provides valid HTML bytes; charset unsupported should trigger IOException
        InputStream in = new ByteArrayInputStream("<li>L</li>".getBytes());
        String badCharset = "UNKNOWN";
        String baseUri = "http://u/";
        assertThrows(IOException.class, () -> Jsoup.parse(in, badCharset, baseUri));
    }

    @Test
    @DisplayName("parse(String, Parser) with htmlParser resolves relative links via empty baseUri")
    public void test_TC25() {
        // No baseUri provided so empty; relative href should remain unchanged
        String html = "<a href='r.html'>R</a>";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, parser);
        // The href is relative and no baseUri => attr("href") is the original
        assertEquals("r.html", doc.select("a").first().attr("href"));
    }

    @Test
    @DisplayName("parse(String, String, Parser) with htmlParser resolves baseUri on no <base> tag")
    public void test_TC26() {
        // Provided baseUri; no <base> tag in HTML => absUrl resolves against baseUri
        String html = "<a href='a.html'>A</a>";
        String baseUri = "http://base/";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        // Confirm that the absolute URL is correctly constructed
        assertEquals("http://base/a.html", doc.select("a").first().absUrl("href"));
    }
}