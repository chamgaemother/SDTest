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
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(html, baseUri, parser) with null parser throws IllegalArgumentException")
    public void test_TC16() {
        String html = "<p>X</p>";
        String baseUri = "http://x/";
        Parser parser = null;
        // Passing null parser should trigger argument validation
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, baseUri, parser));
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri, parser) with XML parser returns Document with supplied parser")
    public void test_TC17() throws IOException {
        // Create temp file with body containing <h3>F</h3>
        Path temp = Files.createTempFile("jsoup-test", ".html");
        temp.toFile().deleteOnExit();
        String content = "<h3>F</h3>";
        Files.write(temp, content.getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        String charset = "UTF-8";
        String baseUri = "https://base/";
        Parser xmlParser = Parser.xmlParser();
        // Using explicit parser branch should yield a doc respecting parser settings
        Document doc = Jsoup.parse(file, charset, baseUri, xmlParser);
        assertAll(
            () -> assertEquals(baseUri, doc.baseUri(), "Base URI should match provided"),
            () -> assertEquals("F", doc.select("h3").first().text(), "Parsed element text should match input")
        );
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri) for existing path returns Document")
    public void test_TC18() throws IOException {
        // Create temp file at Path containing <p>P</p>
        Path temp = Files.createTempFile("jsoup-test-path", ".html");
        temp.toFile().deleteOnExit();
        String content = "<p>P</p>";
        Files.write(temp, content.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://u/";
        // Should select the overload reading from Path
        Document doc = Jsoup.parse(temp, charset, baseUri);
        assertAll(
            () -> assertTrue(doc.body().html().contains("<p>P</p>"), "Body HTML should contain the paragraph"),
            () -> assertEquals(baseUri, doc.baseUri(), "Base URI should be as provided")
        );
    }

    @Test
    @DisplayName("parse(Path, charsetName) with null charsetName defaults to UTF-8 and baseUri from path")
    public void test_TC19() throws IOException {
        // Create temp file with <div>D</div>
        Path temp = Files.createTempFile("jsoup-test-path-null", ".html");
        temp.toFile().deleteOnExit();
        String content = "<div>D</div>";
        Files.write(temp, content.getBytes(StandardCharsets.UTF_8));
        String charset = null;
        // Passing null charset should hit default branch for BOM/UTF-8 and derive baseUri from path
        Document doc = Jsoup.parse(temp, charset);
        assertAll(
            () -> assertTrue(doc.body().html().contains("<div>D</div>"), "Body HTML should contain the div"),
            () -> assertEquals(temp.toAbsolutePath().toString(), doc.baseUri(), "Base URI should be path's absolute string")
        );
    }

    @Test
    @DisplayName("parse(Path) with missing file throws IOException")
    public void test_TC20() {
        Path missing = Paths.get("no-such-file.html");
        // Nonexistent path should cause IOException in load
        assertThrows(IOException.class, () -> Jsoup.parse(missing));
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) without parser uses default HTML parser")
    public void test_TC21() throws IOException {
        String htmlFragment = "<li>Item</li>";
        InputStream in = new ByteArrayInputStream(htmlFragment.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://stream/";
        // Stream overload without explicit parser should choose HTML parser branch
        Document doc = Jsoup.parse(in, charset, baseUri);
        assertAll(
            () -> assertTrue(doc.body().html().contains("<li>Item</li>"), "Body should contain list item"),
            () -> assertEquals(baseUri, doc.baseUri(), "Base URI should be as provided")
        );
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) with corrupt stream throws IOException")
    public void test_TC22() {
        InputStream corrupt = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("stream error");
            }
        };
        String charset = "UTF-8";
        String baseUri = "http://x/";
        // Reading from corrupt stream should propagate IOException
        assertThrows(IOException.class, () -> Jsoup.parse(corrupt, charset, baseUri));
    }
}