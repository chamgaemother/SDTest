package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(File, charsetName, baseUri, Parser) uses the provided parser and loads file successfully")
    public void test_TC11() throws IOException {
        File tmp = File.createTempFile("tc11", ".html");
        tmp.deleteOnExit();
        String html = "<div>X</div>";
        Files.write(tmp.toPath(), html.getBytes("UTF-8"));
        String charset = "UTF-8";
        String baseUri = "http://example.org/";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(tmp, charset, baseUri, parser);
        assertEquals("<div>X</div>", doc.body().html());
    }

    @Test
    @DisplayName("parse(File, charsetName) loads file and infers baseUri from file path")
    public void test_TC12() throws IOException {
        File tmp = File.createTempFile("tc12", ".html");
        tmp.deleteOnExit();
        String html = "<span>Y</span>";
        Files.write(tmp.toPath(), html.getBytes("ISO-8859-1"));
        String charset = "ISO-8859-1";
        Document doc = Jsoup.parse(tmp, charset);
        assertEquals("Y", doc.body().text());
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri) reads path and applies baseUri correctly")
    public void test_TC13() throws IOException {
        Path p = Files.createTempFile("tc13", ".html");
        p.toFile().deleteOnExit();
        String html = "<p>Z</p>";
        Files.write(p, html.getBytes());
        String charset = null;
        String baseUri = "http://host/";
        Document doc = Jsoup.parse(p, charset, baseUri);
        assertEquals("Z", doc.body().text());
    }

    @Test
    @DisplayName("parse(Path) reads path, infers UTF-8 and baseUri from path")
    public void test_TC14() throws IOException {
        Path p = Files.createTempFile("tc14", ".html");
        p.toFile().deleteOnExit();
        String html = "<h1>H</h1>";
        Files.write(p, html.getBytes());
        Document doc = Jsoup.parse(p);
        assertEquals("H", doc.body().text());
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri, Parser) uses parser on path-based file")
    public void test_TC15() throws IOException {
        Path p = Files.createTempFile("tc15", ".html");
        p.toFile().deleteOnExit();
        String html = "<q>Q</q>";
        Files.write(p, html.getBytes("UTF-8"));
        String charset = "UTF-8";
        String baseUri = "";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(p, charset, baseUri, parser);
        assertEquals("<q>Q</q>", doc.body().html());
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) reads stream successfully")
    public void test_TC16() throws IOException {
        String fragment = "<li>L</li>";
        InputStream in = new ByteArrayInputStream(fragment.getBytes("UTF-8"));
        String charset = "UTF-8";
        String baseUri = "http://u/";
        Document doc = Jsoup.parse(in, charset, baseUri);
        assertEquals("L", doc.body().text());
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri, Parser) uses parser on stream")
    public void test_TC17() throws IOException {
        String fragment = "<em>E</em>";
        InputStream in = new ByteArrayInputStream(fragment.getBytes());
        String charset = null;
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(in, charset, baseUri, parser);
        assertEquals("<em>E</em>", doc.body().html());
    }

    @Test
    @DisplayName("parseBodyFragment(String, String) with non-empty baseUri resolves relative links")
    public void test_TC18() {
        String frag = "<img src='img.png'>";
        String baseUri = "http://cdn/";
        Document doc = Jsoup.parseBodyFragment(frag, baseUri);
        String html = doc.body().html();
        assertTrue(html.contains("http://cdn/img.png"));
    }

    @Test
    @DisplayName("parseBodyFragment(String) without baseUri leaves src attribute unchanged")
    public void test_TC19() {
        String frag = "<img src='img.png'>";
        Document doc = Jsoup.parseBodyFragment(frag);
        String html = doc.body().html();
        assertTrue(html.contains("src=\"img.png\""));
    }
}