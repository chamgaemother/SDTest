package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.helper.HttpConnection;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(html, parser) with null html throws IllegalArgumentException")
    public void test_TC11() {
        // html is null to trigger argument validation (intended behavior: reject null input)
        String html = null;
        Parser parser = Parser.htmlParser();
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, parser));
    }

    @Test
    @DisplayName("parse(html, parser) with null parser throws NullPointerException")
    public void test_TC12() {
        // parser is null to trigger NPE when parsing is attempted
        String html = "<p>x</p>";
        Parser parser = null;
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, parser));
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri) with valid small HTML file returns Document with correct baseUri")
    public void test_TC13() throws IOException {
        // create temp file with a simple HTML title to cover file overload branch
        File temp = File.createTempFile("jsoupTest", ".html");
        temp.deleteOnExit();
        String htmlContent = "<html><head><title>F</title></head><body></body></html>";
        Files.write(temp.toPath(), htmlContent.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://base/";
        Document doc = Jsoup.parse(temp, charset, baseUri);
        assertAll(
            () -> assertEquals("F", doc.title(), "Title should be parsed from the file content"),
            () -> assertEquals(baseUri, doc.baseUri(), "Base URI should match the provided baseUri parameter")
        );
    }

    @Test
    @DisplayName("parse(File, charsetName) with non-existent file throws IOException")
    public void test_TC14() {
        // non-existent file to trigger IO exception in file overload
        File missing = new File("no-such-file-12345.html");
        String charset = "UTF-8";
        assertThrows(IOException.class, () -> Jsoup.parse(missing, charset));
    }

    @Test
    @DisplayName("parse(File) with valid file infers UTF-8 and uses file path as baseUri")
    public void test_TC15() throws IOException {
        // default-charset branch: charset is null and baseUri is file path
        File temp = File.createTempFile("jsoupTestBody", ".html");
        temp.deleteOnExit();
        String htmlContent = "<html><body><p>Hello</p></body></html>";
        Files.write(temp.toPath(), htmlContent.getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(temp);
        assertAll(
            () -> assertEquals("Hello", doc.body().text(), "Body text should match the content inside <p>") ,
            () -> assertEquals(temp.getAbsolutePath(), doc.baseUri(), "Base URI should default to file absolute path")
        );
    }

    @Test
    @DisplayName("parse(URL, timeout) with invalid protocol throws MalformedURLException")
    public void test_TC16() {
        // URL with unsupported protocol ftp to trigger MalformedURLException
        assertThrows(MalformedURLException.class, () -> {
            URL url = new URL("ftp://example.com");
            Jsoup.parse(url, 1000);
        });
    }

    @Test
    @DisplayName("parse(URL, timeout) with timeout exceeded throws SocketTimeoutException")
    public void test_TC17() {
        // Use a non-routable IP to force a connection timeout with minimal timeout value
        URL url;
        try {
            url = new URL("http://10.255.255.1");
        } catch (MalformedURLException e) {
            fail("Invalid test URL");
            return;
        }
        int timeoutMillis = 1; // very small to trigger timeout
        assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, timeoutMillis));
    }
}