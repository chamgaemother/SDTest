package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(String html, String baseUri) overload returns Document with given baseUri and parsed content")
    public void test_TC19() {
        // Branch B0→B1→B2→B3→B5: direct call to Parser.parse
        String html = "<div>X</div>";
        String baseUri = "http://host/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("<div>X</div>", doc.body().html(), "Body HTML should match input HTML");
        assertEquals(baseUri, doc.baseUri(), "Base URI should be preserved as given");
    }

    @Test
    @DisplayName("parse(File file, String charsetName) overload loads file with default baseUri from file path")
    public void test_TC20() throws IOException {
        // Branch B0→B1→B4→B6: DataUtil.load(file, null, fileAbs)
        Path tmp = Files.createTempFile(null, ".html");
        Files.write(tmp, "<p>File</p>".getBytes());
        File file = tmp.toFile();
        String charset = null;
        Document doc = Jsoup.parse(file, charset);
        assertEquals("<p>File</p>", doc.body().html(), "Body HTML should equal file content");
        assertEquals(file.getAbsolutePath(), doc.baseUri(), "Base URI should default to file absolute path");
    }

    @Test
    @DisplayName("parse(File file, String charsetName, String baseUri, Parser parser) overload uses custom parser and baseUri")
    public void test_TC21() throws IOException {
        // Branch B0→B1→B4→B7: DataUtil.load(file, null, baseUri, parser)
        Path tmp = Files.createTempFile(null, ".html");
        Files.write(tmp, "<root><a/></root>".getBytes());
        File file = tmp.toFile();
        Parser xml = Parser.xmlParser();
        String base = "http://x/";
        Document doc = Jsoup.parse(file, null, base, xml);
        assertEquals(1, doc.select("a").size(), "XML parser should find one <a> element");
        assertEquals(base, doc.baseUri(), "Base URI should be set as given");
    }

    @Test
    @DisplayName("parse(Path path, String charsetName, String baseUri, Parser parser) overload uses XML parser on Path")
    public void test_TC22() throws IOException {
        // Branch B0→B1→B4→B7: DataUtil.load(path, null, baseUri, parser)
        Path tmp = Files.createTempFile(null, ".html");
        Files.writeString(tmp, "<tag>v</tag>");
        Parser xml = Parser.xmlParser();
        String base = "https://u/";
        Document doc = Jsoup.parse(tmp, null, base, xml);
        assertEquals(1, doc.select("tag").size(), "XML parser should find one <tag> element");
        assertEquals(base, doc.baseUri(), "Base URI should be set as provided");
    }

    @Test
    @DisplayName("parse(InputStream in, String charsetName, String baseUri) overload reads from stream and sets baseUri")
    public void test_TC23() throws IOException {
        // Branch B0→B1→B4→B6: DataUtil.load(in, null, baseUri)
        byte[] data = "<i>I</i>".getBytes();
        try (InputStream in = new ByteArrayInputStream(data)) {
            String base = "http://b/";
            Document doc = Jsoup.parse(in, null, base);
            assertEquals("<i>I</i>", doc.body().html(), "Body HTML should equal input fragment");
            assertEquals(base, doc.baseUri(), "Base URI should equal provided URI");
        }
    }

    @Test
    @DisplayName("parse(InputStream in, String charsetName, String baseUri, Parser parser) overload applies custom parser on stream")
    public void test_TC24() throws IOException {
        // Branch B0→B1→B4→B7: DataUtil.load(in, null, baseUri, parser)
        byte[] data = "<x>1</x>".getBytes();
        try (InputStream in = new ByteArrayInputStream(data)) {
            Parser xml = Parser.xmlParser();
            String base = "https://z/";
            Document doc = Jsoup.parse(in, null, base, xml);
            assertEquals(1, doc.select("x").size(), "XML parser should find one <x> element");
            assertEquals(base, doc.baseUri(), "Base URI should be set correctly");
        }
    }
}