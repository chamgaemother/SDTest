package org.jsoup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC17: parse(String html, Parser parser) with null html throws IllegalArgumentException")
    public void test_TC17() {
        // Providing null html should trigger IllegalArgumentException per contract
        String html = null;
        Parser parser = Parser.htmlParser();
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, parser));
    }

    @Test
    @DisplayName("TC18: parse(String html, Parser parser) with null parser throws IllegalArgumentException")
    public void test_TC18() {
        // Providing a null parser should trigger IllegalArgumentException per contract
        String html = "<p>test</p>";
        Parser parser = null;
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, parser));
    }

    @Test
    @DisplayName("TC19: parse(Path path, String charsetName, String baseUri, Parser) with XML parser produces correct element")
    public void test_TC19() throws IOException {
        // Using XML parser on a temp file containing XML ensures xml paths are taken
        String xmlContent = "<item>42</item>";
        Path temp = Files.createTempFile("jsoup_test", ".xml");
        Files.write(temp, xmlContent.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "unused";
        Parser parser = Parser.xmlParser();

        Document doc = Jsoup.parse(temp, charset, baseUri, parser);
        assertEquals("42", doc.select("item").text());
    }

    @Test
    @DisplayName("TC20: parse(InputStream in, null charsetName, String baseUri) uses default charset UTF-8")
    public void test_TC20() throws IOException {
        // Providing null charset should default to UTF-8 branch and close the stream
        byte[] data = "<div>OK</div>".getBytes(StandardCharsets.UTF_8);
        class TrackStream extends ByteArrayInputStream {
            boolean closed = false;
            TrackStream(byte[] buf) { super(buf); }
            @Override public void close() throws IOException { closed = true; super.close(); }
        }
        TrackStream in = new TrackStream(data);
        String charset = null;
        String baseUri = "http://base/";

        Document doc = Jsoup.parse(in, charset, baseUri);
        assertEquals("<div>OK</div>", doc.body().html());
        assertTrue(in.closed, "InputStream should be closed after parsing");
    }

    @Test
    @DisplayName("TC21: parse(URL url, int timeoutMillis) on unreachable host throws SocketTimeoutException")
    public void test_TC21() {
        // Using a non-routable IP ensures a timeout branch is exercised
        try {
            URL url = new URL("http://10.255.255.1/");
            int timeout = 100;
            assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, timeout));
        } catch (Exception e) {
            fail("Unexpected exception during setup: " + e.getMessage());
        }
    }
}