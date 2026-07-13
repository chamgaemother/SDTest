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
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("TC10: parse(File, charsetName) with non-null charsetName reads plain HTML file")
    public void test_TC10() throws IOException {
        File file = File.createTempFile("tc10", ".html");
        file.deleteOnExit();
        String html = "<h1>hi</h1>";
        Files.write(file.toPath(), html.getBytes("UTF-8"));

        Document doc = Jsoup.parse(file, "UTF-8");

        assertEquals(file.getAbsolutePath(), doc.baseUri());
        assertEquals(html, doc.body().html());
    }

    @Test
    @DisplayName("TC11: parse(File) without charsetName uses null branch and reads plain HTML file")
    public void test_TC11() throws IOException {
        File file = File.createTempFile("tc11", ".html");
        file.deleteOnExit();
        String html = "<div>test</div>";
        Files.write(file.toPath(), html.getBytes("UTF-8"));

        Document doc = Jsoup.parse(file);

        assertEquals(file.getAbsolutePath(), doc.baseUri());
        assertEquals(html, doc.body().html());
    }

    @Test
    @DisplayName("TC12: parse(File, charsetName, baseUri, parser) uses custom parser branch")
    public void test_TC12() throws IOException {
        File xmlFile = File.createTempFile("tc12", ".xml");
        xmlFile.deleteOnExit();
        String xml = "<root><a/></root>";
        Files.write(xmlFile.toPath(), xml.getBytes("UTF-8"));

        Parser xmlParser = Parser.xmlParser();
        String baseUri = "http://x/";

        Document doc = Jsoup.parse(xmlFile, "UTF-8", baseUri, xmlParser);

        assertEquals(baseUri, doc.baseUri());
        assertEquals(1, doc.select("a").size());
    }

    @Test
    @DisplayName("TC13: parse(Path, charsetName, baseUri) reads file via Path overload")
    public void test_TC13() throws IOException {
        Path p = Files.createTempFile("tc13", ".html");
        p.toFile().deleteOnExit();
        String html = "<span>ok</span>";
        Files.write(p, html.getBytes("UTF-8"));

        String baseUri = "https://u/";

        Document doc = Jsoup.parse(p, "UTF-8", baseUri);

        assertEquals(baseUri, doc.baseUri());
        assertEquals(html, doc.body().html());
    }

    @Test
    @DisplayName("TC14: parse(Path, charsetName) uses two-arg Path overload, charsetName null branch")
    public void test_TC14() throws IOException {
        Path p = Files.createTempFile("tc14", ".html");
        p.toFile().deleteOnExit();
        String html = "<p>x</p>";
        Files.write(p, html.getBytes("UTF-8"));

        Document doc = Jsoup.parse(p, null);

        assertEquals(p.toAbsolutePath().toString(), doc.baseUri());
        assertEquals(html, doc.body().html());
    }

    @Test
    @DisplayName("TC15: parse(Path) one-arg Path overload uses null charset and path branch")
    public void test_TC15() throws IOException {
        Path p = Files.createTempFile("tc15", ".html");
        p.toFile().deleteOnExit();
        String html = "<em>e</em>";
        Files.write(p, html.getBytes("UTF-8"));

        Document doc = Jsoup.parse(p);

        assertEquals(p.toAbsolutePath().toString(), doc.baseUri());
        assertEquals(html, doc.body().html());
    }

    @Test
    @DisplayName("TC16: parse(Path, charsetName, baseUri, parser) uses Path+parser overload")
    public void test_TC16() throws IOException {
        Path xml = Files.createTempFile("tc16", ".xml");
        xml.toFile().deleteOnExit();
        String xmlContent = "<root><b/></root>";
        Files.write(xml, xmlContent.getBytes("UTF-8"));

        Parser xmlParser = Parser.xmlParser();
        String baseUri = "http://y/";

        Document doc = Jsoup.parse(xml, "UTF-8", baseUri, xmlParser);

        assertEquals(baseUri, doc.baseUri());
        assertEquals(1, doc.select("b").size());
    }

    @Test
    @DisplayName("TC17: parse(InputStream, charsetName, baseUri) reads from InputStream branch")
    public void test_TC17() throws IOException {
        String html = "<b>stream</b>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        String baseUri = "https://s/";

        Document doc = Jsoup.parse(in, "UTF-8", baseUri);

        assertEquals(baseUri, doc.baseUri());
        assertEquals(html, doc.body().html());
    }

    @Test
    @DisplayName("TC18: parse(InputStream, charsetName, baseUri, parser) uses parser overload on stream")
    public void test_TC18() throws IOException {
        String xml = "<root><c/></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        Parser xmlParser = Parser.xmlParser();
        String baseUri = "http://z/";

        Document doc = Jsoup.parse(in, null, baseUri, xmlParser);

        assertEquals(baseUri, doc.baseUri());
        assertEquals(1, doc.select("c").size());
    }

    @Test
    @DisplayName("TC19: parseBodyFragment(html, baseUri) with non-empty baseUri returns fragment in body")
    public void test_TC19() {
        String frag = "<i>i</i>";
        String baseUri = "https://f/";

        Document doc = Jsoup.parseBodyFragment(frag, baseUri);

        assertEquals(baseUri, doc.baseUri());
        assertEquals(frag, doc.body().html());
    }

    @Test
    @DisplayName("TC20: parseBodyFragment(html) with empty baseUri returns fragment in body")
    public void test_TC20() {
        String frag = "<strong>s</strong>";

        Document doc = Jsoup.parseBodyFragment(frag);

        assertEquals("", doc.baseUri());
        assertEquals(frag, doc.body().html());
    }
}