package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(html, baseUri) with non-empty html and non-empty baseUri returns DOM with correct base URI (branch: html non-empty, baseUri non-empty)")
    void test_TC01_O1() {
        String html = "<p>Hello</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals(baseUri, doc.baseUri(), "Expected baseUri preserved on document");
        assertTrue(doc.body().html().contains("<p>Hello</p>"), "Body should contain the original paragraph");
    }

    @Test
    @DisplayName("parse(html, baseUri) with empty html returns empty body (branch: html empty)")
    void test_TC02_O1() {
        String html = "";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertTrue(doc.body().html().isEmpty(), "Expected body html to be empty when input html is empty");
    }

    @Test
    @DisplayName("parse(html, baseUri, parser) using custom Parser parses XML fragment (branch: xml parser override)")
    void test_TC03_O2() {
        String html = "<tag>1</tag>";
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        assertEquals(Parser.xmlParser().getSettings().syntax(), doc.outputSettings().syntax(),
                     "Expected document syntax to match xml parser syntax");
    }

    @Test
    @DisplayName("parse(html, parser) with empty baseUri defaults to empty base and uses html parser (branch: default baseUri)")
    void test_TC04_O3() {
        String html = "<p>X</p>";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals("", doc.baseUri(), "Expected default baseUri to be empty");
        assertTrue(doc.body().html().contains("<p>X</p>"), "Body should contain the original fragment");
    }

    @Test
    @DisplayName("parse(html) with only html defaults to empty baseUri and html parser (branch: single-arg overload)")
    void test_TC05_O4() {
        String html = "<div>Y</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.baseUri(), "Expected default baseUri to be empty");
        assertTrue(doc.body().html().contains("<div>Y</div>"), "Body should contain the given div");
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri) with existing file and valid charset returns parsed Document (branch: charsetName non-null)")
    void test_TC06_O5() throws IOException {
        File temp = File.createTempFile("jsoup-test", ".html");
        temp.deleteOnExit();
        String content = "<p>F</p>";
        Files.write(temp.toPath(), content.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "/tmp/base";
        Document doc = Jsoup.parse(temp, charset, baseUri);
        assertTrue(doc.body().html().contains("<p>F</p>"), "Parsed document should contain the file's paragraph");
        assertEquals(baseUri, doc.baseUri(), "Expected baseUri to be passed through");
    }

    @Test
    @DisplayName("parse(File, charsetName) with null charsetName falls back to BOM/meta or UTF-8 (branch: charsetName null)")
    void test_TC07_O5() throws IOException {
        File temp = File.createTempFile("jsoup-test-null", ".html");
        temp.deleteOnExit();
        String content = "<p>N</p>";
        Files.write(temp.toPath(), content.getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(temp, null);
        assertTrue(doc.body().html().contains("<p>N</p>"), "Parsed document should contain the file's paragraph");
        assertEquals(temp.getAbsolutePath(), doc.baseUri(), "Expected baseUri to default to file absolute path");
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) with closed stream throws IOException (exception branch)")
    void test_TC08_O9() {
        InputStream in = new ByteArrayInputStream("<p>dummy</p>".getBytes(StandardCharsets.UTF_8));
        try {
            in.close();
        } catch (IOException e) {
            // ignore
        }
        assertThrows(IOException.class, () -> Jsoup.parse(in, "UTF-8", "http://x/"),
                     "Expected IOException when reading from a closed InputStream");
    }

    @Test
    @DisplayName("parse(URL, timeout) with malformed URL throws MalformedURLException (exception branch)")
    void test_TC09_O11() throws Exception {
        URL url = new URL("ftp://bad");
        assertThrows(MalformedURLException.class, () -> {
            Jsoup.parse(url, 1000);
        }, "Expected MalformedURLException for non-http(s) URL");
    }

    @Test
    @DisplayName("parse(URL, timeout) with reachable site returns parsed Document (branch: successful GET)")
    void test_TC10_O11() throws Exception {
        URL url = new URL("http://example.com");
        int timeout = 500;
        Document stubDoc = Document.createShell("http://example.com");
        Connection mockConn = Mockito.mock(Connection.class);
        Mockito.when(mockConn.timeout(timeout)).thenReturn(mockConn);
        Mockito.when(mockConn.get()).thenReturn(stubDoc);

        try (MockedStatic<HttpConnection> httpMock = Mockito.mockStatic(HttpConnection.class)) {
            httpMock.when(() -> HttpConnection.connect(url)).thenReturn(mockConn);
            Document result = Jsoup.parse(url, timeout);
            assertSame(stubDoc, result, "Expected the stub Document returned from connection.get()");
        }
    }
}