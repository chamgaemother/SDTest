package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("TC10: parse(String html) delegates to Parser.parse(html, \"\") and returns Document with empty baseUri")
    public void test_TC10() {
        // GIVEN
        String html = "<span>abc</span>";
        // WHEN
        Document doc = Jsoup.parse(html);
        // THEN
        // baseUri should be empty because this overload calls Parser.parse(html, "")
        assertEquals("", doc.baseUri());
        // body should contain the fragment
        assertTrue(doc.body().html().contains("<span>abc</span>"));
    }

    @Test
    @DisplayName("TC11: parse(File file, String charsetName, String baseUri, Parser parser) uses provided parser for file input")
    public void test_TC11() throws Exception {
        // GIVEN: create temp XML-like file
        Path tempPath = Files.createTempFile("jsoup_test_tc11", ".xml");
        Files.write(tempPath, "<item/>".getBytes());
        File tempFile = tempPath.toFile();
        String charset = "UTF-8";
        String baseUri = "http://file/";
        Parser xmlParser = Parser.xmlParser();
        // WHEN
        Document doc = Jsoup.parse(tempFile, charset, baseUri, xmlParser);
        // THEN
        // baseUri should match the provided URI
        assertEquals(baseUri, doc.baseUri());
        // xmlParser should parse <item/> into an element
        assertEquals(1, doc.select("item").size());
        // cleanup
        tempFile.delete();
    }

    @Test
    @DisplayName("TC12: parse(Path path, String charsetName, String baseUri) loads from Path with explicit charset and baseUri")
    public void test_TC12() throws Exception {
        // GIVEN: create temp HTML file
        Path path = Files.createTempFile("jsoup_test_tc12", ".html");
        Files.write(path, "<p>path</p>".getBytes());
        String charset = null;
        String baseUri = "http://path/";
        // WHEN
        Document doc = Jsoup.parse(path, charset, baseUri);
        // THEN
        // baseUri should be exactly the provided baseUri
        assertEquals(baseUri, doc.baseUri());
        // html should contain our content
        assertTrue(doc.html().contains("<p>path</p>"));
        // cleanup
        Files.deleteIfExists(path);
    }

    @Test
    @DisplayName("TC13: parse(Path path, String charsetName) loads from Path with charsetName and default baseUri = toAbsolutePath")
    public void test_TC13() throws Exception {
        // GIVEN: create temp HTML file
        Path path = Files.createTempFile("jsoup_test_tc13", ".html");
        Files.write(path, "<div>z</div>".getBytes());
        String charset = "UTF-8";
        // WHEN
        Document doc = Jsoup.parse(path, charset);
        // THEN
        // baseUri should be the file's absolute path string
        assertEquals(path.toAbsolutePath().toString(), doc.baseUri());
        // html should contain our content
        assertTrue(doc.html().contains("<div>z</div>"));
        // cleanup
        Files.deleteIfExists(path);
    }

    @Test
    @DisplayName("TC14: parse(Path path) loads from Path with null charset and baseUri = toAbsolutePath")
    public void test_TC14() throws Exception {
        // GIVEN: create temp HTML file
        Path path = Files.createTempFile("jsoup_test_tc14", ".html");
        Files.write(path, "<h1>t</h1>".getBytes());
        // WHEN
        Document doc = Jsoup.parse(path);
        // THEN
        // baseUri should be the file's absolute path string
        assertEquals(path.toAbsolutePath().toString(), doc.baseUri());
        // html should contain our content
        assertTrue(doc.html().contains("<h1>t</h1>"));
        // cleanup
        Files.deleteIfExists(path);
    }

    @Test
    @DisplayName("TC15: parse(InputStream in, String charsetName, String baseUri) reads stream and returns Document")
    public void test_TC15() throws Exception {
        // GIVEN: ByteArrayInputStream with HTML
        ByteArrayInputStream in = new ByteArrayInputStream("<p>S</p>".getBytes());
        String charset = "UTF-8";
        String baseUri = "http://stream/";
        // WHEN
        Document doc = Jsoup.parse(in, charset, baseUri);
        // THEN
        // baseUri should match provided baseUri
        assertEquals(baseUri, doc.baseUri());
        // html should contain our fragment
        assertTrue(doc.html().contains("<p>S</p>"));
    }

    @Test
    @DisplayName("TC16: parse(InputStream in, String charsetName, String baseUri, Parser parser) reads stream with parser")
    public void test_TC16() throws Exception {
        // GIVEN: ByteArrayInputStream with XML fragment
        ByteArrayInputStream in = new ByteArrayInputStream("<n/>".getBytes());
        String charset = null;
        String baseUri = "";
        Parser xmlParser = Parser.xmlParser();
        // WHEN
        Document doc = Jsoup.parse(in, charset, baseUri, xmlParser);
        // THEN
        // baseUri blank as provided
        assertEquals("", doc.baseUri());
        // xmlParser should parse <n/> into an element
        assertEquals(1, doc.select("n").size());
    }

    @Test
    @DisplayName("TC17: parse(File file, String charsetName, String baseUri) with invalid charsetName throws IOException")
    public void test_TC17() throws Exception {
        // GIVEN: create temp HTML file
        Path tempPath = Files.createTempFile("jsoup_test_tc17", ".html");
        Files.write(tempPath, "<p>bad</p>".getBytes());
        File tempFile = tempPath.toFile();
        String badCharset = "INVALID-CHARSET";
        String baseUri = "http://bad/";
        // WHEN / THEN
        assertThrows(IOException.class, () -> Jsoup.parse(tempFile, badCharset, baseUri));
        // cleanup
        tempFile.delete();
    }

    @Test
    @DisplayName("TC18: parse(InputStream in, String charsetName, String baseUri) with invalid charsetName throws IOException")
    public void test_TC18() {
        // GIVEN: ByteArrayInputStream with HTML
        ByteArrayInputStream in = new ByteArrayInputStream("<p>X</p>".getBytes());
        String badCharset = "BAD";
        String baseUri = "";
        // WHEN / THEN
        assertThrows(IOException.class, () -> Jsoup.parse(in, badCharset, baseUri));
    }
}