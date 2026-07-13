package org.jsoup;

import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(html, baseUri) with non-empty baseUri invokes Parser.parse and returns a Document")
    public void test_TC01_O1() {
        // non-empty baseUri triggers Parser.parse(String, String) branch
        String html = "<p>hello</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals(baseUri, doc.baseUri(), "baseUri should be preserved");
        assertEquals("<p>hello</p>", doc.body().html(), "body html should reflect parsed tree");
    }

    @Test
    @DisplayName("parse(html) with empty baseUri invokes Parser.parse and returns a Document with empty baseUri")
    public void test_TC02_O2() {
        // empty baseUri path
        String html = "<div>test</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.baseUri(), "baseUri should be empty string");
        assertEquals("<div>test</div>", doc.body().html(), "body html should match input");
    }

    @Test
    @DisplayName("parse(html, baseUri, parser) uses provided Parser to parse and returns Document")
    public void test_TC03_O3() {
        // custom parser.parseInput branch
        String xml = "<root><child/></root>";
        String baseUri = "/base";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(xml, baseUri, parser);
        assertEquals(baseUri, doc.baseUri(), "baseUri should match provided");
        assertEquals(1, doc.select("child").size(), "XML parser should find one <child> element");
    }

    @Test
    @DisplayName("parse(html, parser) with empty baseUri and parser.parseInput called")
    public void test_TC04_O4() {
        // overload(String, Parser) with empty baseUri
        String html = "<tag/>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals("", doc.baseUri(), "baseUri should default to empty string");
        assertEquals(1, doc.select("tag").size(), "XML parser should parse <tag>");
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri) loads gzipped .gz file successfully")
    public void test_TC05_O5() throws IOException {
        // .gz file path branch
        String content = "<p>gz</p>";
        File temp = File.createTempFile("jsoupTest", ".html.gz");
        temp.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(temp);
             GZIPOutputStream gos = new GZIPOutputStream(fos)) {
            gos.write(content.getBytes());
        }
        String charset = "UTF-8";
        String baseUri = "http://f.com/";
        Document doc = Jsoup.parse(temp, charset, baseUri);
        assertEquals(baseUri, doc.baseUri(), "baseUri should match provided");
        assertEquals(content, doc.body().html(), "body html should equal uncompressed file content");
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri) throws IOException when file not found")
    public void test_TC06_O5() {
        // missing file triggers IOException
        File file = new File("/non/existent.html");
        assertThrows(IOException.class, () -> Jsoup.parse(file, null, ""));
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) with valid HTTP URL returns Document from GET")
    public void test_TC07_O6() throws IOException {
        // stub HttpConnection.connect to return stubbed Connection
        URL url = new URL("http://example.com/");
        int timeout = 5000;
        Document expected = new Document("http://example.com/");
        org.jsoup.Connection connStub = Mockito.mock(org.jsoup.Connection.class); // Fixed import for Connection
        Mockito.when(connStub.timeout(timeout)).thenReturn(connStub);
        Mockito.when(connStub.get()).thenReturn(expected);
        try (MockedStatic<HttpConnection> ms = Mockito.mockStatic(HttpConnection.class)) {
            ms.when(() -> HttpConnection.connect(url)).thenReturn(connStub);
            Document doc = Jsoup.parse(url, timeout);
            assertNotNull(doc, "Document should not be null");
            assertEquals(expected.baseUri(), doc.baseUri(), "baseUri should come from stub Document");
        }
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) throws MalformedURLException when URL protocol is ftp")
    public void test_TC08_O6() throws Exception {
        // unsupported protocol triggers MalformedURLException
        URL url = new URL("ftp://example.com/file");
        assertThrows(java.net.MalformedURLException.class, () -> Jsoup.parse(url, 1000));
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) throws SocketTimeoutException when connect timeout exceeded")
    public void test_TC09_O6() throws Exception {
        // real connect to non-routable IP triggers SocketTimeoutException
        URL url = new URL("http://10.255.255.1/");
        assertThrows(java.net.SocketTimeoutException.class, () -> Jsoup.parse(url, 1));
    }
}