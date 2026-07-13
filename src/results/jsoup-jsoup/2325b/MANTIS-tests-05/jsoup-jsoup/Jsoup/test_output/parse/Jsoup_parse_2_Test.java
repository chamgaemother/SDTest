package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.Connection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC18: parse(File, charsetName, baseUri, parser) invokes custom Parser.parseInput overload")
    public void test_TC18() throws Exception {
        // Create a temporary HTML file with known content
        File tmp = File.createTempFile("jsoup", ".html");
        tmp.deleteOnExit();
        String html = "<u>under</u>";
        Files.write(tmp.toPath(), html.getBytes("UTF-8"));
        String charset = "UTF-8";
        String baseUri = "http://base/";
        // Stub parser records inputs and returns its own document
        class StubParser extends Parser {
            final Document doc = new Document("stub");
            String inHtml, inBase;
            StubParser() { super(null); }
            @Override public Document parseInput(String htmlStr, String uri) {
                inHtml = htmlStr; inBase = uri; return doc;
            }
        }
        Parser parser = new StubParser();
        // When: call overload that should use parser.parseInput
        Document result = Jsoup.parse(tmp, charset, baseUri, parser);
        // Then: stub parser's doc is returned and inputs match file content and baseUri
        assertSame(((StubParser) parser).doc, result);
        assertEquals(html, ((StubParser) parser).inHtml);
        assertEquals(baseUri, ((StubParser) parser).inBase);
    }

    @Test
    @DisplayName("TC19: parse(Path, charsetName, baseUri, parser) invokes custom Parser.parseInput overload")
    public void test_TC19() throws Exception {
        // Create a temporary HTML path with content
        Path p = Files.createTempFile("jsoup", ".html");
        p.toFile().deleteOnExit();
        String html = "<em>it</em>";
        Files.write(p, html.getBytes("UTF-8"));
        String charset = null;  // null charset triggers meta/BOM detection path
        String baseUri = "http://uri/";
        class StubParser extends Parser {
            final Document doc = new Document("stub");
            String inHtml, inBase;
            StubParser() { super(null); }
            @Override public Document parseInput(String htmlStr, String uri) {
                inHtml = htmlStr; inBase = uri; return doc;
            }
        }
        Parser parser = new StubParser();
        // Use Path overload
        Document result = Jsoup.parse(p, charset, baseUri, parser);
        assertSame(((StubParser) parser).doc, result);
        assertEquals(html, ((StubParser) parser).inHtml);
        assertEquals(baseUri, ((StubParser) parser).inBase);
    }

    @Test
    @DisplayName("TC20: parse(InputStream, charsetName, baseUri, parser) invokes custom Parser.parseInput overload and closes stream")
    public void test_TC20() throws Exception {
        // Provide a ByteArrayInputStream that tracks closure
        byte[] data = "<i>one</i>".getBytes("UTF-8");
        class TrackStream extends ByteArrayInputStream {
            boolean closed = false;
            TrackStream(byte[] b) { super(b); }
            @Override public void close() throws IOException { super.close(); closed = true; }
        }
        TrackStream in = new TrackStream(data);
        String charset = null;
        String baseUri = "";  // empty baseUri path triggers default path
        class StubParser extends Parser {
            final Document doc = new Document("stub");
            String inHtml, inBase;
            StubParser() { super(null); }
            @Override public Document parseInput(String htmlStr, String uri) {
                inHtml = htmlStr; inBase = uri; return doc;
            }
        }
        Parser parser = new StubParser();
        // When: parse stream
        Document result = Jsoup.parse(in, charset, baseUri, parser);
        // Then: stub doc returned, stream closed, and HTML passed to parser
        assertSame(((StubParser) parser).doc, result);
        assertTrue(in.closed, "InputStream should be closed after parsing");
        assertEquals("<i>one</i>", ((StubParser) parser).inHtml);
    }

    @Test
    @DisplayName("TC21: parse(File) on a .gz compressed HTML file reads and parses content")
    public void test_TC21() throws Exception {
        // Create a gzipped temporary file containing <b>zip</b>
        File gz = File.createTempFile("test", ".html.gz");
        gz.deleteOnExit();
        String html = "<b>zip</b>";
        try (FileOutputStream fos = new FileOutputStream(gz);
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
            gzos.write(html.getBytes("UTF-8"));
        }
        // When: parse gzipped file
        Document doc = Jsoup.parse(gz);
        // Then: content inside <b> should be "zip" and baseUri is file path
        assertEquals("zip", doc.select("b").text());
        assertEquals(gz.getAbsolutePath(), doc.baseUri());
    }

    @Test
    @DisplayName("TC22: parse(File, invalid charsetName) throws IOException for unsupported charset")
    public void test_TC22() throws Exception {
        File tmp = File.createTempFile("jsoup", ".html");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "<p>x</p>".getBytes("UTF-8"));
        String invalidCharset = "INVALID-CHARSET";
        String baseUri = "";
        // Expect IOException when charset is invalid
        assertThrows(IOException.class, () -> Jsoup.parse(tmp, invalidCharset, baseUri));
    }

    @Test
    @DisplayName("TC23: parse(Path) on a .z compressed HTML file reads and parses content")
    public void test_TC23() throws Exception {
        // Create a gzipped .z file via Path
        Path p = Files.createTempFile("test", ".html.z");
        p.toFile().deleteOnExit();
        String html = "<span>z</span>";
        try (FileOutputStream fos = new FileOutputStream(p.toFile());
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
            gzos.write(html.getBytes("UTF-8"));
        }
        // When: parse compressed path
        Document doc = Jsoup.parse(p);
        // Then: span text is "z" and baseUri equals absolute path
        assertEquals("z", doc.select("span").text());
        assertEquals(p.toAbsolutePath().toString(), doc.baseUri());
    }
}