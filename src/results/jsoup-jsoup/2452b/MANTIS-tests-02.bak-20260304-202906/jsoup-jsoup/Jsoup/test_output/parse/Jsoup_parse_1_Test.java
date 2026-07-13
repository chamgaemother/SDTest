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
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(html non-empty) uses default parser and empty baseUri")
    public void test_TC11() {
        String html = "<span>X</span>";
        Document doc = Jsoup.parse(html);
        assertEquals("X", doc.select("span").text(), "Content should be parsed by default HTML parser");
        assertEquals("", doc.baseUri(), "Default baseUri should be empty string");
    }

    @Test
    @DisplayName("parse(html non-empty, Parser) uses provided parser and empty baseUri")
    public void test_TC12() {
        String html = "<node/>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals(1, doc.select("node").size(), "XML parser should recognize self-closing node element");
        assertEquals("", doc.baseUri(), "BaseUri should remain empty when not provided");
    }

    @Test
    @DisplayName("parse(html null) throws IllegalArgumentException in single-arg overload")
    public void test_TC13() {
        String html = null;
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html),
                "Parsing null HTML should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("parse(File file exists) determines charset from BOM/meta and uses file absolute path")
    public void test_TC14() throws IOException {
        File tmp = File.createTempFile("p14", ".html");
        tmp.deleteOnExit();
        String content = "<p>Z</p>";
        Files.write(tmp.toPath(), content.getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(tmp);
        assertEquals("Z", doc.select("p").text(), "File-based parse should read element text correctly");
        assertEquals(tmp.getAbsolutePath(), doc.baseUri(), "BaseUri should be the file's absolute path");
    }

    @Test
    @DisplayName("parse(File,charset,baseUri,Parser) uses custom parser and explicit baseUri")
    public void test_TC15() throws IOException {
        File tmp = File.createTempFile("p15", ".html");
        tmp.deleteOnExit();
        String content = "<t>Y</t>";
        Files.write(tmp.toPath(), content.getBytes(StandardCharsets.UTF_8));
        String cs = "UTF-8";
        String base = "http://base/";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(tmp, cs, base, parser);
        assertEquals("Y", doc.select("t").text(), "Custom parser should recognize element t");
        assertEquals(base, doc.baseUri(), "BaseUri should match provided value");
    }

    @Test
    @DisplayName("parse(Path,charset,baseUri) uses Path overload and custom charset and URI")
    public void test_TC16() throws IOException {
        Path path = Files.createTempFile("p16", ".html");
        path.toFile().deleteOnExit();
        String html = "<div>P</div>";
        Files.write(path, html.getBytes(StandardCharsets.UTF_8));
        String cs = "UTF-8";
        String base = "https://uri/";
        Document doc = Jsoup.parse(path, cs, base);
        assertEquals("P", doc.select("div").text(), "Path-based parse should read div content");
        assertEquals(base, doc.baseUri(), "BaseUri should match provided URI");
    }

    @Test
    @DisplayName("parse(Path,Parser overload) throws IOException for non-existent file")
    public void test_TC17() {
        Path path = Paths.get("does-not-exist.html");
        assertThrows(IOException.class, () -> Jsoup.parse(path),
                "Parsing non-existent path should throw IOException");
    }

    @Test
    @DisplayName("parse(InputStream,charset,baseUri,Parser) closes stream and uses parser")
    public void test_TC18() throws IOException {
        byte[] data = "<em>E</em>".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        String cs = "UTF-8";
        String base = "http://u/";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(in, cs, base, parser);
        assertEquals("E", doc.select("em").text(), "Stream-based parse should process element em");
        assertEquals(base, doc.baseUri(), "BaseUri should match provided URI");
        // ensure stream is closed: reading further should yield -1
        assertEquals(-1, in.read(), "InputStream should be closed after parsing");
    }
}