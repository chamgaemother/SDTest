package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.jsoup.Connection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("TC11: parse(File, charsetName, baseUri, parser) returns Document via provided XML parser")
    public void test_TC11() throws IOException {
        // Branch: file-overload with custom parser, testing B0→B1→B2→B3→B5
        File temp = Files.createTempFile("jsoup_test11", ".xml").toFile();
        temp.deleteOnExit();
        // write simple XML content
        Files.write(temp.toPath(), "<root/>".getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String base = "http://base.test";
        Parser xmlParser = Parser.xmlParser();
        Document doc = Jsoup.parse(temp, charset, base, xmlParser);
        // assert the XML fragment is preserved in body
        assertTrue(doc.body().html().contains("<root/>"), "Body should contain <root/>");
        // assert baseUri preserved
        assertEquals(base, doc.baseUri(), "Base URI should match provided base");
    }

    @Test
    @DisplayName("TC12: parse(Path, charsetName, baseUri) reads file via Path and sets baseUri from parameter")
    public void test_TC12() throws IOException {
        // Branch: path-overload B0→B1→B2→B4→B5, with null charsetName fallback
        Path path = Files.createTempFile("jsoup_test12", ".html");
        path.toFile().deleteOnExit();
        String html = "<h2>Hi</h2>";
        Files.write(path, html.getBytes(StandardCharsets.UTF_8));
        String cs = null;
        String base = "http://path.test";
        Document doc = Jsoup.parse(path, cs, base);
        // exact body html
        assertEquals(html, doc.body().html(), "Body HTML should equal the file content");
        // baseUri set from parameter
        assertEquals(base, doc.baseUri(), "Base URI should match provided base");
    }

    @Test
    @DisplayName("TC13: parse(Path) loads file and uses Path.toAbsolutePath() as baseUri")
    public void test_TC13() throws IOException {
        // Branch: default-baseUri B0→B1→B2→B4→B6
        Path path = Files.createTempFile("jsoup_test13", ".html");
        path.toFile().deleteOnExit();
        String html = "<p>P</p>";
        Files.write(path, html.getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(path);
        // body matches content
        assertEquals(html, doc.body().html(), "Body HTML should match file content");
        // baseUri is absolute path
        assertEquals(path.toAbsolutePath().toString(), doc.baseUri(), "Base URI should be the file's absolute path");
    }

    @Test
    @DisplayName("TC14: parse(InputStream, charsetName, baseUri, parser) reads stream via XML parser")
    public void test_TC14() throws IOException {
        // Branch: stream-overload B0→B1→B3→B7
        String xml = "<item/>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        String cs = "UTF-8";
        String base = "http://stream.test";
        Parser xmlParser = Parser.xmlParser();
        Document doc = Jsoup.parse(in, cs, base, xmlParser);
        // body contains the item tag
        assertTrue(doc.body().html().contains("<item/>"), "Body should contain <item/>");
        // baseUri preserved from parameter
        assertEquals(base, doc.baseUri(), "Base URI should match provided base");
    }

    @Test
    @DisplayName("TC15: parse(URL, timeout) with non-http URL throws MalformedURLException")
    public void test_TC15() {
        // Branch: url-overload invalid scheme B0→B8→B9
        assertThrows(MalformedURLException.class, () -> {
            URL ftpUrl = new URL("ftp://example.com");
            Jsoup.parse(ftpUrl, 1000);
        });
    }

    @Test
    @DisplayName("TC16: parse(String) with null html throws IllegalArgumentException")
    public void test_TC16() {
        // Branch: null-input negative path B0→B1→B2→B3
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse((String) null));
    }

    @Test
    @DisplayName("TC17: clean(bodyHtml, baseUri, safelist) substitutes DummyUri when baseUri empty and preserveRelativeLinks true")
    public void test_TC17() {
        // Branch: clean-overload, baseUri empty and preserveRelativeLinks triggers DummyUri B0→B1→B2→B4
        String src = "<a href=foo>Link</a>";
        Safelist sl = Safelist.basic();
        sl.preserveRelativeLinks(true);
        String out = Jsoup.clean(src, "", sl);
        // ensure relative link is preserved (href=foo) rather than removed
        assertTrue(out.contains("href=\"foo\""), "Relative href should be preserved when preserveRelativeLinks is true");
    }
}