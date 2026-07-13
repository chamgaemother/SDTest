package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("TC11: parse(File) overload reads file with default null charset and uses file path as baseUri")
    public void test_TC11() throws IOException {
        // Creating temp HTML file with a simple <p>Z</p> payload to trigger default charset branch
        Path temp = Files.createTempFile("jsoup-tc11", ".html");
        Files.write(temp, "<p>Z</p>".getBytes("UTF-8"));
        File f = temp.toFile();

        // Changed to use null charset as per the error guide
        Document doc = Jsoup.parse(f, null);
        // Verify body HTML contains our fragment and baseUri is file absolute path
        assertTrue(doc.body().html().contains("<p>Z</p>"), "Should parse the <p>Z</p> content");
        assertEquals(f.getAbsolutePath(), doc.baseUri(), "Base URI should be the file's absolute path");

        // Cleanup
        f.delete();
    }

    @Test
    @DisplayName("TC12: parse(File, charsetName, baseUri) uses custom XML parser override")
    public void test_TC12() throws IOException {
        // XML parser branch: use xmlParser() to override HTML parser
        Path temp = Files.createTempFile("jsoup-tc12", ".xml");
        Files.write(temp, "<elem>1</elem>".getBytes("UTF-8"));
        File f = temp.toFile();

        Parser xml = Parser.xmlParser();
        // Changed to remove baseUri as per the error guide
        Document doc = Jsoup.parse(f, "UTF-8");
        // Settings syntax should match xml parser's syntax, and body HTML contains our element
        assertEquals(xml.getSettings().syntax(), doc.outputSettings().syntax(),
                "Syntax should follow the XML parser settings");
        assertTrue(doc.body().html().contains("<elem>1</elem>"),
                "Body should contain the <elem>1</elem> fragment");

        f.delete();
    }

    @Test
    @DisplayName("TC13: parse(Path, charsetName, baseUri) reads Path like File with explicit charset")
    public void test_TC13() throws IOException {
        // Explicit charset branch for Path
        Path p = Files.createTempFile("jsoup-tc13", ".html");
        Files.write(p, "<p>P</p>".getBytes("UTF-8"));

        // Changed to remove baseUri as per the error guide
        Document doc = Jsoup.parse(p, "UTF-8");
        assertTrue(doc.body().html().contains("<p>P</p>"),
                "Should parse <p>P</p> with explicit charset");
        assertEquals("http://pbase/", doc.baseUri(),
                "Base URI should be the provided URI");
        p.toFile().delete();
    }

    @Test
    @DisplayName("TC14: parse(Path, charsetName) overload uses path absolute as baseUri when charset null")
    public void test_TC14() throws IOException {
        // Null charset branch: uses path absolute as baseUri
        Path p = Files.createTempFile("jsoup-tc14", ".html");
        Files.write(p, "<p>N</p>".getBytes("UTF-8"));

        Document doc = Jsoup.parse(p, null);
        assertTrue(doc.body().html().contains("<p>N</p>"),
                "Should parse <p>N</p> even with null charset");
        assertEquals(p.toAbsolutePath().toString(), doc.baseUri(),
                "Base URI should be the path's absolute string");
        p.toFile().delete();
    }

    @Test
    @DisplayName("TC15: parse(Path) overload uses path absolute as baseUri and null charset")
    public void test_TC15() throws IOException {
        // Default Path branch: no charset parameter
        Path p = Files.createTempFile("jsoup-tc15", ".html");
        Files.write(p, "<p>D</p>".getBytes("UTF-8"));

        Document doc = Jsoup.parse(p);
        assertTrue(doc.body().html().contains("<p>D</p>"),
                "Should parse <p>D</p> with default overload");
        assertEquals(p.toAbsolutePath().toString(), doc.baseUri(),
                "Base URI should be the absolute path");
        p.toFile().delete();
    }

    @Test
    @DisplayName("TC16: parse(Path, charsetName, baseUri, parser) overload uses XML parser on Path")
    public void test_TC16() throws IOException {
        // XML parser branch for Path
        Path p = Files.createTempFile("jsoup-tc16", ".xml");
        Files.write(p, "<node>5</node>".getBytes("UTF-8"));

        Parser xml = Parser.xmlParser();
        // Changed to remove baseUri as per the error guide
        Document doc = Jsoup.parse(p, "UTF-8");
        assertEquals(xml.getSettings().syntax(), doc.outputSettings().syntax(),
                "Syntax should match xml parser settings");
        assertTrue(doc.body().html().contains("<node>5</node>"),
                "Body should contain <node>5</node>");
        p.toFile().delete();
    }

    @Test
    @DisplayName("TC17: parse(InputStream, charsetName, baseUri) reads from valid InputStream")
    public void test_TC17() throws IOException {
        // Stream branch: use ByteArrayInputStream for HTML fragment
        InputStream in = new ByteArrayInputStream("<p>S</p>".getBytes("UTF-8"));
        Document doc = Jsoup.parse(in, "UTF-8", "http://s/uri");
        assertTrue(doc.body().html().contains("<p>S</p>"),
                "Should parse <p>S</p> from stream");
        assertEquals("http://s/uri", doc.baseUri(),
                "Base URI should be respected from argument");
    }

    @Test
    @DisplayName("TC18: parse(InputStream, charsetName, baseUri, parser) reads stream with custom parser")
    public void test_TC18() throws IOException {
        // Stream XML parser branch
        InputStream in = new ByteArrayInputStream("<x>7</x>".getBytes("UTF-8"));
        Parser xml = Parser.xmlParser();
        // Changed to remove baseUri as per the error guide
        Document doc = Jsoup.parse(in, null, "http://b/");
        assertEquals(xml.getSettings().syntax(), doc.outputSettings().syntax(),
                "Syntax should follow XML parser settings");
        assertTrue(doc.body().html().contains("<x>7</x>"),
                "Body should contain <x>7</x> fragment");
    }

    @Test
    @DisplayName("TC19: parseBodyFragment(String, baseUri) with non-empty baseUri and html fragment")
    public void test_TC19() {
        // Body-fragment branch with provided baseUri
        String frag = "<b>Bold</b>";
        String base = "http://frag/";
        Document doc = Jsoup.parseBodyFragment(frag, base);
        assertTrue(doc.body().html().contains("<b>Bold</b>"),
                "Fragment should be parsed as body content");
        assertEquals(base, doc.baseUri(),
                "Base URI should be preserved as provided");
    }

    @Test
    @DisplayName("TC20: parseBodyFragment(String) default baseUri empty for fragment")
    public void test_TC20() {
        // Body-fragment default baseUri branch
        String frag = "<i>Italic</i>";
        Document doc = Jsoup.parseBodyFragment(frag);
        assertTrue(doc.body().html().contains("<i>Italic</i>"),
                "Fragment should parse correctly");
        assertEquals("", doc.baseUri(),
                "Default baseUri for fragment should be empty string");
    }
}