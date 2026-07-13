package org.jsoup;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.HttpStatusException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC20: parse(Path, charsetName, baseUri) reads file via Path overload and resolves relative links against provided baseUri")
    public void test_TC20() throws IOException {
        // GIVEN a temp HTML file with a relative link
        Path temp = Files.createTempFile("test", ".html");
        String html = "<a href=\"q.html\">x</a>";
        Files.write(temp, html.getBytes("UTF-8"));
        String charset = "UTF-8";
        String baseUri = "http://site.com/dir/";
        // WHEN parsing via Path overload
        Document doc = Jsoup.parse(temp, charset, baseUri);
        // THEN the <a> element's absolute URL should resolve against baseUri
        assertEquals("http://site.com/dir/q.html",
                doc.selectFirst("a").absUrl("href"));
        // cleanup
        Files.deleteIfExists(temp);
    }

    @Test
    @DisplayName("TC21: parse(Path, charsetName, baseUri) throws IOException when Path does not exist")
    public void test_TC21() {
        // GIVEN a non-existent Path
        Path missing = Path.of("no_such_file.html");
        String charset = "UTF-8";
        String baseUri = "u";
        // WHEN/THEN calling parse should throw IOException
        assertThrows(IOException.class, () -> Jsoup.parse(missing, charset, baseUri));
    }

    @Test
    @DisplayName("TC22: parse(URL, timeout) throws SocketTimeoutException if server delays beyond timeout")
    public void test_TC22() throws IOException {
        // GIVEN a server that delays response beyond timeout
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse()
                .setBody("<p>delayed</p>")
                .setBodyDelay(2, java.util.concurrent.TimeUnit.SECONDS));
        server.start();
        URL url = server.url("/").url();
        int timeout = 500; // ms, less than 2s delay
        // WHEN/THEN parse should throw SocketTimeoutException
        assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, timeout));
        server.shutdown();
    }

    @Test
    @DisplayName("TC23: parse(URL, timeout) throws HttpStatusException on non-200 HTTP status")
    public void test_TC23() throws IOException {
        // GIVEN a server that returns 404
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not found"));
        server.start();
        URL url = server.url("/").url();
        int timeout = 1000;
        // WHEN/THEN parse should throw HttpStatusException for status != 200
        assertThrows(HttpStatusException.class, () -> Jsoup.parse(url, timeout));
        server.shutdown();
    }

    @Test
    @DisplayName("TC24: parse(URL, timeout) throws UnsupportedMimeTypeException on unsupported content-type")
    public void test_TC24() throws IOException {
        // GIVEN a server that returns image/png content
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("PNGDATA")
                .addHeader("Content-Type", "image/png"));
        server.start();
        URL url = server.url("/img").url();
        int timeout = 1000;
        // WHEN/THEN parse should throw UnsupportedMimeTypeException for image/png
        assertThrows(UnsupportedMimeTypeException.class, () -> Jsoup.parse(url, timeout));
        server.shutdown();
    }

    @Test
    @DisplayName("TC25: parse(File, charsetName, baseUri, parser) throws IOException for invalid charset on gzipped file")
    public void test_TC25() throws IOException {
        // GIVEN a gzipped temp file and a bad charset
        Path temp = Files.createTempFile("gz", ".html.gz");
        File gzFile = temp.toFile();
        String content = "<p>paragraph</p>";
        try (FileOutputStream fos = new FileOutputStream(gzFile);
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
            gzos.write(content.getBytes("UTF-8"));
        }
        String badCharset = "BAD-CHARSET";
        String baseUri = "u";
        Parser parser = Parser.htmlParser();
        // WHEN/THEN parse should throw IOException due to invalid charset
        assertThrows(IOException.class, () -> Jsoup.parse(gzFile, badCharset, baseUri, parser));
        Files.deleteIfExists(temp);
    }
}