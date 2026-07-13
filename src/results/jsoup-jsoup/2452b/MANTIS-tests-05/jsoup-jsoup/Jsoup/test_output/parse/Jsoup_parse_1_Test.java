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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(File, charsetName, baseUri, Parser) returns Document using provided parser")
    public void test_TC12() throws Exception {
        File temp = File.createTempFile("tc12", ".html");
        try (FileWriter w = new FileWriter(temp)) {
            w.write("<tag>XML</tag>");
        }
        String charset = "UTF-8";
        String baseUri = "http://base/";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(temp, charset, baseUri, parser);
        assertEquals("<tag>XML</tag>", doc.body().html());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri) returns Document with baseUri equal to provided string")
    public void test_TC13() throws Exception {
        Path path = Files.createTempFile("tc13", ".html");
        Files.write(path, "<p>Path</p>".getBytes());
        String charset = "UTF-8";
        String baseUri = "http://path.base/";
        Document doc = Jsoup.parse(path, charset, baseUri);
        assertEquals("<p>Path</p>", doc.body().html());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path, charsetName) sets baseUri to path.toAbsolutePath().toString()")
    public void test_TC14() throws Exception {
        Path path = Files.createTempFile("tc14", ".html");
        Files.write(path, "<div>Default</div>".getBytes());
        String charset = "UTF-8";
        Document doc = Jsoup.parse(path, charset);
        String expectedBase = path.toAbsolutePath().toString();
        assertEquals("<div>Default</div>", doc.body().html());
        assertEquals(expectedBase, doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path) sets charsetName null and baseUri to absolute path")
    public void test_TC15() throws Exception {
        Path path = Files.createTempFile("tc15", ".html");
        Files.write(path, "<span>Auto</span>".getBytes());
        Document doc = Jsoup.parse(path);
        String expectedBase = path.toAbsolutePath().toString();
        assertEquals("<span>Auto</span>", doc.body().html());
        assertEquals(expectedBase, doc.baseUri());
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) returns Document reading from stream")
    public void test_TC16() throws Exception {
        String html = "<b>Bytes</b>";
        InputStream in = new ByteArrayInputStream(html.getBytes());
        String charset = "UTF-8";
        String baseUri = "http://stream/";
        Document doc = Jsoup.parse(in, charset, baseUri);
        assertEquals("<b>Bytes</b>", doc.body().html());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri, Parser) returns Document using provided parser")
    public void test_TC17() throws Exception {
        String html = "<i>It</i>";
        InputStream in = new ByteArrayInputStream(html.getBytes());
        String charset = "UTF-8";
        String baseUri = "http://stream2/";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(in, charset, baseUri, parser);
        assertEquals("<i>It</i>", doc.body().html());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) returns Document when connection and get succeed")
    public void test_TC18() throws Exception {
        URL url = new URL("http://ok.example.com");
        int timeout = 500;
        Document expected = Parser.parse("<p>OK</p>", url.toString());
        try (MockedStatic<HttpConnection> ms = mockStatic(HttpConnection.class)) {
            HttpConnection con = mock(HttpConnection.class);
            ms.when(() -> HttpConnection.connect(url)).thenReturn(con);
            when(con.timeout(timeout)).thenReturn(con);
            when(con.get()).thenReturn(expected);
            Document doc = Jsoup.parse(url, timeout);
            assertEquals("<p>OK</p>", doc.body().html());
        }
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) throws HttpStatusException on non-OK status")
    public void test_TC19() throws Exception {
        URL url = new URL("http://error.example.com");
        int timeout = 200;
        try (MockedStatic<HttpConnection> ms = mockStatic(HttpConnection.class)) {
            HttpConnection con = mock(HttpConnection.class);
            ms.when(() -> HttpConnection.connect(url)).thenReturn(con);
            when(con.timeout(timeout)).thenReturn(con);
            org.jsoup.HttpStatusException ex = new org.jsoup.HttpStatusException("404 not found", 404, url.toString());
            when(con.get()).thenThrow(ex);
            org.jsoup.HttpStatusException thrown = assertThrows(org.jsoup.HttpStatusException.class,
                () -> Jsoup.parse(url, timeout));
            assertEquals("404 not found", thrown.getMessage());
        }
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) throws UnsupportedMimeTypeException on bad mime type")
    public void test_TC20() throws Exception {
        URL url = new URL("http://bad.example.com");
        int timeout = 300;
        try (MockedStatic<HttpConnection> ms = mockStatic(HttpConnection.class)) {
            HttpConnection con = mock(HttpConnection.class);
            ms.when(() -> HttpConnection.connect(url)).thenReturn(con);
            when(con.timeout(timeout)).thenReturn(con);
            org.jsoup.UnsupportedMimeTypeException ex =
                new org.jsoup.UnsupportedMimeTypeException(url, "image/png");
            when(con.get()).thenThrow(ex);
            assertThrows(org.jsoup.UnsupportedMimeTypeException.class,
                () -> Jsoup.parse(url, timeout));
        }
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri) throws IOException on invalid charsetName")
    public void test_TC21() throws Exception {
        File temp = File.createTempFile("tc21", ".html");
        try (FileWriter w = new FileWriter(temp)) {
            w.write("<p>Bad</p>");
        }
        String invalidCharset = "NO_SUCH_CHARSET";
        String baseUri = "";
        assertThrows(IOException.class, () -> Jsoup.parse(temp, invalidCharset, baseUri));
    }

}