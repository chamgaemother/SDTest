package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(String html, String baseUri, Parser parser) resolves relative URLs with htmlParser")
    public void test_TC12() {
        String html = "<img src='pic.png'>";
        String base = "http://example.com/sub/";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, base, parser);
        String abs = doc.select("img").first().absUrl("src");
        assertEquals("http://example.com/sub/pic.png", abs);
    }

    @Test
    @DisplayName("parse(String html, Parser parser) with empty html returns empty body for xmlParser")
    public void test_TC13() {
        String html = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals("", doc.body().html());
    }

    @Test
    @DisplayName("parse(String html, String baseUri, Parser parser) with null parser throws IllegalArgumentException")
    public void test_TC14() {
        String html = "<p>test</p>";
        String base = "";
        Parser parser = null;
        assertThrows(IllegalArgumentException.class, () -> Jsoup.parse(html, base, parser));
    }

    @Test
    @DisplayName("parse(File file, String charsetName, String baseUri, Parser parser) uses xmlParser to parse file")
    public void test_TC15() throws IOException {
        Path temp = Files.createTempFile("t", ".html");
        Files.write(temp, "<div>X</div>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        String charset = "UTF-8";
        String base = "http://b/";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(file, charset, base, parser);
        assertEquals("X", doc.select("div").first().text());
    }

    @Test
    @DisplayName("parse(Path path, String charsetName) with nonexistent path throws IOException")
    public void test_TC16() {
        Path path = Paths.get("no-such-file.html");
        String charset = "UTF-8";
        assertThrows(IOException.class, () -> Jsoup.parse(path, charset));
    }

    @Test
    @DisplayName("parse(InputStream in, String charsetName, String baseUri, Parser parser) with xmlParser reads stream")
    public void test_TC17() throws IOException {
        byte[] data = "<span>Z</span>".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        String charset = "UTF-8";
        String base = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(in, charset, base, parser);
        assertEquals("Z", doc.select("span").first().text());
    }

    @Test
    @DisplayName("parseBodyFragment(String bodyHtml, String baseUri) resolves relative links in fragment")
    public void test_TC18() {
        String body = "<a href='p'>here</a>";
        String base = "https://x/y/";
        Document doc = Jsoup.parseBodyFragment(body, base);
        String href = doc.select("a").first().absUrl("href");
        assertEquals("https://x/y/p", href);
    }
}