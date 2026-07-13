package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(html, empty baseUri) returns Document with empty baseUri and normalized body")
    public void test_TC11() {
        String html = "<div/>";
        String baseUri = "";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("", doc.baseUri(), "Expected baseUri to remain empty");
        assertEquals("<div></div>", doc.body().html(), "Expected self-closing div normalized to <div></div>");
    }

    @Test
    @DisplayName("parse(html, custom Parser) invokes parser.parseInput and returns stub Document")
    public Document test_TC12() {
        String html = "x";
        Parser stub = new Parser() {
            public Parser() {}
            @Override
            public Document parseInput(String h, String u) {
                Document d = new Document(u);
                d.title("stub");
                return d;
            }
        };
        Document doc = Jsoup.parse(html, stub);
        assertEquals("", doc.baseUri(), "Expected default baseUri empty");
        assertEquals("stub", doc.title(), "Expected title from stub parser");
        return doc;
    }

    @Test
    @DisplayName("parse(html, baseUri, custom Parser) delegates to parser.parseInput with correct baseUri")
    public Document test_TC13() {
        String html = "p";
        String baseUri = "u";
        Parser stub = new Parser() {
            public Parser() {}
            @Override
            public Document parseInput(String h, String bu) {
                Document d = new Document(bu);
                d.title("byStub");
                return d;
            }
        };
        Document doc = Jsoup.parse(html, baseUri, stub);
        assertEquals("u", doc.baseUri(), "Expected baseUri passed to stub");
        assertEquals("byStub", doc.title(), "Expected title from stub");
        return doc;
    }

    @Test
    @DisplayName("parse(File, charset, baseUri, parser) returns Document via DataUtil with parser override")
    public Document test_TC14() throws IOException {
        Path temp = Files.createTempFile("jsoup-test", ".html");
        Files.write(temp, "<p>z</p>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        String charset = "UTF-8";
        String baseUri = "u";
        Parser stub = new Parser() {
            public Parser() {}
            @Override
            public Document parseInput(String h, String bu) {
                Document d = new Document(bu);
                d.title("fp");
                return d;
            }
        };
        Document doc = Jsoup.parse(file, charset, baseUri, stub);
        assertEquals("u", doc.baseUri(), "Expected baseUri from argument");
        assertEquals("fp", doc.title(), "Expected title from stub parser in DataUtil.load");
        return doc;
    }

    @Test
    @DisplayName("parse(Path, charset, baseUri) loads via DataUtil.load(Path)")
    public void test_TC15() throws IOException {
        Path path = Files.createTempFile("jsoup-test", ".html");
        String content = "<h4>h4</h4>";
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        String charset = null;
        String baseUri = "myUri";
        Document doc = Jsoup.parse(path, charset, baseUri);
        assertEquals("myUri", doc.baseUri(), "Expected provided baseUri");
        assertEquals(content, doc.body().html(), "Expected body HTML from file content");
    }

    @Test
    @DisplayName("parse(Path) loads with default charset and baseUri from toAbsolutePath")
    public void test_TC16() throws IOException {
        Path path = Files.createTempFile("jsoup-test", ".html");
        String fragment = "<span>txt</span>";
        Files.write(path, fragment.getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(path);
        String expectedUri = path.toAbsolutePath().toString();
        assertEquals(expectedUri, doc.baseUri(), "Expected baseUri from absolute path");
        assertTrue(doc.body().html().contains("<span>"), "Expected body to contain the span fragment");
    }

    @Test
    @DisplayName("parse(InputStream, charset, baseUri) loads via DataUtil.load(InputStream)")
    public void test_TC17() throws IOException {
        String html = "<li>i</li>";
        InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "u2";
        Document doc = Jsoup.parse(in, charset, baseUri);
        assertEquals("u2", doc.baseUri(), "Expected provided baseUri");
        assertEquals(html, doc.body().html(), "Expected body HTML from InputStream content");
    }

    @Test
    @DisplayName("parse(InputStream, charset, baseUri, parser) loads via DataUtil.load(InputStream, parser)")
    public void test_TC18() throws IOException {
        InputStream in = new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8));
        String charset = null;
        String baseUri = "bb";
        Parser stub = new Parser() {
            public Parser() {}
            @Override
            public Document parseInput(String h, String bu) {
                Document d = new Document(bu);
                d.title("st");
                return d;
            }
        };
        Document doc = Jsoup.parse(in, charset, baseUri, stub);
        assertEquals("st", doc.title(), "Expected title from stub parser");
        assertEquals("bb", doc.baseUri(), "Expected baseUri from argument");
    }

    @Test
    @DisplayName("parse(File) on non-existent file throws IOException")
    public void test_TC19() {
        File file = new File("nonexistent.html");
        assertThrows(IOException.class, () -> Jsoup.parse(file));
    }

    @Test
    @DisplayName("parse(Path) on non-existent path throws IOException")
    public void test_TC20() throws IOException {
        Path path = Path.of("nope.html");
        assertThrows(IOException.class, () -> Jsoup.parse(path));
    }
}