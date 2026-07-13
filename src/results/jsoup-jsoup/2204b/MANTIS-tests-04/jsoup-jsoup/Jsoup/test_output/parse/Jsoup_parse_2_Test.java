package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC23: parse(String html, String baseUri, Parser parser) with null html throws NullPointerException")
    public void test_TC23() {
        // Passing null html to overload that delegates to parser.parseInput should trigger NPE on null check
        String html = null;
        String baseUri = "http://x";
        Parser parser = Parser.htmlParser();
        assertThrows(NullPointerException.class, () -> Jsoup.parse(html, baseUri, parser));
    }

    @Test
    @DisplayName("TC24: parseBodyFragment(String bodyHtml, String baseUri) returns document fragment with resolved baseUri")
    public void test_TC24() {
        // A simple anchor fragment tests the branch that uses provided baseUri for href resolution
        String bodyHtml = "<a href='r'>r</a>";
        String baseUri = "http://b/";
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        assertAll("Fragment and baseUri should match",
            () -> assertEquals("<a href=\"r\">r</a>", doc.body().html()),
            () -> assertEquals("http://b/", doc.baseUri())
        );
    }

    @Test
    @DisplayName("TC25: parseBodyFragment(String bodyHtml) returns document fragment with empty baseUri")
    public void test_TC25() {
        // Calling overload without baseUri should result in empty baseUri
        String bodyHtml = "<span/>";
        Document doc = Jsoup.parseBodyFragment(bodyHtml);
        assertAll("Fragment and default-empty baseUri should match",
            () -> assertEquals("<span></span>", doc.body().html()),
            () -> assertEquals("", doc.baseUri())
        );
    }

    @Test
    @DisplayName("TC26: parse(Path path, String charsetName, String baseUri, Parser parser) with XML parser preserves unbalanced tags")
    public void test_TC26() throws IOException {
        // XML parser should auto-balance tags: unclosed <x> becomes <x>y</x>
        Path tmp = Files.createTempFile("jsoup", ".xml");
        Files.write(tmp, "<x>y".getBytes(StandardCharsets.UTF_8));
        String charset = null;
        String baseUri = "custom";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(tmp, charset, baseUri, parser);
        assertAll("XML fragment body and custom baseUri should match",
            () -> assertEquals("<x>y</x>", doc.body().html()),
            () -> assertEquals("custom", doc.baseUri())
        );
        // cleanup
        Files.deleteIfExists(tmp);
    }

    @Test
    @DisplayName("TC27: parse(Path path, String charsetName, String baseUri) throws IOException for non-existent path")
    public void test_TC27() {
        // Non-existent file path should lead to IOException in DataUtil.load
        Path path = Paths.get("no-such-file.html");
        String charset = "UTF-8";
        String baseUri = "u";
        assertThrows(IOException.class, () -> Jsoup.parse(path, charset, baseUri));
    }

    @Test
    @DisplayName("TC28: parse(Path path, String charsetName) with null charset uses path as baseUri")
    public void test_TC28() throws IOException {
        // When charset is null, overload uses file B0→B1→B3→B5 branch and baseUri = path.toAbsolutePath().toString()
        Path tmp = Files.createTempFile("jsoup", ".html");
        Files.write(tmp, "<p>z</p>".getBytes(StandardCharsets.UTF_8));
        String charset = null;
        Document doc = Jsoup.parse(tmp, charset);
        assertAll("Body fragment and auto baseUri from path should match",
            () -> assertEquals("<p>z</p>", doc.body().html()),
            () -> assertEquals(tmp.toAbsolutePath().toString(), doc.baseUri())
        );
        // cleanup
        Files.deleteIfExists(tmp);
    }
}