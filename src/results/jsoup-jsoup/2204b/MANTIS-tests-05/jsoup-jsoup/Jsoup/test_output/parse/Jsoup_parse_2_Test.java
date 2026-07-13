package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(String html, String baseUri, Parser parser) with valid HTML and HTML parser returns Document with correct syntax and baseUri")
    public void test_TC15() {
        // Using htmlParser to ensure the html syntax branch is taken (B7→B8)
        String html = "<div id='x'>X</div>";
        String baseUri = "http://test/";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        // oracle: html syntax, preserved baseUri, element found
        assertEquals("html", doc.outputSettings().syntax(), "Expected html syntax via htmlParser");
        assertEquals(baseUri, doc.baseUri(), "Expected baseUri preserved");
        assertNotNull(doc.select("div#x").first(), "Expected div#x element present");
    }

    @Test
    @DisplayName("parse(InputStream in, String charsetName, String baseUri) with valid UTF-8 stream returns Document with correct text and baseUri")
    public void test_TC16() throws IOException {
        // Byte stream + charset branch selects DataUtil.load(InputStream,..)
        byte[] bytes = "<p>Stream</p>".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(bytes);
        String charset = "UTF-8";
        String baseUri = "http://s/";
        Document doc = Jsoup.parse(in, charset, baseUri);
        // oracle: text content and baseUri preserved
        assertEquals("Stream", doc.text(), "Expected parsed text from stream");
        assertEquals(baseUri, doc.baseUri(), "Expected baseUri preserved from input");
    }

    @Test
    @DisplayName("parse(InputStream in, String charsetName, String baseUri, Parser parser) with valid XML stream returns Document with xml syntax")
    public void test_TC17() throws IOException {
        // Using xmlParser to force xml syntax branch
        byte[] bytes = "<n>1</n>".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(bytes);
        String charset = "UTF-8";
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(in, charset, baseUri, parser);
        // oracle: xml syntax and text content
        assertEquals("xml", doc.outputSettings().syntax(), "Expected xml syntax via xmlParser");
        assertEquals("1", doc.text(), "Expected text '1' from XML stream");
    }

    @Test
    @DisplayName("parse(File file, String charsetName, String baseUri) with valid HTML file returns Document with correct body")
    public void test_TC18() throws IOException {
        // File load with explicit charset and baseUri branch
        Path tmp = Files.createTempFile("file", ".html");
        Files.write(tmp, "<p>File</p>".getBytes("UTF-8"));
        File file = tmp.toFile();
        String charset = "UTF-8";
        String baseUri = "http://f/";
        Document doc = Jsoup.parse(file, charset, baseUri);
        // oracle: text 'File' and preserved baseUri
        assertEquals("File", doc.text(), "Expected text 'File' from file");
        assertEquals(baseUri, doc.baseUri(), "Expected provided baseUri");
    }

    @Test
    @DisplayName("parse(File file, String charsetName) with null charsetName uses file absolute path as baseUri")
    public void test_TC19() throws IOException {
        // Null charset triggers default detection and file.getAbsolutePath() as baseUri
        Path tmp = Files.createTempFile("f2", ".html");
        Files.write(tmp, "<b>X</b>".getBytes());
        File file = tmp.toFile();
        String charset = null;
        Document doc = Jsoup.parse(file, charset);
        // oracle: text 'X' and baseUri from file path
        assertEquals("X", doc.text(), "Expected text 'X' from file");
        assertEquals(file.getAbsolutePath(), doc.baseUri(), "Expected baseUri = file absolute path");
    }

    @Test
    @DisplayName("parse(File file) with default charset determines UTF-8 and uses file baseUri")
    public void test_TC20() throws IOException {
        // No-args file overload, charset null and baseUri = file path
        Path tmp = Files.createTempFile("f3", ".html");
        Files.write(tmp, "<i>I</i>".getBytes());
        File file = tmp.toFile();
        Document doc = Jsoup.parse(file);
        // oracle: text 'I' and baseUri = file absolute path
        assertEquals("I", doc.text(), "Expected text 'I' from default file parse");
        assertEquals(file.getAbsolutePath(), doc.baseUri(), "Expected baseUri file path");
    }

    @Test
    @DisplayName("parse(File file, String charsetName, String baseUri, Parser parser) with XML parser returns xml syntax")
    public void test_TC21() throws IOException {
        // File + parser overload to test XML branch
        Path tmp = Files.createTempFile("f4", ".html");
        Files.write(tmp, "<x>Y</x>".getBytes("UTF-8"));
        File file = tmp.toFile();
        String charset = "UTF-8";
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(file, charset, baseUri, parser);
        // oracle: xml syntax and text 'Y'
        assertEquals("xml", doc.outputSettings().syntax(), "Expected xml syntax");
        assertEquals("Y", doc.text(), "Expected text 'Y' from XML file");
    }

    @Test
    @DisplayName("parse(Path path, String charsetName, String baseUri) with valid HTML path returns correct Document")
    public void test_TC22() throws IOException {
        // Path+charset+baseUri overload
        Path path = Files.createTempFile("p", ".html");
        Files.write(path, "<u>U</u>".getBytes("UTF-8"));
        String charset = "UTF-8";
        String baseUri = "http://p/";
        Document doc = Jsoup.parse(path, charset, baseUri);
        // oracle: text 'U' and provided baseUri
        assertEquals("U", doc.text(), "Expected text 'U' from Path parse");
        assertEquals(baseUri, doc.baseUri(), "Expected provided baseUri");
    }

    @Test
    @DisplayName("parse(Path path, String charsetName) with null charsetName uses path.toAbsolutePath() as baseUri")
    public void test_TC23() throws IOException {
        // Null charset triggers default and path.toAbsolutePath() as baseUri
        Path path = Files.createTempFile("p2", ".html");
        Files.write(path, "<u>V</u>".getBytes());
        String charset = null;
        Document doc = Jsoup.parse(path, charset);
        // oracle: text 'V' and baseUri = absolute path
        assertEquals("V", doc.text(), "Expected text 'V' from Path parse");
        assertEquals(path.toAbsolutePath().toString(), doc.baseUri(), "Expected baseUri absolute path");
    }

    @Test
    @DisplayName("parse(Path path) with default charset uses path.toAbsolutePath() as baseUri")
    public void test_TC24() throws IOException {
        // No-args Path overload
        Path path = Files.createTempFile("p3", ".html");
        Files.write(path, "<u>W</u>".getBytes());
        Document doc = Jsoup.parse(path);
        // oracle: text 'W' and baseUri = absolute path
        assertEquals("W", doc.text(), "Expected text 'W' from default Path parse");
        assertEquals(path.toAbsolutePath().toString(), doc.baseUri(), "Expected baseUri absolute path");
    }

    @Test
    @DisplayName("parse(Path path, String charsetName, String baseUri, Parser parser) with XML parser returns xml syntax")
    public void test_TC25() throws IOException {
        // Path+parser overload branch for XML
        Path path = Files.createTempFile("p4", ".html");
        Files.write(path, "<z>Z</z>".getBytes("UTF-8"));
        String charset = "UTF-8";
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(path, charset, baseUri, parser);
        // oracle: xml syntax and text 'Z'
        assertEquals("xml", doc.outputSettings().syntax(), "Expected xml syntax");
        assertEquals("Z", doc.text(), "Expected text 'Z' from XML Path parse");
    }

    @Test
    @DisplayName("parseBodyFragment(String bodyHtml) with non-empty fragment returns Document with empty baseUri and fragment body")
    public void test_TC26() {
        // parseBodyFragment no-baseUri overload branch
        String frag = "<span>F</span>";
        Document doc = Jsoup.parseBodyFragment(frag);
        // oracle: fragment preserved and empty baseUri
        assertEquals(frag, doc.body().html(), "Expected body HTML equals fragment");
        assertEquals("", doc.baseUri(), "Expected empty baseUri for no-baseUri overload");
    }
}