package org.jsoup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

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
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("TC11: parse(InputStream, charsetName, baseUri) reads a UTF-8 stream and returns a Document with correct body and baseUri")
    void test_TC11() throws IOException {
        // using a non-empty UTF-8 InputStream triggers DataUtil.load branch for streams
        InputStream in = new ByteArrayInputStream("<p>Hi</p>".getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://base/";
        Document doc = Jsoup.parse(in, charset, baseUri);
        assertAll(
            () -> assertEquals("<p>Hi</p>", doc.body().html()),
            () -> assertEquals("http://base/", doc.baseUri())
        );
    }

    @Test
    @DisplayName("TC12: parse(InputStream, charsetName, baseUri, parser) uses XML parser and returns an empty body for empty input")
    void test_TC12() throws IOException {
        // empty InputStream ensures parser.parseInput branch with xml parser, no elements produced
        InputStream in = new ByteArrayInputStream(new byte[0]);
        String charset = null;
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(in, charset, baseUri, parser);
        assertAll(
            () -> assertEquals("", doc.body().html()),
            () -> assertEquals("", doc.baseUri())
        );
    }

    @Test
    @DisplayName("TC13: parse(File, charsetName, baseUri) loads a temporary HTML file and returns a Document with resolved baseUri")
    void test_TC13() throws IOException {
        // existing temp file with content invokes DataUtil.load(File, charset, baseUri)
        Path tempPath = Files.createTempFile("jsoup", ".html");
        File file = tempPath.toFile();
        Files.write(tempPath, "<h1>Title</h1>".getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://fbase/";
        Document doc = Jsoup.parse(file, charset, baseUri);
        assertAll(
            () -> assertEquals("<h1>Title</h1>", doc.body().html()),
            () -> assertEquals("http://fbase/", doc.baseUri())
        );
        file.delete();
    }

    @Test
    @DisplayName("TC14: parse(File, charsetName) uses file absolute path as baseUri when charsetName is null")
    void test_TC14() throws IOException {
        // null charset triggers overload that uses file absolute path as baseUri
        Path tempPath = Files.createTempFile("jsoup", ".html");
        File file = tempPath.toFile();
        Files.write(tempPath, "<i>Italics</i>".getBytes(StandardCharsets.UTF_8));
        String charset = null;
        Document doc = Jsoup.parse(file, charset);
        assertAll(
            () -> assertEquals("<i>Italics</i>", doc.body().html()),
            () -> assertEquals(file.getAbsolutePath(), doc.baseUri())
        );
        file.delete();
    }

    @Test
    @DisplayName("TC15: parse(File) determines charset automatically and uses file absolute path as baseUri")
    void test_TC15() throws IOException {
        // no charset argument uses overload with automatic charset detection and file baseUri
        Path tempPath = Files.createTempFile("jsoup", ".html");
        File file = tempPath.toFile();
        Files.write(tempPath, "<b>Bold</b>".getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(file);
        assertAll(
            () -> assertEquals("<b>Bold</b>", doc.body().html()),
            () -> assertEquals(file.getAbsolutePath(), doc.baseUri())
        );
        file.delete();
    }

    @Test
    @DisplayName("TC16: parse(File, charsetName, baseUri, parser) uses XML parser to preserve unbalanced tags from file")
    void test_TC16() throws IOException {
        // xml parser preserves unbalanced tags, wrapping them properly
        Path tempPath = Files.createTempFile("jsoup", ".xml");
        File file = tempPath.toFile();
        Files.write(tempPath, "<tag>xml".getBytes(StandardCharsets.UTF_8));
        Parser parser = Parser.xmlParser();
        String baseUri = "baseX";
        Document doc = Jsoup.parse(file, null, baseUri, parser);
        assertAll(
            () -> assertEquals("<tag>xml</tag>", doc.body().html()),
            () -> assertEquals("baseX", doc.baseUri())
        );
        file.delete();
    }

    @Test
    @DisplayName("TC17: parse(Path, charsetName, baseUri) loads a Path and returns Document with correct baseUri")
    void test_TC17() throws IOException {
        // Path-based overload with explicit charset and baseUri
        Path path = Files.createTempFile("jsoup", ".html");
        Files.write(path, "<em>Em</em>".getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://pbase/";
        Document doc = Jsoup.parse(path, charset, baseUri);
        assertAll(
            () -> assertEquals("<em>Em</em>", doc.body().html()),
            () -> assertEquals("http://pbase/", doc.baseUri())
        );
        path.toFile().delete();
    }

    @Test
    @DisplayName("TC18: parse(Path, charsetName) uses path.toAbsolutePath() as baseUri when charsetName is null")
    void test_TC18() throws IOException {
        // null charset triggers Path overload using toAbsolutePath().toString() as baseUri
        Path path = Files.createTempFile("jsoup", ".html");
        Files.write(path, "<u>Under</u>".getBytes(StandardCharsets.UTF_8));
        String charset = null;
        Document doc = Jsoup.parse(path, charset);
        assertAll(
            () -> assertEquals("<u>Under</u>", doc.body().html()),
            () -> assertEquals(path.toAbsolutePath().toString(), doc.baseUri())
        );
        path.toFile().delete();
    }

    @Test
    @DisplayName("TC19: parse(Path) auto-detects charset and uses path.toAbsolutePath() as baseUri")
    void test_TC19() throws IOException {
        // no charset argument uses Path overload with automatic charset detection and path baseUri
        Path path = Files.createTempFile("jsoup", ".html");
        Files.write(path, "<s>Strike</s>".getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(path);
        assertAll(
            () -> assertEquals("<s>Strike</s>", doc.body().html()),
            () -> assertEquals(path.toAbsolutePath().toString(), doc.baseUri())
        );
        path.toFile().delete();
    }

    @Test
    @DisplayName("TC20: parse(File non-existent, charsetName, baseUri) throws IOException for missing file")
    void test_TC20() {
        // missing file triggers IOException in DataUtil.load
        File file = new File("no-such-file.html");
        String charset = "UTF-8";
        String baseUri = "http://x/";
        assertThrows(IOException.class, () -> Jsoup.parse(file, charset, baseUri));
    }

    @Test
    @DisplayName("TC21: parse((InputStream)null, charsetName, baseUri) throws NullPointerException before load")
    void test_TC21() {
        // null InputStream should immediately cause NullPointerException
        InputStream in = null;
        String charset = "UTF-8";
        String baseUri = "";
        assertThrows(NullPointerException.class, () -> Jsoup.parse(in, charset, baseUri));
    }

    @Test
    @DisplayName("TC22: parse(URL with non-http protocol, timeout) throws MalformedURLException for invalid URL protocol")
    void test_TC22() throws Exception {
        // URL with ftp protocol should cause HttpConnection.connect(url) to throw MalformedURLException
        URL url = new URL("ftp://example.com/");
        int timeout = 1000;
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, timeout));
    }
}