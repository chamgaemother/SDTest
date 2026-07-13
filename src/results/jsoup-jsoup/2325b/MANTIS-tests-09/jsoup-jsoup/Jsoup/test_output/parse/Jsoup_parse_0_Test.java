package org.jsoup;

import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.Connection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(String html, String baseUri) with simple HTML returns Document with correct baseUri")
    void test_TC01_O1() {
        String html = "<p>Hi</p>";
        String base = "http://example.com/";
        Document doc = Jsoup.parse(html, base);
        assertAll(
            () -> assertEquals("http://example.com/", doc.baseUri(), "Base URI should match input base"),
            () -> assertEquals("<p>Hi</p>", doc.body().html(), "Body HTML should preserve paragraph content")
        );
    }

    @Test
    @DisplayName("parse(String html, String baseUri) with empty html returns empty body element")
    void test_TC02_O1() {
        String html = "";
        String base = "http://x/";
        Document doc = Jsoup.parse(html, base);
        assertEquals("", doc.body().html(), "Empty HTML should yield empty body content");
    }

    @Test
    @DisplayName("parse(String html, String baseUri) with null html throws NullPointerException")
    void test_TC03_O1() {
        String base = "http://x/";
        assertThrows(NullPointerException.class, () -> Jsoup.parse((String) null, base));
    }

    @Test
    @DisplayName("parse(String html, Parser parser) with custom xmlParser returns Document parsed by provided parser")
    void test_TC04_O2() {
        String html = "<node>data</node>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertAll(
            () -> assertEquals("", doc.baseUri(), "Default baseUri for parse(html, parser) should be empty"),
            () -> assertEquals(1, doc.getElementsByTag("node").size(), "XML parser should create one <node> element")
        );
    }

    @Test
    @DisplayName("parse(String html, Parser parser) with null parser throws IllegalArgumentException")
    void test_TC05_O2() {
        String html = "<p>Test</p>";
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, (Parser) null));
    }

    @Test
    @DisplayName("parse(File file, String charsetName, String baseUri) loads from plain file returns Document")
    void test_TC06_O3() throws IOException {
        Path temp = Files.createTempFile("test", ".html");
        Files.write(temp, "<div>f</div>".getBytes(StandardCharsets.UTF_8));
        File f = temp.toFile();
        String cs = "UTF-8";
        String base = "/base";
        Document doc = Jsoup.parse(f, cs, base);
        assertAll(
            () -> assertEquals("<div>f</div>", doc.body().html(), "Body HTML should match file content"),
            () -> assertEquals(base, doc.baseUri(), "Base URI should be the provided base")
        );
        f.delete();
    }

    @Test
    @DisplayName("parse(File file, String charsetName) with file missing throws IOException")
    void test_TC07_O3() {
        File f = new File("no_such.html");
        String cs = "UTF-8";
        assertThrows(IOException.class, () -> Jsoup.parse(f, cs));
    }

    @Test
    @DisplayName("parse(Path path, String charsetName, String baseUri) loads from Path returns Document")
    void test_TC08_O4() throws IOException {
        Path p = Files.createTempFile("testPath", ".html");
        Files.write(p, "<span>p</span>".getBytes(StandardCharsets.UTF_8));
        String cs = "UTF-8";
        String base = "baseUri";
        Document doc = Jsoup.parse(p, cs, base);
        assertAll(
            () -> assertEquals("<span>p</span>", doc.body().html(), "Body HTML should match path content"),
            () -> assertEquals(base, doc.baseUri(), "Base URI should be the provided base")
        );
        Files.deleteIfExists(p);
    }

    @Test
    @DisplayName("parse(InputStream in, String charsetName, String baseUri) loads from stream returns Document")
    void test_TC09_O5() throws IOException {
        byte[] data = "<b>1</b>".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        String cs = "UTF-8";
        String base = "http://x/";
        Document doc = Jsoup.parse(in, cs, base);
        assertAll(
            () -> assertEquals("<b>1</b>", doc.body().html(), "Body HTML should match stream content"),
            () -> assertEquals(base, doc.baseUri(), "Base URI should be the provided base")
        );
    }

    @Test
    @DisplayName("parse(URL url, int timeoutMillis) with valid HTTP URL returns fetched Document")
    void test_TC10_O6() throws Exception {
        URL url = new URL("http://example.com");
        Connection con = Mockito.mock(Connection.class);
        Mockito.when(con.timeout(1000)).thenReturn(con);
        Document fakeDoc = Jsoup.parse("<ok>", "");
        Mockito.when(con.get()).thenReturn(fakeDoc);
        try (MockedStatic<HttpConnection> mocked = Mockito.mockStatic(HttpConnection.class)) {
            mocked.when(() -> HttpConnection.connect(url)).thenReturn(con);
            Document doc = Jsoup.parse(url, 1000);
            assertEquals("<ok>", doc.body().html(), "Body HTML should match mocked HTTP response");
        }
    }
}