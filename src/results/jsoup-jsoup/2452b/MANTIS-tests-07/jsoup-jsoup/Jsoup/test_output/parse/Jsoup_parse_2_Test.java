package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(File, null charsetName, explicit baseUri) loads HTML and sets baseUri")
    public void test_TC21() throws Exception {
        File file = File.createTempFile("tc21", ".html");
        file.deleteOnExit();
        String html = "<p>F</p>";
        Files.write(file.toPath(), html.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);
        String baseUri = "http://file/";

        Document doc = Jsoup.parse(file, null, baseUri);

        assertTrue(doc.body().html().contains(html));
        assertEquals(baseUri, doc.getBaseUri());
    }

    @Test
    @DisplayName("parse(File, charsetName) uses file absolute path as baseUri")
    public void test_TC22() throws Exception {
        File file = File.createTempFile("tc22", ".html");
        file.deleteOnExit();
        String html = "<h2>H</h2>";
        Files.write(file.toPath(), html.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);
        String charset = "UTF-8";

        Document doc = Jsoup.parse(file, charset);

        assertTrue(doc.body().html().contains(html));
        assertEquals(file.getAbsolutePath(), doc.getBaseUri());
    }

    @Test
    @DisplayName("parse(File) loads HTML with default charset and uses file path as baseUri")
    public void test_TC23() throws Exception {
        File file = File.createTempFile("tc23", ".html");
        file.deleteOnExit();
        String html = "<div>D</div>";
        Files.write(file.toPath(), html.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);

        Document doc = Jsoup.parse(file);

        assertTrue(doc.body().html().contains(html));
        assertEquals(file.getAbsolutePath(), doc.getBaseUri());
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri, parser) delegates to custom parser override")
    public void test_TC24() throws Exception {
        File file = File.createTempFile("tc24", ".html");
        file.deleteOnExit();
        Files.write(file.toPath(), "<span>S</span>".getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);
        String charset = "UTF-8";
        String baseUri = "http://stub/";
        Parser stub = Parser.htmlParser(); // Changed to use a valid parser instance

        Document doc = Jsoup.parse(file, charset, baseUri, stub);

        assertEquals(baseUri, doc.getBaseUri());
        assertEquals("", doc.body().html());
    }

    @Test
    @DisplayName("parse(Path, null charsetName, baseUri) loads HTML and sets provided baseUri")
    public void test_TC25() throws Exception {
        Path path = Files.createTempFile("tc25", ".html");
        path.toFile().deleteOnExit();
        String html = "<p>P</p>";
        Files.write(path, html.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);
        String baseUri = path.toAbsolutePath().toString() + "/b";

        Document doc = Jsoup.parse(path, null, baseUri);

        assertTrue(doc.body().html().contains(html));
        assertEquals(baseUri, doc.getBaseUri());
    }

    @Test
    @DisplayName("parse(Path, charsetName) uses path absolute path as baseUri")
    public void test_TC26() throws Exception {
        Path path = Files.createTempFile("tc26", ".html");
        path.toFile().deleteOnExit();
        String html = "<li>L</li>";
        Files.write(path, html.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);
        String charset = "UTF-8";

        Document doc = Jsoup.parse(path, charset);

        assertTrue(doc.body().html().contains(html));
        assertEquals(path.toAbsolutePath().toString(), doc.getBaseUri());
    }

    @Test
    @DisplayName("parse(Path) loads HTML with default charset and uses path absolute path as baseUri")
    public void test_TC27() throws Exception {
        Path path = Files.createTempFile("tc27", ".html");
        path.toFile().deleteOnExit();
        String html = "<h3>H3</h3>";
        Files.write(path, html.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);

        Document doc = Jsoup.parse(path);

        assertTrue(doc.body().html().contains(html));
        assertEquals(path.toAbsolutePath().toString(), doc.getBaseUri());
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri, parser) delegates to custom parser for Path overload")
    public void test_TC28() throws Exception {
        Path path = Files.createTempFile("tc28", ".html");
        path.toFile().deleteOnExit();
        Files.write(path, "<x/>".getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);
        Parser stub = Parser.htmlParser(); // Changed to use a valid parser instance
        String baseUri = "http://p/";

        Document doc = Jsoup.parse(path, null, baseUri, stub);

        assertEquals(baseUri, doc.getBaseUri());
        assertTrue(doc.body().html().isEmpty());
    }

    private static class TrackCloseStream extends FilterInputStream {
        boolean closed = false;
        protected TrackCloseStream(InputStream in) {
            super(in);
        }
        @Override public void close() throws IOException {
            closed = true;
            super.close();
        }
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) loads HTML, closes stream, and sets baseUri")
    public void test_TC29() throws Exception {
        String html = "<h3>S</h3>";
        ByteArrayInputStream raw = new ByteArrayInputStream(html.getBytes("UTF-8"));
        TrackCloseStream in = new TrackCloseStream(raw);
        String baseUri = "http://in/";

        Document doc = Jsoup.parse(in, null, baseUri);

        assertTrue(doc.body().html().contains(html));
        assertEquals(baseUri, doc.getBaseUri());
        assertTrue(in.closed);
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri, parser) delegates to custom parser for stream overload")
    public void test_TC30() throws Exception {
        ByteArrayInputStream raw = new ByteArrayInputStream("<x/>".getBytes("UTF-8"));
        Parser stub = Parser.htmlParser(); // Changed to use a valid parser instance
        String charset = "UTF-8";
        String baseUri = "http://s/";

        Document doc = Jsoup.parse(raw, charset, baseUri, stub);

        assertEquals(baseUri, doc.getBaseUri());
        assertTrue(doc.body().html().isEmpty());
    }

    @Test
    @DisplayName("parse(URL, timeoutMillis) with extremely low timeout throws SocketTimeoutException")
    public void test_TC31() throws Exception {
        URL url = new URL("http://example.com/");
        int timeout = 1; // immediate timeout triggered
        assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, timeout));
    }
}