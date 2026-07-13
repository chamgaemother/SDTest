package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.HttpStatusException;
import org.jsoup.Connection;
import org.jsoup.safety.Safelist;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

// Ensure Mockito is included in dependencies
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC19: parse(InputStream, charset, baseUri) uses DataUtil.load without parser")
    public void test_TC19() throws IOException {
        // GIVEN a simple HTML fragment in a ByteArrayInputStream
        byte[] htmlBytes = "<u>U</u>".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(htmlBytes);
        String charset = "UTF-8";
        String base = "http://inputstream/";
        // WHEN parsing via Jsoup.parse(InputStream, charset, baseUri)
        Document doc = Jsoup.parse(in, charset, base);
        // THEN the body HTML matches and baseUri is preserved
        Assertions.assertEquals("<u>U</u>", doc.body().html()); 
        Assertions.assertEquals(base, doc.baseUri());
    }

    @Test
    @DisplayName("TC20: parse(InputStream, null charsetName, baseUri) closes stream and handles null charset")
    public void test_TC20() throws IOException {
        // GIVEN a simple HTML fragment and null charset to trigger BOM/meta or UTF-8 fallback
        byte[] htmlBytes = "<i>Null</i>".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(htmlBytes);
        String base = "http://null-charset/";
        // WHEN parsing with null charset
        Document doc = Jsoup.parse(in, null, base);
        // THEN body contains the fragment and baseUri preserved
        Assertions.assertTrue(doc.body().html().contains("<i>Null</i>"));
        Assertions.assertEquals(base, doc.baseUri());
        // AND the stream should be closed/read fully
        int after = in.read();
        Assertions.assertEquals(-1, after);
    }

    @Test
    @DisplayName("TC21: parse(File) default charset uses null and file path as baseUri")
    public void test_TC21() throws IOException {
        // GIVEN a temp HTML file with UTF-8 content
        File temp = File.createTempFile("jsoupTC21", ".html");
        temp.deleteOnExit();
        String html = "<h1>Default</h1>";
        Files.write(temp.toPath(), html.getBytes("UTF-8"));
        // WHEN parsing via Jsoup.parse(File)
        Document doc = Jsoup.parse(temp);
        // THEN body HTML matches and baseUri is the file absolute path
        Assertions.assertEquals(html, doc.body().html());
        Assertions.assertEquals(temp.getAbsolutePath(), doc.baseUri());
    }

    @Test
    @DisplayName("TC22: parse(File, null charsetName, baseUri) triggers BOM/meta detection path")
    public void test_TC22() throws IOException {
        // GIVEN a temp file with HTML and null charset to test fallback
        File f = File.createTempFile("jsoupTC22", ".html");
        f.deleteOnExit();
        String html = "<p>BOM</p>";
        Files.write(f.toPath(), html.getBytes("UTF-8"));
        String base = "https://bom.example/";
        // WHEN parsing with null charset and explicit baseUri
        Document doc = Jsoup.parse(f, null, base);
        // THEN body HTML matches and baseUri is preserved
        Assertions.assertEquals(html, doc.body().html());
        Assertions.assertEquals(base, doc.baseUri());
    }

    @Test
    @DisplayName("TC23: parse(URL, timeout) propagates HttpStatusException from connection.get()")
    public void test_TC23() throws Exception {
        // GIVEN a URL and a mocked Connection that throws HttpStatusException on get()
        URL url = new URL("http://example.com/error");
        Connection con = Mockito.mock(Connection.class);
        // condition: ensure timeout chain returns the same connection
        Mockito.when(con.timeout(500)).thenReturn(con);
        // simulate HttpStatusException being thrown by get()
        HttpStatusException httpEx = new HttpStatusException("status error", 500, url.toString());
        Mockito.when(con.get()).thenThrow(httpEx);
        // inject mock into HttpConnection.connect(URL)
        try (MockedStatic<HttpConnection> ms = Mockito.mockStatic(HttpConnection.class)) {
            ms.when(() -> HttpConnection.connect(url)).thenReturn(con);
            // WHEN calling Jsoup.parse(url, timeout)
            // THEN the same HttpStatusException should be thrown
            HttpStatusException thrown = Assertions.assertThrows(HttpStatusException.class, () -> Jsoup.parse(url, 500));
            Assertions.assertEquals("status error", thrown.getMessage());
            Assertions.assertEquals(500, thrown.getStatusCode());
            Assertions.assertEquals(url.toString(), thrown.getUrl());
        }
    }
}