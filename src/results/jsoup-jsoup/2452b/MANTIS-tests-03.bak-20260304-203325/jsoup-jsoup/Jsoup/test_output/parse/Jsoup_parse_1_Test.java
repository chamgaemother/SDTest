package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(File, charsetName) with valid UTF-8 charset returns parsed Document and uses file absolute path as baseUri")
    public void test_TC11() throws IOException {
        // Create a temp HTML file with <h2>Head2</h2>
        Path tmp = Files.createTempFile("tc11", ".html");
        Files.write(tmp, "<h2>Head2</h2>".getBytes(StandardCharsets.UTF_8));
        File file = tmp.toFile();
        file.deleteOnExit();

        // Exercise the two-arg overload; ensures path->B3->B5 branch
        Document doc = Jsoup.parse(file, "UTF-8");

        // Verify the parsed content and that baseUri is the file absolute path
        assertEquals("Head2", doc.selectFirst("h2").text());
        assertEquals(file.getAbsolutePath(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri, Parser) with xmlParser returns XML-parsed Document and preserves provided baseUri")
    public void test_TC12() throws IOException {
        // Create a temp HTML file with <item>Val</item>
        Path tmp = Files.createTempFile("tc12", ".xml");
        Files.write(tmp, "<item>Val</item>".getBytes(StandardCharsets.UTF_8));
        File file = tmp.toFile();
        file.deleteOnExit();
        String baseUri = "http://mybase/";
        Parser parser = Parser.xmlParser();

        // Exercise the four-arg overload with xmlParser; ensures B4->B6 branch
        Document doc = Jsoup.parse(file, "UTF-8", baseUri, parser);

        // Verify the xml-parsed content and that baseUri is preserved
        assertEquals("Val", doc.selectFirst("item").text());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path, charsetName) two-arg overload uses path absolute string as baseUri")
    public void test_TC13() throws IOException {
        // Create a temp HTML path with <p>Path</p>
        Path path = Files.createTempFile("tc13", ".html");
        Files.write(path, "<p>Path</p>".getBytes(StandardCharsets.UTF_8));
        path.toFile().deleteOnExit();

        // Exercise the two-arg Path overload; ensures B3->B5 branch
        Document doc = Jsoup.parse(path, "UTF-8");

        // Verify the parsed content and that baseUri is path absolute string
        assertEquals("Path", doc.selectFirst("p").text());
        assertEquals(path.toAbsolutePath().toString(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path) one-arg overload detects UTF-8 by BOM/meta and sets baseUri to absolute path")
    public void test_TC14() throws IOException {
        // Create a temp HTML path with BOM and <div>BOM</div>
        byte[] bom = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
        Path path = Files.createTempFile("tc14", ".html");
        Files.write(path, bom);
        Files.write(path, "<div>BOM</div>".getBytes(StandardCharsets.UTF_8), java.nio.file.StandardOpenOption.APPEND);
        path.toFile().deleteOnExit();

        // Exercise the one-arg Path overload; ensures B3->B5 branch
        Document doc = Jsoup.parse(path);

        // Verify that BOM is handled and that baseUri is absolute path
        assertEquals("BOM", doc.selectFirst("div").text());
        assertEquals(path.toAbsolutePath().toString(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri, Parser) with htmlParser returns parsed Document and applies given baseUri")
    public void test_TC15() throws IOException {
        // Create a temp HTML path with <span>SP</span>
        Path path = Files.createTempFile("tc15", ".html");
        Files.write(path, "<span>SP</span>".getBytes(StandardCharsets.UTF_8));
        path.toFile().deleteOnExit();
        String baseUri = "https://pu/";
        Parser parser = Parser.htmlParser();

        // Exercise the four-arg Path overload with htmlParser; ensures B4->B6 branch
        Document doc = Jsoup.parse(path, "UTF-8", baseUri, parser);

        // Verify content is parsed as HTML and baseUri applied
        assertEquals("SP", doc.selectFirst("span").text());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) with valid UTF-8 stream returns parsed Document and closes stream")
    public void test_TC16() throws IOException {
        // Custom InputStream to track close()
        class TrackCloseStream extends ByteArrayInputStream {
            private boolean closed = false;
            public TrackCloseStream(byte[] buf) { super(buf); }
            @Override public void close() throws IOException { super.close(); closed = true; }
            public boolean isClosed() { return closed; }
        }

        String html = "<ul><li>One</li></ul>";
        TrackCloseStream in = new TrackCloseStream(html.getBytes(StandardCharsets.UTF_8));
        String baseUri = "http://stream/";

        // Exercise the three-arg InputStream overload; ensures B3->B5 branch and stream is closed
        Document doc = Jsoup.parse(in, "UTF-8", baseUri);

        // Verify parsed list and that baseUri is used and stream closed
        assertEquals("One", doc.selectFirst("li").text());
        assertEquals(baseUri, doc.baseUri());
        assertTrue(in.isClosed(), "InputStream should be closed after parsing");
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri, Parser) with xmlParser returns XML-parsed Document and closes stream")
    public void test_TC17() throws IOException {
        // Custom InputStream to track close()
        class TrackCloseStream extends ByteArrayInputStream {
            private boolean closed = false;
            public TrackCloseStream(byte[] buf) { super(buf); }
            @Override public void close() throws IOException { super.close(); closed = true; }
            public boolean isClosed() { return closed; }
        }

        String xml = "<root><e>3</e></root>";
        TrackCloseStream in = new TrackCloseStream(xml.getBytes(StandardCharsets.UTF_8));
        String baseUri = "http://streamX/";
        Parser parser = Parser.xmlParser();

        // Exercise the four-arg InputStream overload with xmlParser; ensures B4->B6 branch and stream closed
        Document doc = Jsoup.parse(in, "UTF-8", baseUri, parser);

        // Verify xml parsed element and baseUri, and that stream is closed
        assertEquals("3", doc.selectFirst("e").text());
        assertEquals(baseUri, doc.baseUri());
        assertTrue(in.isClosed(), "InputStream should be closed after parsing");
    }
}