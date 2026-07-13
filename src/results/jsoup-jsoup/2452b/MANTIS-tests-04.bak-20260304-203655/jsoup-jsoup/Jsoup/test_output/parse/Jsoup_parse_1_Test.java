package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("TC11: parse(File) no-arg overload reads UTF-8 file and sets baseUri to file path")
    public void test_TC11() throws IOException {
        Path temp = Files.createTempFile("x", ".html");
        Files.write(temp, "<h2>Hello</h2>".getBytes("UTF-8"));
        File f = temp.toFile();
        Document doc = Jsoup.parse(f);
        assertTrue(doc.html().contains("<h2>Hello</h2>"),
            "Document HTML should contain the file content");
        assertEquals(f.getAbsolutePath(), doc.baseUri(),
            "Base URI should be the file's absolute path");
    }

    @Test
    @DisplayName("TC12: parse(File,charset,baseUri,Parser) uses custom parser and charset")
    public void test_TC12() throws IOException {
        Path temp = Files.createTempFile("y", ".html");
        Files.write(temp, "<tag>XML</tag>".getBytes("UTF-8"));
        File f = temp.toFile();
        String cs = "UTF-8";
        String base = "http://xml/";
        Parser p = Parser.xmlParser();
        Document doc = Jsoup.parse(f, cs, base, p);
        assertTrue(doc.html().contains("<tag>XML</tag>"),
            "Document HTML should reflect XML parser content");
        assertEquals(base, doc.baseUri(),
            "Base URI should be the provided base URI");
    }

    @Test
    @DisplayName("TC13: parse(Path, charsetName, baseUri) reads file via Path overload")
    public void test_TC13() throws IOException {
        Path pth = Files.createTempFile("p", ".html");
        Files.write(pth, "<p>Path</p>".getBytes("UTF-8"));
        String cs = null; // null charset should be accepted
        String base = "http://path/";
        Document doc = Jsoup.parse(pth, cs, base);
        assertTrue(doc.html().contains("<p>Path</p>"),
            "Document HTML should contain the path file content");
        assertEquals(base, doc.baseUri(),
            "Base URI should be the provided base URI");
    }

    @Test
    @DisplayName("TC14: parse(Path, charsetName) no-baseUri uses Path.toAbsolutePath() as baseUri")
    public void test_TC14() throws IOException {
        Path pth = Files.createTempFile("d", ".html");
        Files.write(pth, "<div>P</div>".getBytes("UTF-8"));
        String cs = "UTF-8";
        Document doc = Jsoup.parse(pth, cs);
        assertTrue(doc.html().contains("<div>P</div>"),
            "Document HTML should contain the div content");
        assertEquals(pth.toAbsolutePath().toString(), doc.baseUri(),
            "Base URI should be the absolute path of the file");
    }

    @Test
    @DisplayName("TC15: parse(Path) BOM/meta detection equivalent to null-charset overload")
    public void test_TC15() throws IOException {
        Path pth = Files.createTempFile("s", ".html");
        Files.write(pth, "<span>Z</span>".getBytes("UTF-8"));
        Document doc = Jsoup.parse(pth);
        assertTrue(doc.html().contains("<span>Z</span>"),
            "Document HTML should contain the span content");
        assertEquals(pth.toAbsolutePath().toString(), doc.baseUri(),
            "Base URI should be the absolute path of the file");
    }

    @Test
    @DisplayName("TC16: parse(InputStream,in,null charset) falls back to UTF-8 and parses body")
    public void test_TC16() throws IOException {
        byte[] bytes = "<i>IS</i>".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        String cs = null;
        String base = "http://ins/";
        Document doc = Jsoup.parse(in, cs, base);
        assertEquals("<i>IS</i>", doc.body().html(),
            "Body HTML should exactly match the input fragment");
        assertEquals(base, doc.baseUri(),
            "Base URI should be the provided base URI");
    }

    @Test
    @DisplayName("TC17: parse(InputStream,charset,baseUri,Parser) uses custom parser on stream")
    public void test_TC17() throws IOException {
        byte[] bytes = "<x>Y</x>".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        String cs = "UTF-8";
        String base = "http://str/";
        Parser p = Parser.xmlParser();
        Document doc = Jsoup.parse(in, cs, base, p);
        assertTrue(doc.html().contains("<x>Y</x>"),
            "Document HTML should reflect xmlParser behavior");
        assertEquals(base, doc.baseUri(),
            "Base URI should be the provided base URI");
    }

    @Test
    @DisplayName("TC18: parse(URL,timeoutMillis) with non-HTTP URL throws MalformedURLException")
    public void test_TC18() {
        assertThrows(MalformedURLException.class, () -> {
            URL url = new URL("ftp://example.com/");
            Jsoup.parse(url, 1000);
        }, "Non-HTTP URL should throw MalformedURLException");
    }
}