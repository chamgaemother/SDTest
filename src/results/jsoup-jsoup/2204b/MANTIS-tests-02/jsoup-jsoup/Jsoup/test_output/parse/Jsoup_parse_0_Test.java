package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("TC01_O1 parse(String html, String baseUri) with non-empty HTML and baseUri returns Document with correct baseUri")
    public void test_TC01_O1() {
        String html = "<p>Test</p>";
        String baseUri = "https://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("<p>Test</p>", doc.body().html(), "Body HTML should match input fragment");
        assertEquals(baseUri, doc.baseUri(), "Base URI should be preserved");
    }

    @Test
    @DisplayName("TC02_O1 parse(String html, String baseUri) with empty HTML returns empty body and retains baseUri")
    public void test_TC02_O1() {
        String html = "";
        String baseUri = "http://foo/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("", doc.body().html(), "Empty input yields empty body");
        assertEquals(baseUri, doc.baseUri(), "Base URI should be preserved even if html empty");
    }

    @Test
    @DisplayName("TC03_O2 parse(String html, Parser parser) uses empty baseUri when no baseUri provided")
    public void test_TC03_O2() {
        String html = "<div>X</div>";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals("<div>X</div>", doc.body().html(), "Body HTML should match input fragment");
        assertEquals("", doc.baseUri(), "Base URI should default to empty string");
    }

    @Test
    @DisplayName("TC04_O3 parse(String html) uses empty baseUri")
    public void test_TC04_O3() {
        String html = "<span>Y</span>";
        Document doc = Jsoup.parse(html);
        assertEquals("<span>Y</span>", doc.body().html(), "Body HTML should match input fragment");
        assertEquals("", doc.baseUri(), "Base URI should default to empty string");
    }

    @Test
    @DisplayName("TC05_O4 parse(File file, String charsetName, String baseUri) with existing file returns parsed Document")
    public void test_TC05_O4() throws IOException {
        File temp = File.createTempFile("jsoup-test", ".html");
        temp.deleteOnExit();
        Files.write(temp.toPath(), "<h1>H</h1>".getBytes("UTF-8"));
        String charset = "UTF-8";
        String base = "http://base/";
        Document doc = Jsoup.parse(temp, charset, base);
        assertEquals("<h1>H</h1>", doc.body().html(), "Body HTML should reflect file contents");
        assertEquals(base, doc.baseUri(), "Base URI should match provided");
    }

    @Test
    @DisplayName("TC06_O4 parse(File file) with missing file throws IOException")
    public void test_TC06_O4() {
        File non = new File("nonexistent.html");
        assertThrows(IOException.class, () -> Jsoup.parse(non), "Nonexistent file should throw IOException");
    }

    @Test
    @DisplayName("TC07_O5 parse(Path path, String charsetName, String baseUri) with gzipped .gz file returns Document")
    public void test_TC07_O5() throws IOException {
        Path gz = Files.createTempFile("jsoup-test", ".html.gz");
        gz.toFile().deleteOnExit();
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(gz.toFile()))) {
            gos.write("<p>Z</p>".getBytes("UTF-8"));
        }
        String base = "https://u/";
        Document doc = Jsoup.parse(gz, null, base);
        assertEquals("<p>Z</p>", doc.body().html(), "Body HTML should reflect gzipped contents");
        assertEquals(base, doc.baseUri(), "Base URI should match provided");
    }

    @Test
    @DisplayName("TC08_O6 parse(InputStream in, String charsetName, String baseUri) with valid stream returns Document")
    public void test_TC08_O6() throws IOException {
        byte[] bytes = "<b>B</b>".getBytes("UTF-8");
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            Document doc = Jsoup.parse(in, "UTF-8", "base");
            assertEquals("<b>B</b>", doc.body().html(), "Body HTML should match stream contents");
            assertEquals("base", doc.baseUri(), "Base URI should match provided");
        }
    }

    @Test
    @DisplayName("TC09_O7 parse(URL url, int timeoutMillis) with unsupported protocol throws MalformedURLException")
    public void test_TC09_O7() throws Exception {
        URL url = new URL("ftp://example.com");
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, 1000),
            "Unsupported protocol should throw MalformedURLException");
    }

    @Test
    @DisplayName("TC10_O7 parse(URL url, int timeoutMillis) with valid HTTP URL returns Document")
    public void test_TC10_O7() throws Exception {
        URL url = new URL("http://example.com");
        Document stubDoc = new Document("http://stub/");
        HttpConnection.Response dummyResp = Mockito.mock(HttpConnection.Response.class);
        org.jsoup.Connection dummyCon = Mockito.mock(org.jsoup.Connection.class);
        Mockito.when(dummyCon.timeout(Mockito.anyInt())).thenReturn(dummyCon);
        Mockito.when(dummyCon.get()).thenReturn(stubDoc);
        try (MockedStatic<HttpConnection> mockStatic = Mockito.mockStatic(HttpConnection.class)) {
            // Fixed return type for mockStatic
            mockStatic.when(() -> HttpConnection.connect(url)).thenReturn(dummyCon);
            Document result = Jsoup.parse(url, 500);
            assertSame(stubDoc, result, "Should return the stubbed Document for HTTP URL");
        }
    }
}