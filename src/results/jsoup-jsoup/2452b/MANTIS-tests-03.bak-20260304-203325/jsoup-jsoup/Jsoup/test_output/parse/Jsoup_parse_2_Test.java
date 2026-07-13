package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.helper.HttpConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(File) one-arg overload reads file with default charset inference and sets baseUri to absolute path")
    void test_TC18() throws IOException {
        // GIVEN a temp file with <h3>OneArg</h3>
        File temp = File.createTempFile("tc18", ".html");
        temp.deleteOnExit();
        try (FileWriter fw = new FileWriter(temp)) {
            fw.write("<h3>OneArg</h3>");
        }
        // WHEN
        Document doc = Jsoup.parse(temp);
        // THEN
        // verifies parsing branch with default-charset path, baseUri from file abs path
        assertEquals("OneArg", doc.selectFirst("h3").text());
        assertEquals(temp.getAbsolutePath(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri, Parser) four-arg overload with xmlParser preserves provided baseUri")
    void test_TC19() throws IOException {
        // GIVEN a temp file with <node>X</node>, charset UTF-8, and xmlParser
        File temp = File.createTempFile("tc19", ".xml");
        temp.deleteOnExit();
        try (FileWriter fw = new FileWriter(temp)) {
            fw.write("<node>X</node>");
        }
        String baseUri = "http://filebase/";
        Parser parser = Parser.xmlParser();
        // WHEN
        Document doc = Jsoup.parse(temp, "UTF-8", baseUri, parser);
        // THEN
        // ensures xmlParser branch, baseUri unaffected
        assertEquals("X", doc.selectFirst("node").text());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(InputStream, null, baseUri) three-arg overload uses default charset detection and closes stream")
    void test_TC20() throws IOException {
        // GIVEN a stream containing <li>StreamNull</li>, null charset triggers detection path
        class TrackCloseStream extends ByteArrayInputStream {
            private boolean closed = false;
            TrackCloseStream(byte[] b) { super(b); }
            @Override public void close() throws IOException { closed = true; super.close(); }
            boolean isClosed() { return closed; }
        }
        TrackCloseStream in = new TrackCloseStream("<li>StreamNull</li>".getBytes());
        String baseUri = "http://instream/";
        // WHEN
        Document doc = Jsoup.parse(in, null, baseUri);
        // THEN
        // coverage for B0→B3→B5: default charset & stream-close branch
        assertEquals("StreamNull", doc.selectFirst("li").text());
        assertEquals(baseUri, doc.baseUri());
        assertTrue(in.isClosed(), "InputStream should be closed after parse");
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) throws SocketTimeoutException when connection timed out")
    void test_TC21() throws Exception {
        // GIVEN a mocked HttpConnection that throws on timeout call to cover exception branch
        URL url = new URL("http://example.com");
        HttpConnection.Connection connMock = mock(HttpConnection.Connection.class);
        when(connMock.timeout(anyInt())).thenThrow(new SocketTimeoutException("timed out"));
        try (MockedStatic<HttpConnection> mocked = mockStatic(HttpConnection.class)) {
            mocked.when(() -> HttpConnection.connect(url)).thenReturn(connMock);
            // WHEN / THEN
            assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, 1));
        }
    }

    @Test
    @DisplayName("parseBodyFragment(String, String) returns fragment in body with provided baseUri resolution")
    void test_TC22() {
        // GIVEN simple body fragment and baseUri
        String html = "<em>Frag</em>";
        String baseUri = "http://frag/";
        // WHEN
        Document doc = Jsoup.parseBodyFragment(html, baseUri);
        // THEN
        // covers fragment parse with non-empty baseUri
        assertEquals("<em>Frag</em>", doc.body().html());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parseBodyFragment(String) one-arg overload returns fragment in body with empty baseUri")
    void test_TC23() {
        // GIVEN simple body fragment, default-baseUri path
        String html = "<strong>Only</strong>";
        // WHEN
        Document doc = Jsoup.parseBodyFragment(html);
        // THEN
        assertEquals("<strong>Only</strong>", doc.body().html());
        assertEquals("", doc.baseUri());
    }
}