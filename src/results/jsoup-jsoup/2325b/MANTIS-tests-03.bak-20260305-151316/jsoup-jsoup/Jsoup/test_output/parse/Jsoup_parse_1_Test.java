package org.jsoup;

import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(File, String charsetName) with non-null charset uses provided charset and file path as baseUri")
    public void test_TC11() throws IOException {
        // Create temp file with HTML content to drive file-parsing branch
        File f = Files.createTempFile("jsoup-test", ".html").toFile();
        f.deleteOnExit();
        String html = "<h1>Header</h1>";
        Files.write(f.toPath(), html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";

        // Exercise parse(File, String)
        Document doc = Jsoup.parse(f, charset);

        // Verify baseUri and body html from expectedResult
        assertEquals(f.getAbsolutePath(), doc.baseUri());
        assertEquals(html, doc.body().html());
    }

    @Test
    @DisplayName("parse(File, String charsetName, String baseUri, Parser) with xmlParser produces xml syntax output")
    public void test_TC12() throws IOException {
        // Prepare temp file with XML-like content to trigger Parser.xmlParser overload
        File f = Files.createTempFile("jsoup-xml", ".xml").toFile();
        f.deleteOnExit();
        String content = "<item/><child>text</child>";
        Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
        String charset = null; // auto-detect branch
        String base = "http://base/";
        Parser xmlParser = Parser.xmlParser();

        // Call parse with parser
        Document doc = Jsoup.parse(f, charset, base, xmlParser);

        // Assert baseUri and xml syntax setting
        assertEquals(base, doc.baseUri());
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    @Test
    @DisplayName("parse(Path, String charsetName, String baseUri) with non-null charset reads file via Path")
    public void test_TC13() throws IOException {
        // Use Path-based overload with non-null charset to cover Path->DataUtil
        Path temp = Files.createTempDirectory("jsoup-path");
        Path file = temp.resolve("test.html");
        String html = "<div>PathTest</div>";
        Files.write(file, html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String base = "https://example.org/";

        Document doc = Jsoup.parse(file, charset, base);

        assertEquals(base, doc.baseUri());
        assertEquals(html, doc.body().html());
    }

    @Test
    @DisplayName("parse(Path, String charsetName) with null charsetName falls back to default and uses path as baseUri")
    public void test_TC14() throws IOException {
        // Path overload without charset to take default branch
        Path temp = Files.createTempDirectory("jsoup-path-default");
        Path file = temp.resolve("d.html");
        String html = "<span>Default</span>";
        Files.write(file, html.getBytes(StandardCharsets.UTF_8));

        Document doc = Jsoup.parse(file);

        assertEquals(file.toAbsolutePath().toString(), doc.baseUri());
        assertEquals(html, doc.body().html());
    }

    @Test
    @DisplayName("parse(Path, String charsetName, String baseUri, Parser) using xmlParser on a Path source")
    public void test_TC15() throws IOException {
        // Path+Parser branch with xmlParser
        Path temp = Files.createTempDirectory("jsoup-path-xml");
        Path file = temp.resolve("x.xml");
        String xml = "<x attr=\"v\"/>";
        Files.write(file, xml.getBytes(StandardCharsets.UTF_8));
        Parser xmlParser = Parser.xmlParser();
        String base = "http://site/";

        Document doc = Jsoup.parse(file, "UTF-8", base, xmlParser);

        assertEquals(base, doc.baseUri());
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    @Test
    @DisplayName("parse(InputStream, String charsetName, String baseUri, Parser) applies parser and closes stream")
    public void test_TC16() throws IOException {
        // InputStream+Parser branch; stream should be closed
        byte[] bytes = "<p>Stream</p>".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        Parser htmlParser = Parser.htmlParser();
        String base = "base://";

        Document doc = Jsoup.parse(in, "UTF-8", base, htmlParser);
        // After parse, stream closed: reading should throw IOException
        assertEquals(base, doc.baseUri());
        assertEquals("<p>Stream</p>", doc.body().html());
        assertThrows(IOException.class, () -> in.read(new byte[1]));
    }

    @Test
    @DisplayName("parseBodyFragment(String, String) with nested tags sets baseUri and body fragment")
    public void test_TC17() {
        // Body fragment with baseUri branch
        String frag = "<div><a href=\"b.html\">link</a></div>";
        String base = "http://x/";

        Document doc = Jsoup.parseBodyFragment(frag, base);

        assertEquals(base, doc.baseUri());
        assertEquals(frag, doc.body().html());
    }

    @Test
    @DisplayName("parseBodyFragment(String) with no baseUri uses empty baseUri")
    public void test_TC18() {
        // Fragment default-baseUri branch
        String frag = "<span>F</span>";

        Document doc = Jsoup.parseBodyFragment(frag);

        assertEquals("", doc.baseUri());
        assertEquals(frag, doc.body().html());
    }

    @Test
    @DisplayName("parse(URL, timeout) throws SocketTimeoutException when connection times out")
    public void test_TC19() throws Exception {
        // Mock HttpConnection.connect to throw on get() to simulate timeout
        URL u = new URL("http://timeout.test");
        Connection mockCon = mock(Connection.class);
        when(mockCon.timeout(50)).thenReturn(mockCon);
        when(mockCon.get()).thenThrow(new SocketTimeoutException());

        try (MockedStatic<HttpConnection> mocked = Mockito.mockStatic(HttpConnection.class)) {
            mocked.when(() -> HttpConnection.connect(u)).thenReturn(mockCon);

            // Expect SocketTimeoutException from parse(URL, timeout)
            assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(u, 50));
        }
    }
}