package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(File, charsetName, baseUri, parser) delegates to DataUtil.load with provided Parser")
    void test_TC18() throws IOException {
        // GIVEN: temp file with known HTML content
        Path tempPath = Files.createTempFile("test", ".html");
        String html = "<html><body><p>X</p></body></html>";
        Files.write(tempPath, html.getBytes(StandardCharsets.UTF_8));
        File tempFile = tempPath.toFile();
        String charset = "UTF-8";
        String baseUri = "http://base/";
        // stub parser records calls
        final StringBuilder recordedHtml = new StringBuilder();
        final StringBuilder recordedBase = new StringBuilder();
        Parser stub = Parser.htmlParser(); // Changed to use public static method
        // WHEN: parse with stub parser
        Document doc = Jsoup.parse(tempFile, charset, baseUri, stub);
        // THEN: stub parser was invoked with file contents and provided baseUri
        assertTrue(recordedHtml.toString().contains("<p>X</p>"), "Parser.parseInput should receive file HTML");
        assertEquals(baseUri, recordedBase.toString(), "Parser.parseInput should receive the provided baseUri");
        // returned Document should come from stub, so its baseUri() equals stub's baseUri in constructor
        assertEquals("stub", doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri) loads file via DataUtil.load")
    void test_TC19() throws IOException {
        // GIVEN: temp path with body containing 'Y'
        Path path = Files.createTempFile("file", ".html");
        String html = "<html><body>Y</body></html>";
        Files.write(path, html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "https://example.org/";
        // WHEN: parse using path overload
        Document doc = Jsoup.parse(path, charset, baseUri);
        // THEN: body text and baseUri as provided
        assertEquals("Y", doc.body().text());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path, charsetName) infers baseUri from path and charsetName provided")
    void test_TC20() throws IOException {
        // GIVEN: temp path with body 'Z'
        Path path = Files.createTempFile("file", ".html");
        String html = "<html><body>Z</body></html>";
        Files.write(path, html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        // WHEN: parse with default baseUri
        Document doc = Jsoup.parse(path, charset);
        // THEN: body text matches and baseUri infers absolute path
        assertEquals("Z", doc.body().text());
        assertEquals(path.toAbsolutePath().toString(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path) infers charset null and baseUri from path")
    void test_TC21() throws IOException {
        // GIVEN: temp path with body 'A'
        Path path = Files.createTempFile("file", ".html");
        String html = "<html><body>A</body></html>";
        Files.write(path, html.getBytes(StandardCharsets.UTF_8));
        // WHEN: parse with only path
        Document doc = Jsoup.parse(path);
        // THEN: body text matches and baseUri infers absolute path
        assertEquals("A", doc.body().text());
        assertEquals(path.toAbsolutePath().toString(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(File, charsetName) with invalid charsetName throws IOException")
    void test_TC22() throws IOException {
        // GIVEN: temp file with basic content
        Path tempPath = Files.createTempFile("test", ".html");
        String html = "<p>B</p>";
        Files.write(tempPath, html.getBytes(StandardCharsets.UTF_8));
        File tempFile = tempPath.toFile();
        String badCharset = "BAD-CHARSET";
        // WHEN/THEN: expect IOException for unsupported charset
        assertThrows(IOException.class, () -> Jsoup.parse(tempFile, badCharset));
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) reads stream and closes it")
    void test_TC23() throws IOException {
        // GIVEN: InputStream with body 'S'
        byte[] bytes = "<html><body>S</body></html>".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(bytes);
        String charset = "UTF-8";
        String baseUri = "http://b/";
        // WHEN: parse stream overload
        Document doc = Jsoup.parse(in, charset, baseUri);
        // THEN: body text and baseUri match
        assertEquals("S", doc.body().text());
        assertEquals(baseUri, doc.baseUri());
        // Note: stream should be closed inside DataUtil.load, test only observable behavior
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri, parser) uses custom parser on stream")
    void test_TC24() throws IOException {
        // GIVEN: InputStream with fragment '<p>T</p>'
        byte[] bytes = "<p>T</p>".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(bytes);
        String charset = "UTF-8";
        String baseUri = "";
        final StringBuilder recorded = new StringBuilder();
        Parser stub = Parser.htmlParser(); // Changed to use public static method
        // WHEN: parse with stub parser
        Document doc = Jsoup.parse(in, charset, baseUri, stub);
        // THEN: stub parser received input containing '<p>T</p>' and result from stub used
        assertTrue(recorded.toString().contains("<p>T</p>"), "Parser.parseInput should see stream HTML");
        assertEquals("out", doc.baseUri());
    }

    @Test
    @DisplayName("parseBodyFragment(bodyHtml, baseUri) parses fragment and sets baseUri")
    void test_TC25() {
        // GIVEN: fragment with relative link, baseUri provided
        String frag = "<div><a href=one.html>link</a></div>";
        String baseUri = "http://u/";
        // WHEN: parseBodyFragment with baseUri
        Document doc = Jsoup.parseBodyFragment(frag, baseUri);
        // THEN: fragment present and absUrl resolves with baseUri
        assertTrue(doc.body().html().contains("<div>"));
        String abs = doc.select("a").get(0).absUrl("href");
        assertTrue(abs.startsWith(baseUri), "absUrl should resolve using provided baseUri");
    }

    @Test
    @DisplayName("parseBodyFragment(bodyHtml) uses empty baseUri")
    void test_TC26() {
        // GIVEN: fragment with relative link, no baseUri
        String frag = "<a href=two.html>l</a>";
        // WHEN: parseBodyFragment default
        Document doc = Jsoup.parseBodyFragment(frag);
        // THEN: link href remains unresolved
        assertTrue(doc.body().html().contains("two.html"));
        String abs = doc.select("a").get(0).absUrl("href");
        assertTrue(abs.isEmpty(), "absUrl should be empty when no baseUri provided");
    }
}