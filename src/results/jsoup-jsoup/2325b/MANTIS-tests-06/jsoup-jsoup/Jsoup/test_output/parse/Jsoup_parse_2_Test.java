package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC21: parse(html, Parser) invokes custom parser.parseInput and returns stub Document")
    public void test_TC21() {
        // Use stub Parser to cover overload that calls Parser.parseInput(html, "")
        String html = "<x>y</x>";
        Parser stub = new Parser(null) {
            @Override
            public Document parseInput(String sourceHtml, String baseUri) {
                Document d = new Document(baseUri);
                d.title("stub1");
                return d;
            }
        };
        Document doc = Jsoup.parse(html, stub);
        // Expect baseUri "" and title from stub
        assertEquals("", doc.baseUri());
        assertEquals("stub1", doc.title());
    }

    @Test
    @DisplayName("TC22: parse(html, baseUri, Parser) invokes custom parser.parseInput with provided baseUri")
    public void test_TC22() {
        // Cover overload parse(String, String, Parser) path B0->B2->B4
        String html = "body";
        String baseUri = "u2";
        Parser stub = new Parser(null) {
            @Override
            public Document parseInput(String src, String bu) {
                Document d = new Document(bu);
                d.title("stub2");
                return d;
            }
        };
        Document doc = Jsoup.parse(html, baseUri, stub);
        assertEquals("u2", doc.baseUri());
        assertEquals("stub2", doc.title());
    }

    @Test
    @DisplayName("TC23: parseBodyFragment(bodyHtml, baseUri) returns Document containing fragment in body")
    public void test_TC23() {
        // parseBodyFragment should parse fragment into body with given baseUri
        String fragment = "<p>f</p>";
        String baseUri = "http://a/";
        Document doc = Jsoup.parseBodyFragment(fragment, baseUri);
        assertEquals("http://a/", doc.baseUri());
        assertEquals("<p>f</p>", doc.body().html());
    }

    @Test
    @DisplayName("TC24: parseBodyFragment(bodyHtml) uses empty baseUri and returns fragment")
    public void test_TC24() {
        // Default overload uses empty baseUri, covering path B0->B1->B6
        String fragment = "<span>z</span>";
        Document doc = Jsoup.parseBodyFragment(fragment);
        assertEquals("", doc.baseUri());
        assertEquals("<span>z</span>", doc.body().html());
    }

    @Test
    @DisplayName("TC25: parse(File, charsetName, baseUri, Parser) uses DataUtil.load with parser override")
    public void test_TC25() throws IOException {
        // Use temp file to cover overload that delegates to DataUtil.load(file, charset, baseUri, parser)
        Path temp = Files.createTempFile("jsoup", ".html");
        Files.write(temp, "<d>d</d>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        String charset = "UTF-8";
        String baseUri = "u3";
        // Stub parser: DataUtil.load will use our stub parser.printInput inside, covering path B7->B8
        Parser stub = new Parser(null) {
            @Override
            public Document parseInput(String h, String bu) {
                Document d = new Document(bu);
                d.title("fp2");
                return d;
            }
        };
        Document doc = Jsoup.parse(file, charset, baseUri, stub);
        assertEquals("u3", doc.baseUri());
        assertEquals("fp2", doc.title());
    }

    @Test
    @DisplayName("TC26: parse(Path, charsetName, baseUri, Parser) uses DataUtil.load(Path) with parser override")
    public void test_TC26() throws IOException {
        // Use temp path to cover overload that delegates to DataUtil.load(path, charset, baseUri, parser)
        Path path = Files.createTempFile("jsoup", ".html");
        Files.write(path, "<ul><li>i</li></ul>".getBytes(StandardCharsets.UTF_8));
        String charset = null;
        String baseUri = "bu4";
        Parser stub = new Parser(null) {
            @Override
            public Document parseInput(String h, String bu) {
                Document d = new Document(bu);
                d.title("pp3");
                return d;
            }
        };
        Document doc = Jsoup.parse(path, charset, baseUri, stub);
        assertEquals("bu4", doc.baseUri());
        assertEquals("pp3", doc.title());
    }
}