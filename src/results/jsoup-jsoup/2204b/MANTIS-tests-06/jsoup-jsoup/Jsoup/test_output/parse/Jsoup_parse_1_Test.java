package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(File,charsetName,baseUri) with real HTML file returns parsed Document")
    public void test_TC10() throws IOException {
        // Creates a temp HTML file with a <h1> element to satisfy successful file load branch
        File file = File.createTempFile("tc10", ".html");
        file.deleteOnExit();
        String html = "<h1>Hi</h1>";
        Files.write(file.toPath(), html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://base/";
        // Exercise parse(File,charsetName,baseUri) success path
        Document doc = Jsoup.parse(file, charset, baseUri);
        // Verify parsed content and that baseUri is applied
        assertEquals("Hi", doc.select("h1").text());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(File,charsetName) with invalid charsetName throws IOException")
    public void test_TC11() {
        // Temp HTML file exists to reach charset resolution path and trigger error
        final File file;
        try {
            file = File.createTempFile("tc11", ".html");
            file.deleteOnExit();
            Files.write(file.toPath(), "<p>Test</p>".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            fail("Setup failed: " + e.getMessage());
            return; // Ensure the method exits after failure
        }
        String invalidCharset = "NO_SUCH_CHARSET";
        // Expect IOException due to unsupported charsetName
        assertThrows(IOException.class, () -> Jsoup.parse(file, invalidCharset));
    }

    @Test
    @DisplayName("parse(File) uses BOM/UTF-8 default when no charset and returns Document")
    public void test_TC12() throws IOException {
        // Creates a temp HTML file without BOM to test default-charset branch
        File file = File.createTempFile("tc12", ".html");
        file.deleteOnExit();
        String html = "<div>Body</div>";
        Files.write(file.toPath(), html.getBytes(StandardCharsets.UTF_8));
        // Exercise parse(File) default charset and baseUri=file path
        Document doc = Jsoup.parse(file);
        assertEquals("Body", doc.select("div").text());
        assertEquals(file.getAbsolutePath(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(File,charsetName,baseUri,parser) with xmlParser returns XML structure")
    public void test_TC13() throws IOException {
        // XML-like temp file to trigger custom parser path
        File file = File.createTempFile("tc13", ".xml");
        file.deleteOnExit();
        String xml = "<root><empty/></root>";
        Files.write(file.toPath(), xml.getBytes(StandardCharsets.UTF_8));
        Parser parser = Parser.xmlParser();
        // Exercise parse(File,charsetName,baseUri,parser)
        Document doc = Jsoup.parse(file, null, "base://", parser);
        // Verify that xmlParser retains empty-element syntax by selecting the empty tag
        assertEquals(1, doc.select("root > empty").size());
        assertEquals("base://", doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path,charsetName,baseUri) with valid Path returns Document")
    public void test_TC14() throws IOException {
        // Path to temp HTML file to cover Path-based load branch
        Path path = Files.createTempFile("tc14", ".html");
        path.toFile().deleteOnExit();
        String html = "<p>Path</p>";
        Files.write(path, html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://p/";
        Document doc = Jsoup.parse(path, charset, baseUri);
        assertEquals("Path", doc.select("p").text());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(InputStream,charsetName,baseUri) reads and parses stream correctly")
    public void test_TC15() throws IOException {
        // InputStream over HTML string to cover stream reader branch
        String html = "<span>foo</span>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://s/";
        Document doc = Jsoup.parse(in, charset, baseUri);
        assertEquals("foo", doc.select("span").text());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(InputStream,charsetName,baseUri,parser) with xmlParser preserves XML syntax")
    public void test_TC16() throws IOException {
        // InputStream over XML string triggers custom parser branch for streams
        String xml = "<r><e/></r>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Parser parser = Parser.xmlParser();
        // Use null charset to default inside method
        Document doc = Jsoup.parse(in, null, "", parser);
        // Verify xmlParser preserved self-closing 'e' tag
        assertEquals(1, doc.select("r > e").size());
    }

    @Test
    @DisplayName("parse(URL,timeout) with non-HTTP URL throws MalformedURLException")
    public void test_TC17() throws IOException {
        // Use FTP URL to exercise protocol validation path leading to MalformedURLException
        URL url = new URL("ftp://example.com");
        int timeout = 1000;
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, timeout));
    }
}