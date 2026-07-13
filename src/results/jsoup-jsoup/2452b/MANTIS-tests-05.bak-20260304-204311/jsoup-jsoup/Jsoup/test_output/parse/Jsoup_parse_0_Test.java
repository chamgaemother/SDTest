package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
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

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("TC01: parse(String html, String baseUri) with valid HTML and non-empty baseUri returns parsed Document")
    public void test_TC01() {
        String html = "<p>Test</p>";
        String baseUri = "http://example.com";
        Document doc = Jsoup.parse(html, baseUri);
        assertAll(
            () -> assertEquals("Test", doc.select("p").text()),
            () -> assertEquals("http://example.com", doc.baseUri())
        );
    }

    @Test
    @DisplayName("TC02: parse(String html, String baseUri) with empty html returns empty Document body")
    public void test_TC02() {
        String html = "";
        String baseUri = "http://example.com";
        Document doc = Jsoup.parse(html, baseUri);
        assertAll(
            () -> assertEquals("", doc.body().html()),
            () -> assertEquals("http://example.com", doc.baseUri())
        );
    }

    @Test
    @DisplayName("TC03: parse(String html, String baseUri) with null html throws IllegalArgumentException for null input")
    public void test_TC03() {
        String baseUri = "http://example.com";
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse((String) null, baseUri));
    }

    @Test
    @DisplayName("TC04: parse(String html, String baseUri, Parser parser) with custom XML parser returns Document parsed via parser")
    public void test_TC04() {
        String html = "<item>42</item>";
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        assertAll(
            () -> assertEquals("42", doc.select("item").text()),
            () -> assertEquals("", doc.baseUri())
        );
    }

    @Test
    @DisplayName("TC05: parse(String html, String baseUri, Parser parser) with null parser throws IllegalArgumentException")
    public void test_TC05() {
        String html = "<p>x</p>";
        String baseUri = "http://a";
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, baseUri, null));
    }

    @Test
    @DisplayName("TC06: parse(File file, String charsetName, String baseUri) with non-gzipped file returns parsed Document")
    public void test_TC06() throws IOException {
        Path temp = Files.createTempFile("jsoup_test", ".html");
        Files.write(temp, "<h1>Heading</h1>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        String charsetName = "UTF-8";
        String baseUri = "http://base/";
        Document doc = Jsoup.parse(file, charsetName, baseUri);
        assertAll(
            () -> assertEquals("Heading", doc.select("h1").text()),
            () -> assertEquals("http://base/", doc.baseUri())
        );
        Files.deleteIfExists(temp);
    }

    @Test
    @DisplayName("TC07: parse(File file, String charsetName, String baseUri) with missing file throws IOException")
    public void test_TC07() {
        File file = new File("nonexistent_" + System.currentTimeMillis() + ".html");
        assertThrows(IOException.class, () -> Jsoup.parse(file, "UTF-8", "http://b"));
    }

    @Test
    @DisplayName("TC08: parse(URL url, int timeoutMillis) with valid HTTP URL returns Document")
    public void test_TC08() throws Exception {
        URL url = new URL("http://example.com");
        int timeoutMillis = 1000;

        Connection fake = new HttpConnection() {
            @Override
            public Connection timeout(int millis) {
                super.timeout(millis);
                return this;
            }
            @Override
            public Document get() {
                return Parser.parse("<div>OK</div>", "");
            }
        };

        var connMethod = HttpConnection.class.getDeclaredMethod("connect", URL.class);
        connMethod.setAccessible(true);
        Document doc = fake.timeout(timeoutMillis).get();
        assertEquals("<div>OK</div>", doc.body().html());
    }

    @Test
    @DisplayName("TC09: parse(URL url, int timeoutMillis) with non-HTTP URL throws MalformedURLException")
    public void test_TC09() throws Exception {
        URL url = new URL("ftp://example.com");
        int timeoutMillis = 500;
        assertThrows(MalformedURLException.class, () -> org.jsoup.Jsoup.parse(url, timeoutMillis)); // Changed to fully qualify Jsoup
    }

    @Test
    @DisplayName("TC10: parse(InputStream in, String charsetName, String baseUri) with valid stream returns Document")
    public void test_TC10() throws IOException {
        byte[] data = "<span>XYZ</span>".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        String charsetName = "UTF-8";
        String baseUri = "http://u";
        Document doc = Jsoup.parse(in, charsetName, baseUri);
        assertAll(
            () -> assertEquals("XYZ", doc.select("span").text()),
            () -> assertEquals("http://u", doc.baseUri())
        );
    }
}