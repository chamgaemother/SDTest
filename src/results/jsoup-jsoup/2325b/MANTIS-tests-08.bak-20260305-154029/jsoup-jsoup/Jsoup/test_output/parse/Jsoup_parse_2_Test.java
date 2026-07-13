package org.jsoup;

import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.Connection;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.jsoup.nodes.Element;
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

public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(File, null charsetName, baseUri, parser) uses Parser overload with null charset branch")
    public void test_TC21() throws Exception {
        // create temporary XML file to satisfy FileParserOverload branch with null charset
        File temp = File.createTempFile("tc21", ".xml");
        temp.deleteOnExit();
        try (FileWriter w = new FileWriter(temp)) {
            w.write("<root><x/></root>");
        }
        Parser xmlParser = Parser.xmlParser();
        String baseUri = "http://base/";
        // WHEN
        Document doc = Jsoup.parse(temp, null, baseUri, xmlParser);
        // THEN: baseUri set correctly and 'x' element parsed by xmlParser
        assertEquals(baseUri, doc.baseUri());
        assertEquals(1, doc.select("x").size());
    }

    @Test
    @DisplayName("parse(InputStream, null charsetName, baseUri, parser) uses InputStream parser overload with null charset branch")
    public void test_TC22() throws Exception {
        // ByteArrayInputStream of XML triggers StreamParserOverload branch with null charset
        String xml = "<root><y/></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        Parser xmlParser = Parser.xmlParser();
        String baseUri = "http://stream/";
        // WHEN
        Document doc = Jsoup.parse(in, null, baseUri, xmlParser);
        // THEN: baseUri set and 'y' element parsed by xmlParser
        assertEquals(baseUri, doc.baseUri());
        assertEquals(1, doc.select("y").size());
    }

    @Test
    @DisplayName("parse(File, bad charsetName, baseUri) throws IOException for invalid charset")
    public void test_TC23() throws Exception {
        // create temp HTML file to test invalid charset branch in DataUtil.load
        File temp = File.createTempFile("tc23", ".html");
        temp.deleteOnExit();
        try (FileWriter w = new FileWriter(temp)) {
            w.write("<p>hi</p>");
        }
        String badCharset = "INVALID-CHARSET";
        String baseUri = "http://x/";
        // WHEN / THEN: expect IOException due to unsupported charset
        assertThrows(IOException.class, () -> Jsoup.parse(temp, badCharset, baseUri));
    }

    @Test
    @DisplayName("parse(Path, bad charsetName, baseUri) throws IOException for invalid charset")
    public void test_TC24() throws Exception {
        // write simple HTML to temp path to hit PathCharsetBranch
        Path p = Files.createTempFile("tc24", ".html");
        p.toFile().deleteOnExit();
        Files.write(p, "<div>ok</div>".getBytes("UTF-8"));
        String badCharset = "NO-SUCH-CHARSET";
        String baseUri = "https://y/";
        // WHEN / THEN: expect IOException for unsupported charset via DataUtil.load
        assertThrows(IOException.class, () -> Jsoup.parse(p, badCharset, baseUri));
    }

    @Test
    @DisplayName("parse(InputStream, bad charsetName, baseUri) throws IOException for invalid charset")
    public void test_TC25() throws Exception {
        // InputStream with HTML triggers StreamCharsetBranch with invalid charset
        String html = "<p>z</p>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        String badCharset = "XYZ-123";
        String baseUri = "http://z/";
        // WHEN / THEN: IOException for unsupported charset
        assertThrows(IOException.class, () -> Jsoup.parse(in, badCharset, baseUri));
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) propagates HttpStatusException when GET returns non-OK status")
    public void test_TC26() throws Exception {
        URL url = new URL("http://example.com/");
        int timeout = 1000;
        // stub HttpConnection.connect to return a mocked Connection that throws HttpStatusException
        Connection mockConn = mock(Connection.class);
        when(mockConn.timeout(timeout)).thenReturn(mockConn);
        HttpStatusException httpEx = new HttpStatusException("Not OK", 500, url.toString());
        when(mockConn.get()).thenThrow(httpEx);
        try (MockedStatic<HttpConnection> sc = mockStatic(HttpConnection.class)) {
            sc.when(() -> HttpConnection.connect(url)).thenReturn(mockConn);
            // WHEN / THEN: HttpStatusException is propagated
            HttpStatusException thrown = assertThrows(HttpStatusException.class, () -> Jsoup.parse(url, timeout));
            assertEquals("Not OK", thrown.getMessage());
        }
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) propagates UnsupportedMimeTypeException when GET returns unsupported mime")
    public void test_TC27() throws Exception {
        URL url = new URL("http://example.com/");
        int timeout = 2000;
        // stub HttpConnection.connect to return a mocked Connection that throws UnsupportedMimeTypeException
        Connection mockConn = mock(Connection.class);
        when(mockConn.timeout(timeout)).thenReturn(mockConn);
        UnsupportedMimeTypeException mimeEx = new UnsupportedMimeTypeException("text/xyz", null);
        when(mockConn.get()).thenThrow(mimeEx);
        try (MockedStatic<HttpConnection> sc = mockStatic(HttpConnection.class)) {
            sc.when(() -> HttpConnection.connect(url)).thenReturn(mockConn);
            // WHEN / THEN: UnsupportedMimeTypeException is propagated
            UnsupportedMimeTypeException thrown = assertThrows(UnsupportedMimeTypeException.class,
                    () -> Jsoup.parse(url, timeout));
            assertEquals("text/xyz", thrown.getMimeType());
        }
    }
}