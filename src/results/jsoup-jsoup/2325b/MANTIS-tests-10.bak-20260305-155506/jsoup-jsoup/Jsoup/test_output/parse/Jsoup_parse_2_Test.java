package org.jsoup;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    /**
     * Helper InputStream that tracks whether close() was called.
     */
    static class TrackCloseStream extends ByteArrayInputStream {
        boolean closed = false;
        TrackCloseStream(byte[] buf) {
            super(buf);
        }
        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri, parser) uses a non-null charset and custom parser branch")
    public void test_TC28() throws IOException {
        TrackCloseStream in = new TrackCloseStream("<u/>".getBytes("UTF-8"));
        String charset = "UTF-8";
        String baseUri = "/b/";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(in, charset, baseUri, parser);
        assertEquals("<u/>", doc.body().html());
        assertTrue(in.closed, "InputStream should be closed after parsing");
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri, parser) uses custom XML parser on Path branch")
    public void test_TC29() throws IOException {
        Path path = Files.createTempFile("tc29", ".xml");
        try {
            Files.write(path, "<root/>".getBytes("UTF-8"));
            String charset = "UTF-8";
            String baseUri = "http://x/";
            Parser parser = Parser.xmlParser();
            Document doc = Jsoup.parse(path, charset, baseUri, parser);
            assertEquals("<root/>", doc.body().html());
            assertEquals(baseUri, doc.baseUri());
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) throws HttpStatusException on non-OK HTTP response")
    public void test_TC30() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
            }
        });
        server.start();
        try {
            URL url = new URL("http://localhost:" + server.getAddress().getPort() + "/");
            assertThrows(org.jsoup.HttpStatusException.class, () -> Jsoup.parse(url, 500));
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("parse(InputStream, null charsetName, baseUri) uses null charset branch without parser")
    public void test_TC31() throws IOException {
        TrackCloseStream in = new TrackCloseStream("<span>x</span>".getBytes("UTF-8"));
        String charset = null; // triggers null charset overload
        String baseUri = "base";
        Document doc = Jsoup.parse(in, charset, baseUri);
        assertEquals("<span>x</span>", doc.body().html());
        assertTrue(in.closed, "InputStream should be closed after parsing");
    }
}