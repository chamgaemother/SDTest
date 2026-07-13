package org.jsoup;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(html, baseUri, parser) with null parser throws NullPointerException")
    public void test_TC20() {
        // Using null parser should trigger NPE according to contract
        String html = "<p>x</p>";
        String baseUri = "http://example/";
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, baseUri, (Parser) null));
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri) with invalid charsetName throws IOException")
    public void test_TC21() {
        // Nonexistent file or invalid charset leads to IOException in DataUtil.load
        File f = new File("nonexistent.html");
        String invalidCharset = "BAD-CHARSET";
        String baseUri = "http://base/";
        assertThrows(IOException.class, () -> Jsoup.parse(f, invalidCharset, baseUri));
    }

    @Test
    @DisplayName("parse(Path) on non-existent path throws IOException")
    public void test_TC22() {
        // Path not present should cause IOException
        Path p = Paths.get("no_such_file.html");
        assertThrows(IOException.class, () -> Jsoup.parse(p));
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) with valid HTTP URL returns parsed Document")
    public void test_TC23() throws Exception {
        // Start simple HTTP server returning '<div>ok</div>' to satisfy happy path
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        String body = "<div>ok</div>";
        server.createContext("/test.html", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                byte[] resp = body.getBytes();
                exchange.sendResponseHeaders(200, resp.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp);
                }
            }
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            URL url = new URL("http://localhost:" + port + "/test.html");
            int timeout = 5000;
            Document doc = Jsoup.parse(url, timeout);
            // Validate that the parsed body HTML matches the served content
            assertEquals(body, doc.body().html());
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) with too-small timeout throws SocketTimeoutException")
    public void test_TC24() throws Exception {
        // HTTP server delays response beyond timeout to force SocketTimeoutException
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/delay.html", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    Thread.sleep(200); // Delay longer than client timeout
                } catch (InterruptedException ignored) {}
                String resp = "delayed";
                exchange.sendResponseHeaders(200, resp.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp.getBytes());
                }
            }
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            URL url = new URL("http://localhost:" + port + "/delay.html");
            int timeout = 50;
            assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, timeout));
        } finally {
            server.stop(0);
        }
    }
}