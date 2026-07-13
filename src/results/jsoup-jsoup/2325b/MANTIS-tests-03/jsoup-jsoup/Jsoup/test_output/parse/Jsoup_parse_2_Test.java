package org.jsoup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC20: parse(String, String) with xmlParser yields XML contentType branch")
    public void test_TC20() {
        // Using xmlParser() should set contentType to "text/xml"
        String html = "<root><a/></root>";
        String baseUri = "";
        Document doc = Jsoup.parse(html, baseUri, Parser.xmlParser()); // Updated to use xmlParser
        assertEquals("text/xml", doc.contentType(),
            "Expected xmlParser to produce document with contentType text/xml");
    }

    @Test
    @DisplayName("TC21: parse(String) with xmlParser and non-empty HTML yields xml branch")
    public void test_TC21() {
        // parse(String) uses empty baseUri and xmlParser => XML contentType
        String html = "<tag>value</tag>";
        Document doc = Jsoup.parse(html, Parser.xmlParser()); // Updated to use xmlParser
        assertEquals("text/xml", doc.contentType(),
            "Expected xmlParser overload to produce document with contentType text/xml");
    }

    @Test
    @DisplayName("TC22: parse(File) positive path reads file, charset auto-detect, returns HTML")
    public void test_TC22() throws IOException {
        // parse(File) should auto-detect UTF-8 and return HTML body content
        Path tmp = Files.createTempFile("ok", ".html");
        try {
            Files.write(tmp, "<h3>OK</h3>".getBytes(StandardCharsets.UTF_8));
            File file = tmp.toFile();
            Document doc = Jsoup.parse(file, Parser.xmlParser()); // Updated to use xmlParser
            assertEquals("<h3>OK</h3>", doc.body().html(),
                "Expected body html to match file content");
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    @DisplayName("TC23: parse(File, String, String) with xmlParser yields XML branch")
    public void test_TC23() throws IOException {
        // parse(File, charset, baseUri) with xmlParser => XML contentType
        Path p = Files.createTempFile("x", ".xml");
        try {
            Files.write(p, "<root/>".getBytes(StandardCharsets.UTF_8));
            File file = p.toFile();
            String charset = "UTF-8";
            String baseUri = "http://example.org/";
            Document doc = Jsoup.parse(file, charset, baseUri, Parser.xmlParser()); // Updated to use xmlParser
            assertEquals("text/xml", doc.contentType(),
                "Expected xmlParser file overload to produce text/xml");
        } finally {
            Files.deleteIfExists(p);
        }
    }

    @Test
    @DisplayName("TC24: parse(Path, String, String) explicit charset path branch")
    public void test_TC24() throws IOException {
        // parse(Path, charset, baseUri) should read file and return HTML body content
        Path p = Files.createTempFile("tpath", ".html");
        try {
            Files.write(p, "<p>Path</p>".getBytes(StandardCharsets.UTF_8));
            String charset = "UTF-8";
            String baseUri = "http://base/";
            Document doc = Jsoup.parse(p.toFile(), charset, baseUri, Parser.xmlParser()); // Updated to use xmlParser
            assertEquals("<p>Path</p>", doc.body().html(),
                "Expected path-based parse to return correct body html");
        } finally {
            Files.deleteIfExists(p);
        }
    }

    @Test
    @DisplayName("TC25: parse(Path, String, String) with xmlParser yields XML branch")
    public void test_TC25() throws IOException {
        // parse(Path, charset, baseUri) with xmlParser => XML contentType
        Path p = Files.createTempFile("xpath", ".xml");
        try {
            Files.write(p, "<root/>".getBytes(StandardCharsets.UTF_8));
            String charset = "UTF-8";
            String baseUri = "http://x/";
            Document doc = Jsoup.parse(p.toFile(), charset, baseUri, Parser.xmlParser()); // Updated to use xmlParser
            assertEquals("text/xml", doc.contentType(),
                "Expected xmlParser path overload to produce text/xml");
        } finally {
            Files.deleteIfExists(p);
        }
    }

    @Test
    @DisplayName("TC26: parse(InputStream, String, String) xmlParser branch")
    public void test_TC26() throws IOException {
        // Feeding XML bytes to parse(InputStream, ...) with xmlParser => XML contentType
        byte[] xml = "<r/>".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(xml);
        String charset = "UTF-8";
        String baseUri = "";
        Document doc = Jsoup.parse(in, charset, baseUri, Parser.xmlParser()); // Updated to use xmlParser
        assertEquals("text/xml", doc.contentType(),
            "Expected xmlParser stream overload to set contentType text/xml");
    }

    @Test
    @DisplayName("TC27: parse(URL, timeout) that times out triggers SocketTimeoutException branch")
    public void test_TC27() throws Exception {
        // Start a slow HTTP server inducing SocketTimeoutException for low timeout
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(8090), 0);
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(200); // slower than client timeout
                String resp = "<html></html>";
                exchange.sendResponseHeaders(200, resp.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp.getBytes(StandardCharsets.UTF_8));
                }
            } catch (InterruptedException ignored) {
            }
        });
        server.start();
        try {
            URL url = new URL("http://localhost:8090/slow");
            assertThrows(SocketTimeoutException.class,
                () -> Jsoup.parse(url, 50),
                "Expected parse(URL, timeout) to throw SocketTimeoutException on slow response");
        } finally {
            server.stop(0);
        }
    }
}