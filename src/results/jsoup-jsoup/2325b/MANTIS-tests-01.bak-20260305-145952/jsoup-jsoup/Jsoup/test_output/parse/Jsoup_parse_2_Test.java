package org.jsoup;

import org.jsoup.helper.HttpConnection;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document;
import org.jsoup.Connection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.net.URL;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(String html, String baseUri, Parser parser) invokes parser.parseInput and returns its Document")
    public void test_TC17() {
        // Inline stub captures input parameters as per path B0→B4→B5
        String html = "<tag/>";
        String baseUri = "http://base/";
        AtomicReference<String> inHtml = new AtomicReference<>();
        AtomicReference<String> inBase = new AtomicReference<>();
        Document stubDoc = new Document("stub");
        Parser stubParser = new Parser(null) {
            @Override public Document parseInput(String h, String b) {
                inHtml.set(h);
                inBase.set(b);
                return stubDoc;
            }
        };
        // WHEN: call the overload with custom parser
        Document result = Jsoup.parse(html, baseUri, stubParser);
        // THEN: it should return exactly the stubDoc and capture correct args
        assertSame(stubDoc, result, "Expected returned Document to be the one from stubParser");
        assertEquals(html, inHtml.get(), "Parser should receive the original html");
        assertEquals(baseUri, inBase.get(), "Parser should receive the original baseUri");
    }

    @Test
    @DisplayName("parse(String html, String baseUri, Parser parser) propagates exception thrown by parser.parseInput")
    public void test_TC18() {
        // Exception path B0→B4→B6: stub parser throws
        String html = "data";
        String baseUri = "base";
        Parser stubParser = new Parser(null) {
            @Override public Document parseInput(String inHtml, String b) {
                throw new IllegalStateException("fail");
            }
        };
        // WHEN/THEN: invoking parse should throw IllegalStateException with message
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> Jsoup.parse(html, baseUri, stubParser),
                "Expected IllegalStateException to be thrown from stubParser");
        assertEquals("fail", ex.getMessage(), "Exception message should propagate from stubParser");
    }

    @Test
    @DisplayName("parse(URL url, int timeoutMillis) uses Connection.timeout and get to return a Document")
    public void test_TC19() throws IOException {
        // Network path B0→B28→B29→B30→B31: stub static connect and connection behavior
        URL url = new URL("http://example.com");
        int timeout = 5000;
        Document stubDoc = new Document("from-url");
        Connection mockCon = mock(Connection.class);
        // Stub static method HttpConnection.connect via Mockito
        try (MockedStatic<HttpConnection> mocked = Mockito.mockStatic(HttpConnection.class)) {
            mocked.when(() -> HttpConnection.connect(url)).thenReturn(mockCon);
            // stub the connection get() to return our document
            when(mockCon.get()).thenReturn(stubDoc);
            // WHEN: call parse(URL, timeout)
            Document result = Jsoup.parse(url, timeout);
            // THEN: should set timeout and return stubDoc
            // Verify timeout was set before get
            verify(mockCon).timeout(timeout);
            assertSame(stubDoc, result, "Expected returned Document to be the one from connection.get()");
        }
    }
}