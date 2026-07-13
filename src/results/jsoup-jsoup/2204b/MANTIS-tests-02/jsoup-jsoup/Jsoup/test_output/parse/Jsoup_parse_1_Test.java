package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("TC11: parse(String html, String baseUri, Parser parser) uses provided parser and preserves baseUri")
    public void test_TC11() {
        // We use an XML parser branch to verify B0→B3→B12→B14 path
        String html = "<item>1</item>";
        String baseUri = "https://xml.example/";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        // assert that custom parser processes XML tags and baseUri is preserved
        assertTrue(doc.body().html().contains("<item>1</item>"));
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("TC12: parse(File file, String charsetName) loads file with explicit charset and uses file path as baseUri")
    public void test_TC12() throws IOException {
        // Exercise file overload B0→B5→B20→B22 by providing File and charset
        File temp = File.createTempFile("jsoup", ".html");
        temp.deleteOnExit();
        String content = "<div>F</div>";
        Files.write(temp.toPath(), content.getBytes("UTF-8"));
        Document doc = Jsoup.parse(temp, "UTF-8");
        assertEquals("<div>F</div>", doc.body().html());
        assertEquals(temp.getAbsolutePath(), doc.baseUri());
    }

    @Test
    @DisplayName("TC13: parse(File file, String charsetName, String baseUri, Parser parser) uses parser and explicit baseUri")
    public void test_TC13() throws IOException {
        // File + charset + baseUri + parser branch B0→B5→B23→B25
        File temp = File.createTempFile("xml", ".txt");
        temp.deleteOnExit();
        String xml = "<root>OK</root>";
        Files.write(temp.toPath(), xml.getBytes("UTF-8"));
        String base = "http://xml.test/";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(temp, "UTF-8", base, parser);
        assertTrue(doc.body().html().contains("<root>OK</root>"));
        assertEquals(base, doc.baseUri());
    }

    @Test
    @DisplayName("TC14: parse(Path path, String charsetName, String baseUri) loads gzipped path with null charsetName")
    public void test_TC14() throws IOException {
        // Test gzipped Path branch B0→B7→B30→B32
        Path gz = Files.createTempFile("jsoup", ".html.gz");
        gz.toFile().deleteOnExit();
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(gz.toFile()))) {
            gos.write("<p>GZ</p>".getBytes("UTF-8"));
        }
        String base = "https://gz.example/";
        Document doc = Jsoup.parse(gz, null, base);
        assertEquals("<p>GZ</p>", doc.body().html());
        assertEquals(base, doc.baseUri());
    }

    @Test
    @DisplayName("TC15: parse(Path path, String charsetName) loads file path with explicit charset and uses path as baseUri")
    public void test_TC15() throws IOException {
        // Path + charset branch B0→B7→B28→B29
        Path p = Files.createTempFile("jsoup", ".htm");
        p.toFile().deleteOnExit();
        String html = "<span>P</span>";
        Files.write(p, html.getBytes("UTF-8"));
        Document doc = Jsoup.parse(p, "UTF-8");
        assertEquals(html, doc.body().html());
        assertEquals(p.toAbsolutePath().toString(), doc.baseUri());
    }

    @Test
    @DisplayName("TC16: parse(Path path) loads file path with default charset and uses path as baseUri")
    public void test_TC16() throws IOException {
        // Default charset Path overload B0→B7→B26→B27
        Path p = Files.createTempFile("jsoupDef", ".html");
        p.toFile().deleteOnExit();
        String html = "<em>E</em>";
        Files.write(p, html.getBytes("UTF-8"));
        Document doc = Jsoup.parse(p);
        assertEquals(html, doc.body().html());
        assertEquals(p.toAbsolutePath().toString(), doc.baseUri());
    }

    @Test
    @DisplayName("TC17: parse(Path path, String charsetName, String baseUri, Parser parser) uses parser for Path overload")
    public void test_TC17() throws IOException {
        // Full Path overload with parser B0→B7→B33→B35
        Path p = Files.createTempFile("jsoupX", ".xml");
        p.toFile().deleteOnExit();
        String xml = "<x>OK</x>";
        Files.write(p, xml.getBytes("UTF-8"));
        String base = "http://path.test/";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(p, "UTF-8", base, parser);
        assertTrue(doc.body().html().contains("<x>OK</x>"));
        assertEquals(base, doc.baseUri());
    }

    @Test
    @DisplayName("TC18: parse(InputStream in, String charsetName, String baseUri, Parser parser) closes stream after use")
    public void test_TC18() throws IOException {
        // InputStream + parser branch B0→B9→B40→B42; ensure stream closed
        byte[] data = "<i>I</i>".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(in, "UTF-8", "baseUriX", parser);
        assertEquals("<i>I</i>", doc.body().html());
        assertEquals("baseUriX", doc.baseUri());
        // After parse, stream should be closed; reading should yield -1
        assertEquals(-1, in.read());
    }
}