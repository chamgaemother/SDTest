package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for overloaded Jsoup.parse and Jsoup.parseBodyFragment methods.
 */
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parseBodyFragment(html, baseUri) returns a Document whose body contains the fragment and correct baseUri")
    public void test_TC18() {
        // fragment branch: B0→B1→B2→B4→B5; providing non-null html and baseUri exercises parseBodyFragment(html, baseUri)
        String fragment = "<span>F</span>";
        String base = "http://b/";
        Document doc = Jsoup.parseBodyFragment(fragment, base);
        // The body HTML should match the fragment exactly
        assertEquals(fragment, doc.body().html());
        // The baseUri should be applied as given
        assertEquals(base, doc.baseUri());
    }

    @Test
    @DisplayName("parseBodyFragment(html) uses empty baseUri and returns Document with fragment in body")
    public void test_TC19() {
        // fragment branch: B0→B1→B3→B4→B5; using single-arg overload
        String fragment = "<div>X</div>";
        Document doc = Jsoup.parseBodyFragment(fragment);
        // Body HTML should contain the fragment; parser may add spacing but tag content X must be present
        String bodyHtml = doc.body().html();
        assertTrue(bodyHtml.contains("<div>X</div>") || bodyHtml.contains("<div> X</div>"),
                "Expected body HTML to contain the fragment tag");
        // Base URI should be empty string
        assertEquals("", doc.baseUri());
    }

    @Test
    @DisplayName("parseBodyFragment(null, baseUri) throws NullPointerException for null HTML")
    public void test_TC20() {
        // exception branch: B0→B2→B6; null html triggers NPE
        String base = "http://b/";
        assertThrows(NullPointerException.class,
                () -> Jsoup.parseBodyFragment((String) null, base));
    }

    @Test
    @DisplayName("parseBodyFragment(null) throws NullPointerException for null HTML")
    public void test_TC21() {
        // exception branch: B0→B1→B6; single-arg overload with null html
        assertThrows(NullPointerException.class,
                () -> Jsoup.parseBodyFragment((String) null));
    }

    @Test
    @DisplayName("parse(File) throws IOException for non-existent file")
    public void test_TC22() {
        // file-not-found branch: B0→B1→B7; parse(File) should throw IOException
        File file = new File("no-such-file.html");
        assertThrows(IOException.class,
                () -> Jsoup.parse(file));
    }

    @Test
    @DisplayName("parse(File,charsetName,baseUri,parser) with null parser throws NullPointerException")
    public void test_TC23() throws IOException {
        // parser-null branch: B0→B1→B2→B8; config temp file with minimal HTML
        File file = File.createTempFile("tc23", ".html");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("<p>t</p>");
        }
        String charset = "UTF-8";
        String base = "http://x/";
        Parser parser = null;
        assertThrows(NullPointerException.class,
                () -> Jsoup.parse(file, charset, base, parser));
        file.delete();
    }

    @Test
    @DisplayName("parse(InputStream,charsetName,baseUri,parser) with null parser throws NullPointerException")
    public void test_TC24() {
        // parser-null branch: B0→B1→B2→B8; invalid parser for InputStream overload
        InputStream in = new ByteArrayInputStream("<e/>".getBytes());
        String charset = "UTF-8";
        String base = "";
        Parser parser = null;
        assertThrows(NullPointerException.class,
                () -> Jsoup.parse(in, charset, base, parser));
    }

    @Test
    @DisplayName("parse(Path) throws IOException for non-existent path")
    public void test_TC25() {
        // path-not-found branch: B0→B1→B7; parse(Path) should throw IOException
        Path path = Paths.get("no-file.html");
        assertThrows(IOException.class,
                () -> Jsoup.parse(path));
    }

    @Test
    @DisplayName("parse(Path,charsetName,baseUri,parser) with null parser throws NullPointerException")
    public void test_TC26() throws IOException {
        // parser-null branch: B0→B1→B2→B8; temp path exists
        Path path = Files.createTempFile("tc26", ".html");
        Files.write(path, "<t/>".getBytes());
        String charset = "UTF-8";
        String base = "";
        Parser parser = null;
        assertThrows(NullPointerException.class,
                () -> Jsoup.parse(path, charset, base, parser));
        Files.deleteIfExists(path);
    }

    @Test
    @DisplayName("parse(InputStream,charsetName,baseUri) with null InputStream throws NullPointerException")
    public void test_TC27() {
        // null stream branch: B0→B1→B9; null InputStream leads to NPE
        InputStream in = null;
        String charset = "UTF-8";
        String base = "";
        assertThrows(NullPointerException.class,
                () -> Jsoup.parse(in, charset, base));
    }

    @Test
    @DisplayName("parse(URL, timeout) with HTTP URL returns Document and applies timeout")
    public void test_TC28() throws Exception {
        // network branch: B0→B1→B3→B10→B11; successful HTTP GET
        URL url = new URL("http://httpbin.org/html");
        int timeout = 5000;
        Document doc = Jsoup.parse(url, timeout);
        // Document should contain at least one <html> tag
        assertTrue(doc.select("html").size() > 0,
                "Expected HTML document to contain <html> elements");
    }
}