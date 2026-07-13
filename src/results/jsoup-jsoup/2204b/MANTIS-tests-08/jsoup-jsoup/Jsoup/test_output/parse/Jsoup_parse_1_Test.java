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

public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(html, parser) uses provided Parser to produce custom Document")
    public void test_TC11() {
        String html = "<x>Y</x>";
        Parser stub = Parser.htmlParser(); // Changed to use public factory method
        Document doc = Jsoup.parse(html, stub);
        assertEquals("stubbed", doc.title(), "Expected stub parser to set title to 'stubbed'");
    }

    @Test
    @DisplayName("parse(html, baseUri, parser) delegates correctly to provided Parser")
    public void test_TC12() {
        String html = "<a/>";
        String base = "http://u/";
        class Rec extends Parser {
            String lastIn, lastBase;
            Rec() { } // Removed constructor call to super
            @Override public Document parseInput(String in, String baseUri) {
                this.lastIn = in;
                this.lastBase = baseUri;
                return Document.createShell(baseUri);
            }
        }
        Rec rec = new Rec();
        Jsoup.parse(html, base, rec);
        assertAll("Expected parser to receive correct html and baseUri",
            () -> assertEquals(html, rec.lastIn),
            () -> assertEquals(base, rec.lastBase)
        );
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri) loads file and sets correct baseUri")
    public void test_TC13() throws IOException {
        File f = File.createTempFile("jsoup-test13", ".html");
        f.deleteOnExit();
        String content = "<p>f</p>";
        Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://base/";
        Document doc = Jsoup.parse(f, charset, baseUri);
        assertAll("Document should reflect provided baseUri and content",
            () -> assertEquals(baseUri, doc.baseUri()),
            () -> assertTrue(doc.body().html().contains("f"))
        );
    }

    @Test
    @DisplayName("parse(File, charsetName) loads file and uses file path as baseUri")
    public void test_TC14() throws IOException {
        File f = File.createTempFile("jsoup-test14", ".html");
        f.deleteOnExit();
        String content = "<div>z</div>";
        Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        Document doc = Jsoup.parse(f, charset);
        assertAll("Document baseUri should equal file.getAbsolutePath and body contains 'z'",
            () -> assertEquals(f.getAbsolutePath(), doc.baseUri()),
            () -> assertTrue(doc.body().html().contains("z"))
        );
    }

    @Test
    @DisplayName("parse(File) loads file with null charset and uses file path as baseUri")
    public void test_TC15() throws IOException {
        File f = File.createTempFile("jsoup-test15", ".html");
        f.deleteOnExit();
        String content = "<span>n</span>";
        Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(f);
        assertAll("Document baseUri should equal file.getAbsolutePath and body contains 'n'",
            () -> assertEquals(f.getAbsolutePath(), doc.baseUri()),
            () -> assertTrue(doc.body().html().contains("n"))
        );
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri, parser) delegates to DataUtil.load with parser")
    public void test_TC16() throws IOException {
        File f = File.createTempFile("jsoup-test16", ".html");
        f.deleteOnExit();
        Files.write(f.toPath(), "ok".getBytes(StandardCharsets.UTF_8));
        String charset = null;
        String base = "b";
        Parser stub = Parser.htmlParser(); // Changed to use public factory method
        Document doc = Jsoup.parse(f, charset, base, stub);
        assertEquals("p", doc.title(), "Expected stub parser in DataUtil.load to set title 'p'");
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri) loads via Path and sets baseUri")
    public void test_TC17() throws IOException {
        Path p = Files.createTempFile("jsoup-test17", ".html");
        p.toFile().deleteOnExit();
        String content = "<u>q</u>";
        Files.write(p, content.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String base = "http://u/";
        Document doc = Jsoup.parse(p, charset, base);
        assertAll("Document baseUri should equal provided base and body contains 'q'",
            () -> assertEquals(base, doc.baseUri()),
            () -> assertTrue(doc.body().html().contains("q"))
        );
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) reads stream and applies charset and baseUri")
    public void test_TC18() throws IOException {
        byte[] data = "<h>1</h>".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        String charset = "UTF-8";
        String baseUri = "http://s/";
        Document doc = Jsoup.parse(in, charset, baseUri);
        assertAll("Document body should contain '1' and baseUri set",
            () -> assertTrue(doc.body().html().contains("1")),
            () -> assertEquals(baseUri, doc.baseUri())
        );
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri, parser) uses parser for InputStream overload")
    public void test_TC19() throws IOException {
        InputStream in = new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8));
        String charset = null;
        String base = "b";
        Parser stub = Parser.htmlParser(); // Changed to use public factory method
        Document doc = Jsoup.parse(in, charset, base, stub);
        assertEquals("z", doc.title(), "Expected custom parser to set title 'z'");
    }
}