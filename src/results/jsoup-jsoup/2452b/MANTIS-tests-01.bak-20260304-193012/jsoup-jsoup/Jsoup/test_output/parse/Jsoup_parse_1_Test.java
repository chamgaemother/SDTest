package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
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
    @DisplayName("parse(File,charsetName,baseUri) throws IOException for missing file")
    void test_TC11() {
        // precondition: nonexistent file => IOException path B2(exception)
        File file = new File("no-such-file.html");
        assertThrows(IOException.class, () -> Jsoup.parse(file, "UTF-8", "http://example.com"));
    }

    @Test
    @DisplayName("parse(File,charsetName,baseUri) returns Document when file exists and charset provided")
    void test_TC12() throws IOException {
        // create temp file with <p>ok</p>, cover normal branch B4
        Path temp = Files.createTempFile("JsoupTest12", ".html");
        Files.write(temp, "<p>ok</p>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        Document doc = Jsoup.parse(file, "UTF-8", "http://site/");
        assertEquals("ok", doc.select("p").text());
    }

    @Test
    @DisplayName("parse(File,charsetName) uses file absolute path as baseUri")
    void test_TC13() throws IOException {
        // link href '/x' should resolve against file absolute path
        Path temp = Files.createTempFile("JsoupTest13", ".html");
        Files.write(temp, "<a href='/x'>x</a>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        Document doc = Jsoup.parse(file, "UTF-8");
        String abs = doc.select("a").first().absUrl("href");
        assertTrue(abs.startsWith(file.getAbsolutePath()), "Expected abs href start with file path");
    }

    @Test
    @DisplayName("parse(File) determines charset from BOM/meta and uses file absolute path")
    void test_TC14() throws IOException {
        // default overload uses null charset and file path
        Path temp = Files.createTempFile("JsoupTest14", ".html");
        Files.write(temp, "<div id='d'>hi</div>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        Document doc = Jsoup.parse(file);
        assertEquals("hi", doc.getElementById("d").text());
    }

    @Test
    @DisplayName("parse(File,charsetName,baseUri,parser) uses provided Parser for files")
    void test_TC15() throws IOException {
        // custom xml parser should be used
        Path temp = Files.createTempFile("JsoupTest15", ".xml");
        String content = "<xml/>";
        Files.write(temp, content.getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(file, "UTF-8", "", parser);
        // read all file as string for parser expectation
        String fileData = new String(Files.readAllBytes(temp), StandardCharsets.UTF_8);
        Document expected = parser.parseInput(fileData, "");
        assertEquals(expected.outerHtml(), doc.outerHtml());
    }

    @Test
    @DisplayName("parse(Path,charsetName,baseUri) returns Document for Path input")
    void test_TC16() throws IOException {
        // parse via Path overload B4
        Path path = Files.createTempFile("JsoupTest16", ".html");
        Files.write(path, "<span>42</span>".getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(path, "UTF-8", "http://u/");
        assertEquals("42", doc.select("span").text());
    }

    @Test
    @DisplayName("parse(Path,charsetName) uses Path absolute path as baseUri")
    void test_TC17() throws IOException {
        // boundary resolution via Path absolute path
        Path path = Files.createTempFile("JsoupTest17", ".html");
        Files.write(path, "<link href='/r'/>".getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(path, "UTF-8");
        String abs = doc.select("link").first().absUrl("href");
        assertTrue(abs.startsWith(path.toAbsolutePath().toString()));
    }

    @Test
    @DisplayName("parse(Path) default overload delegates charset null and uses absolute path")
    void test_TC18() throws IOException {
        // default Path overload
        Path path = Files.createTempFile("JsoupTest18", ".html");
        Files.write(path, "<h1>head</h1>".getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(path);
        assertEquals("head", doc.select("h1").text());
    }

    @Test
    @DisplayName("parse(Path,charsetName,baseUri,parser) uses provided Parser for Path input")
    void test_TC19() throws IOException {
        // custom parser on Path
        Path path = Files.createTempFile("JsoupTest19", ".xml");
        String content = "<xml/>";
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(path, "UTF-8", "", parser);
        String data = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        Document expected = parser.parseInput(data, "");
        assertEquals(expected.outerHtml(), doc.outerHtml());
    }

    @Test
    @DisplayName("parse(InputStream,charsetName,baseUri) returns Document and closes stream")
    void test_TC20() throws IOException {
        // InputStream content, charset, baseUri, and closure
        class TrackStream extends ByteArrayInputStream {
            boolean closed = false;
            TrackStream(byte[] buf) { super(buf); }
            @Override public void close() throws IOException { closed = true; super.close(); }
        }
        TrackStream in = new TrackStream("<p>in</p>".getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(in, "UTF-8", "http://b/");
        assertEquals("in", doc.select("p").text());
        assertTrue(in.closed, "Stream should be closed after parse");
    }

    @Test
    @DisplayName("parse(InputStream,charsetName,baseUri,parser) uses provided Parser and closes stream")
    void test_TC21() throws IOException {
        // InputStream with parser and ensure closure
        class TrackStream extends ByteArrayInputStream {
            boolean closed = false;
            TrackStream(byte[] buf) { super(buf); }
            @Override public void close() throws IOException { closed = true; super.close(); }
        }
        String content = "<xml/>";
        TrackStream in = new TrackStream(content.getBytes(StandardCharsets.UTF_8));
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(in, "UTF-8", "", parser);
        // Expected via parser
        Document expected = parser.parseInput(content, "");
        assertEquals(expected.outerHtml(), doc.outerHtml());
        assertTrue(in.closed, "Stream should be closed after parse with parser");
    }

    @Test
    @DisplayName("parse(URL,timeout) throws MalformedURLException for non-http(s) URL")
    void test_TC22() throws Exception {
        // ftp scheme invalid => MalformedURLException in connect
        URL url = new URL("ftp://example.com");
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, 1000));
    }
}