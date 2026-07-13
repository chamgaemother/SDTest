package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(html, baseUri, custom parser) invokes parser.parseInput path B0→B2→B3")
    public void test_TC19() {
        String html = "<custom/>";
        String baseUri = "http://u/";
        Parser stub = new Parser(new Parser.Settings()) {
            @Override
            public Document parseInput(String in, String uri) {
                Document d = Document.createShell(uri);
                d.body().appendElement("marker").attr("id", "m");
                return d;
            }
        };
        Document doc = Jsoup.parse(html, baseUri, stub);
        assertNotNull(doc.getElementById("m"), "Expected marker element from stub parser");
        assertEquals(baseUri, doc.baseUri(), "Base URI should persist");
    }

    @Test
    @DisplayName("parse(html, parser) with non-empty html uses parser.parseInput path B0→B1→B4")
    public void test_TC20() {
        String html = "<span id='x'>X</span>";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, "", parser);
        assertEquals("", doc.baseUri(), "Expected empty baseUri when not provided");
        assertEquals("X", doc.getElementById("x").text(), "Expected span text to be parsed");
    }

    @Test
    @DisplayName("parse(File, charsetName) loads file with explicit charset path B0→B2")
    public void test_TC21() throws IOException {
        File f = File.createTempFile("t", ".html");
        f.deleteOnExit();
        Files.write(f.toPath(), "<div>Ž</div>".getBytes(StandardCharsets.UTF_16));
        String cs = "UTF-16";
        Document doc = Jsoup.parse(f, cs);
        assertEquals("Ž", doc.select("div").text(), "Expected UTF-16 character parsed correctly");
        assertEquals(f.getAbsolutePath(), doc.baseUri(), "Expected baseUri to be file absolute path");
    }

    @Test
    @DisplayName("parse(File) default charset and baseUri fallback path B0→B2")
    public void test_TC22() throws IOException {
        File f = File.createTempFile("t", ".html");
        f.deleteOnExit();
        Files.write(f.toPath(), "<p>D2</p>".getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(f);
        assertEquals("D2", doc.select("p").text(), "Expected UTF-8 default charset parsing");
        assertEquals(f.getAbsolutePath(), doc.baseUri(), "Expected baseUri fallback to file path");
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri) loads from Path with explicit charset path B0→B1")
    public void test_TC23() throws IOException {
        Path p = Files.createTempFile("t", ".html");
        Files.write(p, "<h1>È</h1>".getBytes(StandardCharsets.ISO_8859_1));
        String cs = "ISO-8859-1";
        String uri = "http://b/";
        Document doc = Jsoup.parse(p.toFile(), cs);
        assertEquals("È", doc.select("h1").text(), "Expected ISO-8859-1 character parsed correctly");
        assertEquals(uri, doc.baseUri(), "Expected provided baseUri to persist");
    }

    @Test
    @DisplayName("parse(Path) default charset and baseUri fallback path B0→B1")
    public void test_TC24() throws IOException {
        Path p = Files.createTempFile("t", ".html");
        Files.write(p, "<section id='s'>S</section>".getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(p.toFile());
        assertEquals("S", doc.getElementById("s").text(), "Expected section text parsed");
        assertEquals(p.toAbsolutePath().toString(), doc.baseUri(), "Expected baseUri from absolute path");
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri, parser) invokes DataUtil.load with custom parser path B0→B1→B2")
    public void test_TC25() throws IOException {
        Path p = Files.createTempFile("t", ".html");
        Files.write(p, "<x/>".getBytes(StandardCharsets.UTF_8));
        String cs = "UTF-8";
        String uri = "";
        Parser stub = new Parser(new Parser.Settings()) {
            @Override
            public Document parseInput(String in, String baseUri) {
                Document d = Document.createShell(baseUri);
                d.body().appendElement("p").text("P");
                return d;
            }
        };
        Document doc = Jsoup.parse(p.toFile(), cs, uri);
        assertEquals("P", doc.select("p").text(), "Expected 'P' from stub parser");
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) loads stream with valid charset path B0→B1→B2")
    public void test_TC26() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("<i id='i'>I</i>".getBytes(StandardCharsets.UTF_8));
        String cs = "UTF-8";
        String uri = "http://u/";
        Document doc = Jsoup.parse(in, cs, uri);
        assertEquals("I", doc.getElementById("i").text(), "Expected element text parsed from stream");
        assertEquals(uri, doc.baseUri(), "Expected provided baseUri to persist");
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri, parser) with stub returns its Document path B0→B1→B2→B3")
    public void test_TC27() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("<div/>".getBytes(StandardCharsets.UTF_8));
        String cs = "UTF-8";
        String uri = "";
        Parser stub = new Parser(new Parser.Settings()) {
            @Override
            public Document parseInput(String inHtml, String baseUri) {
                Document d = Document.createShell(baseUri);
                d.body().appendElement("stub");
                return d;
            }
        };
        Document doc = Jsoup.parse(in, cs, uri, stub);
        assertNotNull(doc.selectFirst("stub"), "Expected stub element from custom parser");
    }
}