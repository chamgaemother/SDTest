package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("TC01_O1: parse(html, baseUri) with non-empty html and non-empty baseUri resolves relative links (baseUri not empty)")
    public void test_TC01_O1() {
        // baseUri non-empty should cause relative link to resolve against baseUri
        String html = "<a href=\"/page\">link</a>";
        String baseUri = "http://example.com";
        Document doc = Jsoup.parse(html, baseUri, Parser.htmlParser()); // Specify the parser to resolve ambiguity
        assertEquals("<a href=\"http://example.com/page\">link</a>", doc.body().html());
    }

    @Test
    @DisplayName("TC02_O1: parse(html, baseUri) with empty html returns empty body (html empty)")
    public void test_TC02_O1() {
        // empty HTML yields empty body content
        String html = "";
        String baseUri = "http://any/";
        Document doc = Jsoup.parse(html, baseUri, Parser.htmlParser()); // Specify the parser to resolve ambiguity
        assertEquals("", doc.body().html());
    }

    @Test
    @DisplayName("TC03_O1: parse(html, baseUri) with null html throws NullPointerException (html null)")
    public void test_TC03_O1() {
        // null HTML should immediately throw NPE before parsing
        String baseUri = "http://x/";
        assertThrows(NullPointerException.class, () -> Jsoup.parse(null, baseUri, Parser.htmlParser())); // Specify the parser to resolve ambiguity
    }

    @Test
    @DisplayName("TC04_O2: parse(html) (no baseUri) with non-empty html returns body with fragment (baseUri empty)")
    public void test_TC04_O2() {
        // no baseUri (empty) but non-empty HTML should parse exactly as fragment
        String html = "<p>test</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("<p>test</p>", doc.body().html());
    }

    @Test
    @DisplayName("TC05_O2: parse(html) with empty html returns empty body (empty html, empty baseUri)")
    public void test_TC05_O2() {
        // empty HTML and empty baseUri -> empty body
        String html = "";
        Document doc = Jsoup.parse(html);
        assertEquals("", doc.body().html());
    }

    @Test
    @DisplayName("TC06_O2: parse(html) with null html throws NullPointerException (html null)")
    public void test_TC06_O2() {
        // null HTML should cause NPE in no-baseUri overload
        assertThrows(NullPointerException.class, () -> Jsoup.parse((String) null));
    }

    @Test
    @DisplayName("TC07_O3: parse(html, parser) with non-empty html and custom parser uses parser.parseInput")
    public void test_TC07_O3() {
        // using xmlParser should respect XML structure and produce one <a> element
        String html = "<root><a/></root>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals(1, doc.select("a").size());
    }

    @Test
    @DisplayName("TC08_O3: parse(html, parser) with null html throws NullPointerException (html null)")
    public void test_TC08_O3() {
        // null HTML parameter should throw NPE in parser overload
        Parser parser = Parser.htmlParser();
        assertThrows(NullPointerException.class, () -> Jsoup.parse(null, parser));
    }

    @Test
    @DisplayName("TC09_O4: parse(html, baseUri, parser) with valid html, non-empty baseUri and parser resolves links with custom parser")
    public void test_TC09_O4() {
        // xmlParser with non-empty baseUri should resolve relative href to absolute URL
        String html = "<a href=\"/path\"></a>";
        String baseUri = "http://base";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        assertEquals("http://base/path", doc.select("a").first().absUrl("href"));
    }

    @Test
    @DisplayName("TC10_O4: parse(html, baseUri, parser) with null html throws NullPointerException (html null)")
    public void test_TC10_O4() {
        // null HTML should cause NPE before parser invocation
        String baseUri = "http://x/";
        Parser parser = Parser.xmlParser();
        assertThrows(NullPointerException.class, () -> Jsoup.parse(null, baseUri, parser));
    }
}