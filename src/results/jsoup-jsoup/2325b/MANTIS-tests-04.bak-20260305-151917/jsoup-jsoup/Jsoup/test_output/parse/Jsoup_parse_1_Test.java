package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(File, charsetName) uses explicit charsetName and file absolute path as baseUri")
    public void test_TC11() throws Exception {
        // GIVEN a temporary HTML file with simple content
        File temp = File.createTempFile("jsoup", ".html");
        temp.deleteOnExit();
        String html = "<b>X</b>";
        try (Writer w = new OutputStreamWriter(new FileOutputStream(temp), "UTF-8")) {
            w.write(html);
        }
        String charset = "UTF-8";
        // WHEN parsing with explicit charset
        Document doc = Jsoup.parse(temp, charset);
        // THEN the body HTML matches and baseUri is the file absolute path
        assertEquals(html, doc.body().html());
        assertEquals(temp.getAbsolutePath(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(File, charsetName, baseUri, parser) forwards to DataUtil.load with custom parser")
    public void test_TC12() throws Exception {
        // GIVEN a temporary HTML file with a self-closing tag for XML
        File temp = File.createTempFile("jsoup", ".html");
        temp.deleteOnExit();
        String xmlFragment = "<tag/>";
        Files.write(temp.toPath(), xmlFragment.getBytes("UTF-8"));
        Parser xml = Parser.xmlParser();
        String charset = null;
        String baseUri = "http://x/";
        // WHEN parsing with custom XML parser
        Document doc = Jsoup.parse(temp, charset, baseUri, xml);
        // THEN XML parser recognizes self-closing tag and baseUri honored
        assertEquals(1, doc.select("tag").size());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path, charsetName) reads path with explicit charset and uses path absolute as baseUri")
    public void test_TC13() throws Exception {
        // GIVEN a temp Path with italic tag
        Path p = Files.createTempFile("jsoup", ".html");
        p.toFile().deleteOnExit();
        String html = "<i>Y</i>";
        Files.write(p, html.getBytes("UTF-8"));
        String charset = "UTF-8";
        // WHEN parsing with explicit charset
        Document doc = Jsoup.parse(p, charset);
        // THEN body HTML matches and baseUri is path absolute
        assertEquals(html, doc.body().html());
        assertEquals(p.toAbsolutePath().toString(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path) auto-detect charsetName null and path absolute baseUri")
    public void test_TC14() throws Exception {
        // GIVEN a temp Path with underline tag
        Path p = Files.createTempFile("jsoup", ".html");
        p.toFile().deleteOnExit();
        String html = "<u>Z</u>";
        Files.write(p, html.getBytes("UTF-8"));
        // WHEN parsing with auto charset
        Document doc = Jsoup.parse(p);
        // THEN body HTML matches and baseUri is path absolute
        assertEquals(html, doc.body().html());
        assertEquals(p.toAbsolutePath().toString(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri, parser) uses custom parser on Path input")
    public void test_TC15() throws Exception {
        // GIVEN a temp Path with custom tag for XML parser
        Path p = Files.createTempFile("jsoup", ".html");
        p.toFile().deleteOnExit();
        String xmlFragment = "<foo/>";
        Files.write(p, xmlFragment.getBytes("UTF-8"));
        Parser xml = Parser.xmlParser();
        String charset = null;
        String baseUri = "https://y/";
        // WHEN parsing with XML parser override
        Document doc = Jsoup.parse(p, charset, baseUri, xml);
        // THEN self-closing foo recognized and baseUri honored
        assertEquals(1, doc.select("foo").size());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri, parser) closes stream and uses provided parser")
    public void test_TC16() throws Exception {
        // GIVEN a RecordingInputStream over "<p/>"
        byte[] data = "<p/>".getBytes("UTF-8");
        RecordingInputStream in = new RecordingInputStream(new ByteArrayInputStream(data));
        Parser xml = Parser.xmlParser();
        String charset = "UTF-8";
        String baseUri = "http://s/";
        // WHEN parsing with stream and parser override
        Document doc = Jsoup.parse(in, charset, baseUri, xml);
        // THEN stream is closed, p recognized, and baseUri honored
        assertTrue(in.isClosed(), "InputStream should be closed by the parse method");
        assertEquals(1, doc.select("p").size());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(File, charsetName) throws IOException on invalid charsetName")
    public void test_TC17() throws Exception {
        // GIVEN a temp HTML file
        File temp = File.createTempFile("jsoup", ".html");
        temp.deleteOnExit();
        Files.write(temp.toPath(), "<b>X</b>".getBytes("UTF-8"));
        String badCharset = "NO-CHARSET";
        // WHEN calling with unsupported charset, THEN IOException
        assertThrows(IOException.class, () -> Jsoup.parse(temp, badCharset));
    }

    // Helper to record close()
    static class RecordingInputStream extends FilterInputStream {
        private boolean closed = false;
        protected RecordingInputStream(InputStream in) {
            super(in);
        }
        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
        public boolean isClosed() {
            return closed;
        }
    }
}