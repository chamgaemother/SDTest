package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.jsoup.internal.SharedConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(File, null, baseUri) throws IOException for non-existent file path")
    public void test_TC27() {
        // GIVEN a File that does not exist, to trigger a file-not-found path in DataUtil.load
        File f = new File("/non/existent.html");
        String charsetName = null;
        String baseUri = "http://example.com/";
        // WHEN/THEN: expect IOException due to missing file
        assertThrows(IOException.class, () -> Jsoup.parse(f, charsetName, baseUri));
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri) throws IOException for missing Path")
    public void test_TC28() {
        // GIVEN a Path that does not exist, so DataUtil.load will throw IOException on path-not-found
        Path p = Paths.get("/no/such.html");
        String charset = "UTF-8";
        String baseUri = "https://x";
        // WHEN/THEN: missing path triggers IOException
        assertThrows(IOException.class, () -> Jsoup.parse(p, charset, baseUri));
    }

    @Test
    @DisplayName("parse(InputStream, charsetName, baseUri) correctly parses with non-null charsetName")
    public void test_TC29() throws IOException {
        // GIVEN an InputStream with simple HTML, and a valid charset to use the non-null charsetName path
        byte[] htmlBytes = "<p>ZZ</p>".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(htmlBytes);
        String charset = "UTF-8";
        String baseUri = "u";
        // WHEN: parse(InputStream) should read and close stream, producing a Document with body html and baseUri
        Document doc = Jsoup.parse(in, charset, baseUri);
        // THEN: the body fragment HTML matches input and the baseUri is preserved
        assertAll(
            () -> assertEquals("<p>ZZ</p>", doc.body().html(), "Expected body HTML from input stream"),
            () -> assertEquals("u", doc.baseUri(), "Expected baseUri to be set on document")
        );
    }

    @Test
    @DisplayName("clean(String, non-empty baseUri, safelist.preserveRelativeLinks=true does not use DummyUri prefix)")
    public void test_TC30() {
        // GIVEN html with a relative link, non-empty baseUri to bypass DummyUri substitution, and safelist preserving relative links
        String html = "<a href=\"page.html\">x</a>";
        String baseUri = "http://ex/";
        Safelist safelist = Safelist.basic().preserveRelativeLinks(true);
        // WHEN: cleaning with non-empty baseUri should resolve relative URLs without injecting DummyUri
        String out = Jsoup.clean(html, baseUri, safelist);
        // THEN: output contains fully qualified link and does not contain DummyUri placeholder
        assertAll(
            () -> assertTrue(out.contains("href=\"http://ex/page.html\""), "Expected resolved absolute URL without DummyUri"),
            () -> assertFalse(out.contains(SharedConstants.DummyUri), "DummyUri should not appear when baseUri is non-empty and preserveRelativeLinks is true")
        );
    }

    @Test
    @DisplayName("parse(String, baseUri, xmlParser) retains xml structure and non-empty baseUri")
    public void test_TC31() {
        // GIVEN XML input and xmlParser to follow alternate parsing path, with a non-empty baseUri
        String html = "<root><item>1</item></root>";
        String baseUri = "urn:test:";
        Parser parser = Parser.xmlParser();
        // WHEN: parse with xml parser should retain element structure and set baseUri
        Document doc = Jsoup.parse(html, baseUri, parser);
        // THEN: exactly one <item> element and baseUri preserved
        assertAll(
            () -> assertEquals(1, doc.select("item").size(), "Expected one <item> element parsed by xmlParser"),
            () -> assertEquals("urn:test:", doc.baseUri(), "Expected baseUri to be set on document")
        );
    }
}