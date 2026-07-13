package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("TC01: parse(String, String) with non-empty HTML and non-empty baseUri returns a Document with expected body content")
    public void test_TC01() {
        String html = "<p>Text</p>";
        String baseUri = "http://example.com";
        Document doc = Jsoup.parse(html, baseUri);

        assertAll(
            () -> assertEquals("<p>Text</p>", doc.body().html(), "body HTML should match input fragment"),
            () -> assertEquals("http://example.com", doc.baseUri(), "baseUri should be set to provided value")
        );
    }

    @Test
    @DisplayName("TC02: parse(String) with empty HTML returns empty Document body and empty baseUri")
    public void test_TC02() {
        String html = "";
        Document doc = Jsoup.parse(html);

        assertAll(
            () -> assertEquals("", doc.body().html(), "empty input yields empty body HTML"),
            () -> assertEquals("", doc.baseUri(), "no baseUri provided should default to empty")
        );
    }

    @Test
    @DisplayName("TC03: parse(String, String, Parser) with custom Parser parses XML correctly")
    public void test_TC03() {
        String xml = "<root><title>XMLTitle</title></root>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(xml, "", parser);

        assertEquals("XMLTitle",
            doc.selectFirst("title").text(),
            "xmlParser should produce a Document where title element text is parsed exactly");
    }

    @Test
    @DisplayName("TC04: parse(File, null, baseUri) loads from gzip file and returns Document with element")
    public void test_TC04() throws IOException {
        Path temp = Files.createTempFile("jsoup-test", ".html.gz");
        temp.toFile().deleteOnExit();
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(temp.toFile()))) {
            gos.write("<div>GZ</div>".getBytes(StandardCharsets.UTF_8));
        }
        File gzFile = temp.toFile();
        String baseUri = "http://base/";
        Document doc = Jsoup.parse(gzFile, null, baseUri);

        assertAll(
            () -> assertEquals("GZ", doc.selectFirst("div").text(), "should read and parse gzipped HTML"),
            () -> assertEquals("http://base/", doc.baseUri(), "baseUri passed to overload should be preserved")
        );
    }

    @Test
    @DisplayName("TC05: parse(File) with missing file throws IOException")
    public void test_TC05() {
        File missing = new File("nonexistent_" + System.nanoTime() + ".html");
        assertThrows(IOException.class, () -> Jsoup.parse(missing));
    }

    @Test
    @DisplayName("TC06: parse(Path, charsetName, baseUri, Parser) with real Path and custom Parser returns parsed output")
    public void test_TC06() throws IOException {
        Path tmp = Files.createTempFile("jsoup-p6", ".html");
        tmp.toFile().deleteOnExit();
        Files.write(tmp, "<span>X</span>".getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(tmp, "UTF-8", "", Parser.htmlParser());

        assertEquals("X",
            doc.selectFirst("span").text(),
            "htmlParser should parse span element text correctly");
    }

    @Test
    @DisplayName("TC07: parse(InputStream, charsetName, baseUri) with InputStream and invalid charset throws IOException")
    public void test_TC07() {
        byte[] data = "<p>Error</p>".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        assertThrows(IOException.class, () -> Jsoup.parse(in, "INVALID-CHARSET", ""));
    }

    @Test
    @DisplayName("TC08: parse(URL, timeoutMillis) with HTTP URL returns parsed Document")
    public void test_TC08() throws Exception {
        URL url = new URL("http://example.com");
        java.lang.reflect.Method connectMethod = HttpConnection.class.getMethod("connect", URL.class);
        connectMethod.setAccessible(true);
        HttpConnection fake = new HttpConnection() {
            @Override public HttpConnection.Response execute() { return new HttpConnection.Response(); }
            @Override public HttpConnection.Response get() { return new HttpConnection.Response(); }
            @Override public HttpConnection.Response post() { return new HttpConnection.Response(); }
            @Override public HttpConnection.Connection timeout(int millis) { return this; }
            @Override public HttpConnection.Connection url(URL url) { return this; }
            @Override public HttpConnection.Connection request(org.jsoup.Connection.Request request) { return this; }
            @Override public org.jsoup.Connection.Request request() { return null; }
            @Override public HttpConnection.Connection method(org.jsoup.Connection.Method method) { return this; }
            @Override public org.jsoup.Connection.Method method() { return null; }
            @Override public HttpConnection.Connection header(String name, String value) { return this; }
            @Override public HttpConnection.Connection headers(java.util.Map<String, String> headers) { return this; }
            @Override public java.util.Map<String, String> headers() { return null; }
            @Override public HttpConnection.Connection cookie(String name, String value) { return this; }
            @Override public java.util.Map<String, String> cookies() { return null; }
            @Override public HttpConnection.Connection cookies(java.util.Map<String, String> cookies) { return this; }
            @Override public HttpConnection.Connection data(String key, String value) { return this; }
            @Override public HttpConnection.Connection data(java.util.Collection<org.jsoup.Connection.KeyVal> data) { return this; }
            @Override public java.util.Map<String, java.util.List<org.jsoup.Connection.KeyVal>> multiData() { return null; }
            @Override public HttpConnection.Connection followRedirects(boolean followRedirects) { return this; }
            @Override public boolean followRedirects() { return false; }
        };
        java.lang.reflect.Field implField = HttpConnection.class.getDeclaredField("factory");
        implField.setAccessible(true);
        implField.set(null, (HttpConnection.Factory) urlArg -> fake);

        Document doc = Jsoup.parse(url, 5000);
        assertEquals("OK", doc.selectFirst("h1").text(), "fake connection should return <h1>OK</h1>");
    }

    @Test
    @DisplayName("TC09: parse(URL, timeoutMillis) with non-HTTP URL throws MalformedURLException")
    public void test_TC09() throws Exception {
        URL ftpUrl = new URL("ftp://example.com");
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(ftpUrl, 1000));
    }

    @Test
    @DisplayName("TC10: parse(String, Parser) with HTML and xmlParser defaults baseUri to empty string")
    public void test_TC10() {
        String html = "<i>Italic</i>";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, parser);

        assertAll(
            () -> assertEquals("<i>Italic</i>", doc.body().html(), "body HTML must match provided fragment"),
            () -> assertEquals("", doc.baseUri(), "baseUri should default to empty string")
        );
    }
}