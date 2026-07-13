package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(File) loads file with default null charset and uses file absolute path as baseUri")
    public void test_TC16() throws IOException {
        // Given a temp HTML file with UTF-8 content, exercising the default-file-path branch
        Path temp = Files.createTempFile("t", ".html");
        Files.write(temp, "<h3>Main</h3>".getBytes(StandardCharsets.UTF_8));
        File f = temp.toFile();
        // When
        Document doc = Jsoup.parse(f);
        // Then: content should match and baseUri is the file's absolute path
        assertEquals("<h3>Main</h3>", doc.body().html());
        assertEquals(f.getAbsolutePath(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri, Parser) loads file using provided parser and charsetName null")
    public void test_TC17() throws IOException {
        // Given XML-like content and xmlParser to hit the parser-input branch
        Path temp = Files.createTempFile("txml", ".html");
        Files.write(temp, "<node>val</node>".getBytes(StandardCharsets.UTF_8));
        File f = temp.toFile();
        String base = "http://xml/";
        Parser parser = Parser.xmlParser();
        // When
        Document doc = Jsoup.parse(f, null, base, parser);
        // Then: xml element should be present and baseUri preserved
        assertEquals(1, doc.getElementsByTag("node").size());
        assertEquals(base, doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path) loads path with default null charset and uses absolute path as baseUri")
    public void test_TC18() throws IOException {
        // Given a temp Path with HTML to cover the path-file-default-branch
        Path p = Files.createTempFile("tp", ".html");
        Files.write(p, "<div>P</div>".getBytes(StandardCharsets.UTF_8));
        // When
        Document doc = Jsoup.parse(p);
        // Then: body HTML matches and baseUri is the absolute path string
        assertEquals("<div>P</div>", doc.body().html());
        assertEquals(p.toAbsolutePath().toString(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri, Parser) loads stream via provided parser")
    public void test_TC19() throws IOException {
        // Given a ByteArrayInputStream and xmlParser for the stream-branch
        byte[] data = "<t>1</t>".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        String charset = "UTF-8";
        String base = "http://b/";
        Parser parser = Parser.xmlParser();
        // When
        Document doc = Jsoup.parse(in, charset, base, parser);
        // Then: tag t parsed once and baseUri matches
        assertEquals(1, doc.getElementsByTag("t").size());
        assertEquals(base, doc.baseUri());
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) with non-http scheme URL throws MalformedURLException")
    public void test_TC20() throws Exception {
        // Given an unsupported ftp URL to exercise protocol check
        URL url = new URL("ftp://example.com/file");
        int timeout = 1000;
        // Then: parse should immediately throw MalformedURLException
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, timeout));
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) propagates SocketTimeoutException from Connection.get()")
    public void test_TC21() throws Exception {
        // Given a mocked HttpConnection.connect to force a timeout on get()
        URL url = new URL("http://slow.example");
        int tm = 1234;
        MockedStatic<HttpConnection> ms = Mockito.mockStatic(HttpConnection.class);
        try {
            Connection con = mock(Connection.class);
            when(con.timeout(tm)).thenReturn(con);
            when(con.get()).thenThrow(new SocketTimeoutException());
            ms.when(() -> HttpConnection.connect(url)).thenReturn(con);
            // Then: SocketTimeoutException should be thrown
            assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, tm));
        } finally {
            ms.close();
        }
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri, Parser) loads path and uses provided parser")
    public void test_TC22() throws IOException {
        // Given XML-like content in a Path and xmlParser to cover parser-branch
        Path p = Files.createTempFile("tpxml", ".html");
        Files.write(p, "<e>v</e>".getBytes(StandardCharsets.UTF_8));
        String cs = "UTF-8";
        String base = "http://px/";
        Parser parser = Parser.xmlParser();
        // When
        Document doc = Jsoup.parse(p, cs, base, parser);
        // Then: element e found once and baseUri set to provided base
        assertEquals(1, doc.getElementsByTag("e").size());
        assertEquals(base, doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path, charsetName) loads path and uses absolute path as baseUri")
    public void test_TC23() throws IOException {
        // Given a Path with HTML and explicit charset to hit path-with-charset-branch
        Path p = Files.createTempFile("tp2", ".html");
        Files.write(p, "<span>Z</span>".getBytes(StandardCharsets.UTF_8));
        String cs = "UTF-8";
        // When
        Document doc = Jsoup.parse(p, cs);
        // Then: body HTML matches and baseUri is absolute path string
        assertEquals("<span>Z</span>", doc.body().html());
        assertEquals(p.toAbsolutePath().toString(), doc.baseUri());
    }
}