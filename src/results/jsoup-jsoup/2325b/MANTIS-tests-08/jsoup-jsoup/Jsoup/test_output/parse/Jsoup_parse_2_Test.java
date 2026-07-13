package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC14: parse(File, null charsetName, baseUri) reads uncompressed HTML file and returns Document")
    public void test_TC14() throws IOException {
        // Given a temp file with simple HTML and null charset triggers default UTF-8 path (B3→B4)
        Path tmp = Files.createTempFile("tc14", ".html");
        tmp.toFile().deleteOnExit();
        String fragment = "<p>Hello</p>";
        Files.write(tmp, fragment.getBytes(StandardCharsets.UTF_8));
        File file = tmp.toFile();
        String charset = null;
        String baseUri = "http://base/";

        // When
        Document doc = Jsoup.parse(file, charset, baseUri);

        // Then
        assertEquals(fragment, doc.body().html());
    }

    @Test
    @DisplayName("TC15: parse(File, invalid charsetName) throws IOException for unsupported charset")
    public void test_TC15() {
        // Given bad charset triggers load error path (B5)
        Path tmp;
        try {
            tmp = Files.createTempFile("tc15", ".html");
            tmp.toFile().deleteOnExit();
            Files.write(tmp, "<p>X</p>".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            fail("Setup failed: " + e.getMessage());
            return;
        }
        File file = tmp.toFile();
        String charset = "BAD-CHARSET";
        String baseUri = "";

        // Then
        assertThrows(IOException.class,
                () -> Jsoup.parse(file, charset, baseUri));
    }

    @Test
    @DisplayName("TC16: parse(File) uses default null charset and file absolute path as baseUri")
    public void test_TC16() throws IOException {
        // Given only file triggers overload default baseUri and null charset (delegates to parse(file, null, absPath))
        Path tmp = Files.createTempFile("tc16", ".html");
        tmp.toFile().deleteOnExit();
        String fragment = "<div>OK</div>";
        Files.write(tmp, fragment.getBytes(StandardCharsets.UTF_8));
        File file = tmp.toFile();

        // When
        Document doc = Jsoup.parse(file);

        // Then
        assertEquals(fragment, doc.body().html());
    }

    @Test
    @DisplayName("TC17: parse(File, charsetName, baseUri, xmlParser) uses custom Parser to load file")
    public void test_TC17() throws IOException {
        // Given file with XML content and xmlParser triggers DataUtil.load with parser branch (B6)
        Path tmp = Files.createTempFile("tc17", ".xml");
        tmp.toFile().deleteOnExit();
        String xml = "<root><a/></root>";
        Files.write(tmp, xml.getBytes(StandardCharsets.UTF_8));
        File file = tmp.toFile();
        String charset = null;
        String baseUri = "";
        Parser parser = Parser.xmlParser();

        // When
        Document doc = Jsoup.parse(file, charset, baseUri, parser);

        // Then xmlParser should keep element <a>
        assertEquals(1, doc.select("a").size());
    }

    @Test
    @DisplayName("TC18: parse(Path, charsetName, baseUri) reads HTML via Path and returns Document")
    public void test_TC18() throws IOException {
        // Given path + null charset + baseUri triggers DataUtil.load(Path, charset, baseUri) (B7)
        Path path = Files.createTempFile("tc18", ".html");
        path.toFile().deleteOnExit();
        String span = "<span>Hi</span>";
        Files.write(path, span.getBytes(StandardCharsets.UTF_8));
        String charset = null;
        String baseUri = "http://x/";

        // When
        Document doc = Jsoup.parse(path, charset, baseUri);

        // Then
        assertEquals(span, doc.body().html());
    }

    @Test
    @DisplayName("TC19: parse(Path, charsetName) uses Path absolute path as baseUri and default charset null")
    public void test_TC19() throws IOException {
        // Given path + charsetName triggers DataUtil.load(path, charset, absPath) (B8)
        Path path = Files.createTempFile("tc19", ".html");
        path.toFile().deleteOnExit();
        String bold = "<b>Bold</b>";
        Files.write(path, bold.getBytes(StandardCharsets.UTF_8));
        String charset = null;

        // When
        Document doc = Jsoup.parse(path, charset);

        // Then
        assertEquals(bold, doc.body().html());
    }

    @Test
    @DisplayName("TC20: parse(Path) uses null charset and Path absolute path as baseUri")
    public void test_TC20() throws IOException {
        // Given only path triggers DataUtil.load(path, null, absPath) branch (B8)
        Path path = Files.createTempFile("tc20", ".html");
        path.toFile().deleteOnExit();
        String italic = "<i>Italic</i>";
        Files.write(path, italic.getBytes(StandardCharsets.UTF_8));

        // When
        Document doc = Jsoup.parse(path);

        // Then
        assertEquals(italic, doc.body().html());
    }

    @Test
    @DisplayName("TC21: parse(Path, charsetName, baseUri, parser) uses custom Parser on Path")
    public void test_TC21() throws IOException {
        // Given XML file path + xmlParser triggers DataUtil.load with parser on Path (B9)
        Path path = Files.createTempFile("tc21", ".xml");
        path.toFile().deleteOnExit();
        String xml = "<root><x/></root>";
        Files.write(path, xml.getBytes(StandardCharsets.UTF_8));
        String charset = null;
        String baseUri = "";
        Parser parser = Parser.xmlParser();

        // When
        Document doc = Jsoup.parse(path, charset, baseUri, parser);

        // Then parser should preserve <x>
        assertEquals(1, doc.select("x").size());
    }

    @Test
    @DisplayName("TC22: parse(InputStream, charsetName, baseUri) reads from stream and returns Document")
    public void test_TC22() throws IOException {
        // Given stream + charset + baseUri triggers DataUtil.load(InputStream, charset, baseUri) (B10)
        String html = "<ul><li>1</li></ul>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://u/";

        // When
        Document doc = Jsoup.parse(in, charset, baseUri);

        // Then
        assertEquals(html, doc.body().html());
    }

    @Test
    @DisplayName("TC23: parse(InputStream, charsetName, baseUri, parser) uses custom Parser on stream")
    public void test_TC23() throws IOException {
        // Given XML input stream + xmlParser triggers DataUtil.load with parser on InputStream (B11)
        String xml = "<root><y/></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "";
        Parser parser = Parser.xmlParser();

        // When
        Document doc = Jsoup.parse(in, charset, baseUri, parser);

        // Then parser should yield <y>
        assertEquals(1, doc.select("y").size());
    }

    @Test
    @DisplayName("TC24: parse(URL, timeout) with non-HTTP URL throws MalformedURLException")
    public void test_TC24() {
        // Given unsupported scheme ftp triggers HttpConnection.connect(url) MalformedURLException (B12)
        URL url;
        try {
            url = new URL("ftp://example.com");
        } catch (MalformedURLException e) {
            fail("URL setup failed");
            return;
        }
        int timeout = 1000;

        // Then
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, timeout));
    }
}