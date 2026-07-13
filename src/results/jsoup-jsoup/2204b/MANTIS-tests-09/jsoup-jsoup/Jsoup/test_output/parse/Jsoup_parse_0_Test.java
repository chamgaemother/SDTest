package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("TC01: parse(null, anyBaseUri) throws NullPointerException when html is null")
    public void test_TC01() {
        // Branch B1: html == null should trigger NPE
        String html = null;
        String baseUri = "http://example.com";
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, baseUri));
    }

    @Test
    @DisplayName("TC02: parse(\"\",\"\") returns empty Document when html is empty")
    public void test_TC02() {
        // Branch B2: html is empty, should produce a Document with empty body
        String html = "";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.body().html(), "Empty HTML should yield empty body");
    }

    @Test
    @DisplayName("TC03: parse(\"<p>Hi</p>\",\"/base\") returns Document resolving relative URLs against baseUri")
    public void test_TC03() {
        // Branch B2: non-null, non-empty html with explicit baseUri
        String html = "<p>Hi</p>";
        String baseUri = "/base";
        Document doc = Jsoup.parse(html, baseUri);
        assertAll("Check element text and baseUri",
            () -> assertEquals("Hi", doc.select("p").first().text(), "Expected paragraph text"),
            () -> assertEquals("/base", doc.baseUri(), "Expected baseUri set on Document")
        );
    }

    @Test
    @DisplayName("TC04: parse(\"<tag/>\",Parser.xmlParser()) returns XML parsed Document when using XML parser")
    public void test_TC04() {
        // Overload parse(html, Parser): uses xmlParser path (B3)
        String html = "<tag/>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals(1, doc.select("tag").size(), "XML parser should create self-closed tag");
    }

    @Test
    @DisplayName("TC05: parse(\"<tag>1</tag>\",\"/u\",Parser.htmlParser()) returns HTML parsed Document when using HTML parser overload")
    public void test_TC05() {
        // Overload parse(html, baseUri, parser): HTML parser path (B4)
        String html = "<tag>1</tag>";
        String baseUri = "/u";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        assertEquals("1", doc.select("tag").first().text(), "HTML parser should parse content text");
    }

    @Test
    @DisplayName("TC06: parse(tempFile, null, \"baseUri\") loads file content when charsetName null and file exists")
    public void test_TC06() throws IOException {
        // Overload parse(file, charsetName, baseUri): valid file, charsetName=null triggers default BOM/meta detection (B5)
        File temp = File.createTempFile("jsoupTest", ".html");
        temp.deleteOnExit();
        try (FileWriter w = new FileWriter(temp)) {
            w.write("<div>f</div>");
        }
        Document doc = Jsoup.parse(temp, null, "baseUri");
        assertEquals("f", doc.select("div").first().text(), "Should load and parse div text");
    }

    @Test
    @DisplayName("TC07: parse(nonExistentFile, \"UTF-8\", \"u\") throws IOException when file not found")
    public void test_TC07() {
        // Overload parse(file, charsetName, baseUri): missing file should throw IOException (B6)
        File file = new File("no-such.html");
        assertThrows(IOException.class, () -> Jsoup.parse(file, "UTF-8", "u"));
    }

    @Test
    @DisplayName("TC08: parse(inputStream, \"UTF-8\", \"b\") returns Document when input stream provided")
    public void test_TC08() throws IOException {
        // Overload parse(InputStream, charsetName, baseUri): valid stream path (B7)
        String fragment = "<span>x</span>";
        InputStream in = new ByteArrayInputStream(fragment.getBytes("UTF-8"));
        Document doc = Jsoup.parse(in, "UTF-8", "b");
        assertEquals("x", doc.select("span").text(), "Should parse span text from InputStream");
    }

    @Test
    @DisplayName("TC09: parse(new URL(\"ftp://x\"),1000) throws MalformedURLException for unsupported protocol")
    public void test_TC09() throws IOException {
        // Overload parse(URL, timeout): ftp protocol is unsupported, expecting MalformedURLException (B8)
        URL url = new URL("ftp://x");
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, 1000));
    }

    @Test
    @DisplayName("TC10: parseBodyFragment(\"<p>a</p>\",\"bu\") returns Document body fragment with p element")
    public void test_TC10() {
        // parseBodyFragment(html, baseUri): should set body to fragment (B9)
        Document doc = Jsoup.parseBodyFragment("<p>a</p>", "bu");
        assertEquals("<p>a</p>", doc.body().html(), "Body HTML must equal input fragment");
    }

    @Test
    @DisplayName("TC11: parseBodyFragment(\"<p>b</p>\") returns Document body fragment with empty baseUri")
    public void test_TC11() {
        // parseBodyFragment(html): default baseUri = "" path (B10)
        Document doc = Jsoup.parseBodyFragment("<p>b</p>");
        assertAll("Check body HTML and default baseUri",
            () -> assertEquals("<p>b</p>", doc.body().html(), "Body HTML must equal input fragment"),
            () -> assertEquals("", doc.baseUri(), "Default baseUri should be empty string")
        );
    }
}