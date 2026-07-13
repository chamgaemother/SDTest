package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(Path path, String charsetName, String baseUri, Parser parser) uses xmlParser on a real file")
    public void test_TC19() throws IOException {
        // prepare a temp HTML file with UTF-8 content '<h1>Hi</h1>' to hit xmlParser path
        Path temp = Files.createTempFile("tc19", ".html");
        Files.write(temp, "<h1>Hi</h1>".getBytes("UTF-8"));
        String charset = "UTF-8";
        String baseUri = "http://example.com/";
        Parser parser = Parser.xmlParser();
        // WHEN: parsing the file using Path overload with parser
        Document doc = Jsoup.parse(temp, charset, baseUri, parser);
        // THEN: the <h1> element's text should be 'Hi'
        assertEquals("Hi", doc.select("h1").first().text());
    }

    @Test
    @DisplayName("parse(Path path) with valid gzipped file supports .gz suffix")
    public void test_TC20() throws IOException {
        // prepare a temp gzipped file containing '<p>GZ</p>' to trigger gzip-handling branch
        Path gz = Files.createTempFile("tc20", ".gz");
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(gz.toFile()))) {
            gos.write("<p>GZ</p>".getBytes("UTF-8"));
        }
        // WHEN: parsing the gzipped Path default charset
        Document doc = Jsoup.parse(gz);
        // THEN: the <p> element's text should be 'GZ'
        assertEquals("GZ", doc.select("p").first().text());
    }

    @Test
    @DisplayName("parse(InputStream in, null charsetName, String baseUri) auto-detects charset when charsetName is null")
    public void test_TC21() throws IOException {
        // InputStream over '<div>Auto</div>' in UTF-8, null charset triggers auto-detect path
        byte[] htmlBytes = "<div>Auto</div>".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(htmlBytes);
        String charset = null;
        String baseUri = "";
        // WHEN: parsing stream with null charset and empty baseUri
        Document doc = Jsoup.parse(in, charset, baseUri);
        // THEN: the <div> element's text should be 'Auto'
        assertEquals("Auto", doc.select("div").first().text());
    }

    @Test
    @DisplayName("parse(String html, Parser parser) with null parser throws IllegalArgumentException")
    public void test_TC22() {
        // Passing null parser should throw IllegalArgumentException per contract
        String html = "<p>X</p>";
        Parser parser = null;
        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, parser));
    }

    @Test
    @DisplayName("parse(InputStream in, String charsetName, String baseUri, Parser parser) with null parser throws IllegalArgumentException")
    public void test_TC23() throws IOException {
        // InputStream over '<span>Y</span>' and null parser triggers exception branch
        byte[] htmlBytes = "<span>Y</span>".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(htmlBytes);
        String charset = "UTF-8";
        String baseUri = "";
        Parser parser = null;
        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(in, charset, baseUri, parser));
    }
}