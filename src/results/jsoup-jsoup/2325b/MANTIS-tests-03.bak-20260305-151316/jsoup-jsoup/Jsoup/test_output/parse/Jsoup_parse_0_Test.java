package org.jsoup;

import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for Jsoup.parse method overloads based on provided scenarios.
 */
public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("TC01_O1: parse(String html, String baseUri) with non-empty html and non-empty baseUri returns Document with correct body and baseUri")
    public void test_TC01_O1() {
        String html = "<p>Test</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("<p>Test</p>", doc.body().html());
    }

    @Test
    @DisplayName("TC02_O1: parse(String html, String baseUri) with empty html returns empty body fragment Document")
    public void test_TC02_O1() {
        String html = "";
        String baseUri = "http://foo";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("http://foo", doc.baseUri());
        assertEquals("", doc.body().html());
    }

    @Test
    @DisplayName("TC03_O2: parse(String html, String baseUri, Parser parser) with xmlParser parses xml style input")
    public void test_TC03_O2() {
        String xml = "<root><child/></root>";
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(xml, baseUri, parser);
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    @Test
    @DisplayName("TC04_O3: parse(String html, Parser parser) with empty baseUri and xmlParser sets baseUri to empty")
    public void test_TC04_O3() {
        String html = "<a href=\"b.html\">x</a>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals("", doc.baseUri());
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    @Test
    @DisplayName("TC05_O4: parse(String html) uses default parser and empty baseUri")
    public void test_TC05_O4() {
        String html = "<p>Foo</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.baseUri());
        assertEquals("<p>Foo</p>", doc.body().html());
    }

    @Test
    @DisplayName("TC06_O5: parse(File file, String charsetName, String baseUri) with valid UTF-8 file returns parsed Document")
    public void test_TC06_O5() throws IOException {
        File tmp = File.createTempFile("jsoupTest", ".html");
        tmp.deleteOnExit();
        try (FileWriter fw = new FileWriter(tmp); OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
            writer.write("<div>OK</div>");
        }
        String charset = "UTF-8";
        String baseUri = "http://u";
        Document doc = Jsoup.parse(tmp, charset, baseUri);
        assertEquals(baseUri, doc.baseUri());
        assertEquals("<div>OK</div>", doc.body().html());
    }

    @Test
    @DisplayName("TC07_O7: parse(File file) uses null charsetName and file absolutePath as baseUri")
    public void test_TC07_O7() throws IOException {
        File tmp = File.createTempFile("jsoupTest", ".html");
        tmp.deleteOnExit();
        try (FileWriter fw = new FileWriter(tmp); OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
            writer.write("<span>X</span>");
        }
        Document doc = Jsoup.parse(tmp);
        assertEquals(tmp.getAbsolutePath(), doc.baseUri());
        assertEquals("<span>X</span>", doc.body().html());
    }

    @Test
    @DisplayName("TC08_O11: parse(InputStream in, String charsetName, String baseUri) with multi-byte and fallback charset")
    public void test_TC08_O11() throws IOException {
        String text = "Ä";
        byte[] utf16 = text.getBytes(StandardCharsets.UTF_16);
        InputStream in = new ByteArrayInputStream(utf16);
        String charset = "UTF-16";
        String baseUri = "base";
        Document doc = Jsoup.parse(in, charset, baseUri);
        assertEquals(baseUri, doc.baseUri());
        assertEquals("Ä", doc.body().html());
    }

    @Test
    @DisplayName("TC09_O14: parse(URL url, int timeoutMillis) with valid HTTP URL returns fetched Document")
    public void test_TC09_O14() throws IOException {
        URL url = new URL("http://test");
        int timeout = 1000;
        Document stubDoc = Document.createShell("");
        stubDoc.body().appendElement("p").text("X");
        Connection conStub = mock(Connection.class);
        when(conStub.timeout(timeout)).thenReturn(conStub);
        when(conStub.get()).thenReturn(stubDoc);
        try (MockedStatic<HttpConnection> httpConnMock = Mockito.mockStatic(HttpConnection.class)) {
            httpConnMock.when(() -> HttpConnection.connect(url)).thenReturn(conStub);
            Document doc = Jsoup.parse(url, timeout);
            assertEquals("X", doc.body().text());
        }
    }

    @Test
    @DisplayName("TC10_O14: parse(URL url, int timeoutMillis) throws MalformedURLException when URL protocol is ftp")
    public void test_TC10_O14() {
        assertThrows(MalformedURLException.class, () -> {
            URL ftpUrl = new URL("ftp://example.com");
            Jsoup.parse(ftpUrl, 500);
        });
    }
}