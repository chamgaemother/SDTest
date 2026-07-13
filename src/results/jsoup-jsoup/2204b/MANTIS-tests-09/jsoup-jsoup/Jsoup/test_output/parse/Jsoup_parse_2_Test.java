package org.jsoup;

import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.UnsupportedMimeTypeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(html) with non-empty HTML returns a Document with correct body element")
    public void test_TC22() {
        // B0→B2→B5→B8: choose single-arg overload branch
        String html = "<p>hello</p>";
        Document doc = Jsoup.parse(html);
        // Expect the body fragment to match input exactly
        assertEquals(html, doc.body().html());
    }

    @Test
    @DisplayName("parse(html, Parser.htmlParser()) exercises htmlParser path in two-arg overload")
    public void test_TC23() {
        // B0→B3→B6→B9: two-arg overload with htmlParser
        String html = "<p>test</p>";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, parser);
        // The htmlParser should produce a <p> element with text 'test'
        assertEquals("test", doc.select("p").first().text());
    }

    @Test
    @DisplayName("parse(InputStream,charset,baseUri,Parser.xmlParser()) processes self-closing tags via xmlParser")
    public void test_TC24() throws IOException {
        // B0→B7→B11→B14: inputstream + xmlParser path
        byte[] data = "<item/>".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(in, "UTF-8", "base", parser);
        // xmlParser should treat <item/> as self-closing and include one item element
        assertEquals(1, doc.select("item").size());
    }

    @Test
    @DisplayName("parse(URL,timeout) throws HttpStatusException on HTTP 404 response")
    public void test_TC25() throws Exception {
        // B0→B15→B18→B20: network 404 leads to HttpStatusException
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/missing", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        try {
            int port = server.getAddress().getPort();
            URL url = new URL("http://localhost:" + port + "/missing");
            // Expect HttpStatusException when GET returns 404
            assertThrows(org.jsoup.HttpStatusException.class, () -> Jsoup.parse(url, 1000));
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("parse(URL,timeout) throws UnsupportedMimeTypeException on unsupported Content-Type")
    public void test_TC26() throws Exception {
        // B0→B15→B18→B21: network returns image/png triggers UnsupportedMimeTypeException
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/img", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                byte[] body = new byte[0];
                exchange.getResponseHeaders().add("Content-Type", "image/png");
                exchange.sendResponseHeaders(200, body.length);
                OutputStream os = exchange.getResponseBody();
                os.write(body);
                os.close();
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        try {
            int port = server.getAddress().getPort();
            URL url = new URL("http://localhost:" + port + "/img");
            assertThrows(UnsupportedMimeTypeException.class, () -> Jsoup.parse(url, 1000));
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("parse(URL,timeout) throws SocketTimeoutException when connection exceeds timeoutMillis")
    public void test_TC27() throws Exception {
        // B0→B15→B17→B19: simulate delayed response causing timeout
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/delay", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String response = "<p>late</p>";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        try {
            int port = server.getAddress().getPort();
            URL url = new URL("http://localhost:" + port + "/delay");
            // Timeout set to 50ms, but server sleeps 200ms
            assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, 50));
        } finally {
            server.stop(0);
        }
    }
}