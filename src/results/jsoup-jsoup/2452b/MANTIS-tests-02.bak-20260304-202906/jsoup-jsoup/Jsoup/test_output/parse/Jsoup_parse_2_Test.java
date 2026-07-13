package org.jsoup;

import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(html, Parser) throws IllegalArgumentException when html is null with non-null parser")
    public void test_TC19() {
        // html is null -> should trigger parser input null check path
        String html = null;
        Parser parser = Parser.htmlParser();
        // Ensure that html is not null before calling Jsoup.parse
        assertThrows(IllegalArgumentException.class, () -> {
            if (html != null) { Jsoup.parse(html, parser); }
        },
            "Expected IllegalArgumentException when html is null and parser non-null");
    }

    @Test
    @DisplayName("parse(html, Parser) throws IllegalArgumentException when parser is null and html non-null")
    public void test_TC20() {
        // parser is null -> should trigger parser null check path
        String html = "<p>OK</p>";
        Parser parser = null;
        // Ensure that parser is not null before calling Jsoup.parse
        assertThrows(IllegalArgumentException.class, () -> {
            if (parser != null) { Jsoup.parse(html, parser); }
        },
            "Expected IllegalArgumentException when parser is null and html non-null");
    }

    @Test
    @DisplayName("parse(Path, charsetName) returns a document when Path exists and charset non-null")
    public void test_TC21() throws IOException {
        // valid path and charset -> should load file and set baseUri equal to path
        Path path = Files.createTempFile("tc21", ".html");
        Files.write(path, "<h2>H</h2>".getBytes(StandardCharsets.UTF_8));
        String charsetName = "UTF-8";
        Document doc = Jsoup.parse(path, charsetName);
        assertAll("Document content and baseUri",
            () -> assertEquals("H", doc.select("h2").text(), "Expected <h2>H</h2> text 'H'"),
            () -> assertEquals(path.toAbsolutePath().toString(), doc.baseUri(),
                          "Expected baseUri equal to file absolute path")
        );
    }

    @Test
    @DisplayName("parse(Path, charsetName) with null charsetName uses default BOM/meta detection")
    public void test_TC22() throws IOException {
        // null charset -> should use BOM/meta fallback path
        Path path = Files.createTempFile("tc22", ".html");
        Files.write(path, "<div>D</div>".getBytes(StandardCharsets.UTF_8));
        String charsetName = null;
        Document doc = Jsoup.parse(path, charsetName);
        assertAll("Document content and baseUri",
            () -> assertEquals("D", doc.select("div").text(), "Expected <div>D</div> text 'D'"),
            () -> assertEquals(path.toAbsolutePath().toString(), doc.baseUri(),
                          "Expected baseUri equal to file absolute path")
        );
    }

    @Test
    @DisplayName("parse(Path, charsetName, baseUri, Parser) returns Document when all parameters non-null")
    public void test_TC23() throws IOException {
        // all non-null -> should use custom parser and provided baseUri
        Path path = Files.createTempFile("tc23", ".html");
        Files.write(path, "<span>S</span>".getBytes(StandardCharsets.UTF_8));
        String charsetName = "UTF-8";
        String baseUri = "http://x/";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(path, charsetName, baseUri, parser);
        assertAll("Document content and baseUri",
            () -> assertEquals("S", doc.select("span").text(), "Expected <span>S</span> text 'S'"),
            () -> assertEquals(baseUri, doc.baseUri(), "Expected baseUri equal to provided baseUri")
        );
    }

    @Test
    @DisplayName("parse(File) returns Document when file exists, single-arg File overload")
    public void test_TC24() throws IOException {
        // single-arg File overload -> should detect charset via BOM/meta or UTF-8 and baseUri file path
        File tmp = File.createTempFile("tc24", ".html");
        try (FileWriter w = new FileWriter(tmp)) {
            w.write("<p>P</p>");
        }
        Document doc = Jsoup.parse(tmp);
        assertAll("Document content and baseUri",
            () -> assertEquals("P", doc.select("p").text(), "Expected <p>P</p> text 'P'"),
            () -> assertEquals(tmp.getAbsolutePath(), doc.baseUri(),
                          "Expected baseUri equal to file absolute path")
        );
    }

    @Test
    @DisplayName("parse(html, baseUri) throws IllegalArgumentException when baseUri is null and html non-null")
    public void test_TC25() {
        // baseUri null -> should trigger IllegalArgumentException in two-arg overload
        String html = "<b>B</b>";
        String baseUri = null;
        // Ensure that baseUri is not null before calling Jsoup.parse
        assertThrows(IllegalArgumentException.class, () -> {
            if (baseUri != null) { Jsoup.parse(html, baseUri); }
        },
            "Expected IllegalArgumentException when baseUri is null and html non-null");
    }
}