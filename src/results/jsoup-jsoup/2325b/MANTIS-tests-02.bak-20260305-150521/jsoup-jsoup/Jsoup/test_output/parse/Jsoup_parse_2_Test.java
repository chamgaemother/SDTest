package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(String, String, Parser) with XML parser returns elements preserved and retains provided baseUri")
    public void test_TC19() {
        String html = "<root><child>OK</child></root>";
        String baseUri = "http://example.org/";
        Parser xmlParser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, baseUri, xmlParser);
        assertEquals("OK", doc.select("child").first().text(), "The XML parser should preserve the <child> element text");
        assertEquals(baseUri, doc.baseUri(), "The document baseUri should match the provided baseUri");
    }

    @Test
    @DisplayName("parse(String, Parser) with HTML parser returns normalized HTML and default empty baseUri")
    public void test_TC20() {
        String html = "<div/>";
        Parser htmlParser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, htmlParser);
        assertEquals("<div></div>", doc.body().html(), "HTML parser should normalize <div/> to <div></div>");
        assertEquals("", doc.baseUri(), "Base URI should default to empty string when not provided");
    }

    @Test
    @DisplayName("parse(InputStream, String charsetName, String baseUri) with valid charset reads stream and sets baseUri")
    public void test_TC21() throws Exception {
        String inputHtml = "<p>ok</p>";
        byte[] bytes = inputHtml.getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(bytes);
        String charset = "UTF-8";
        String baseUri = "http://stream.example/";
        Document doc = Jsoup.parse(in, charset, baseUri);
        assertEquals("ok", doc.select("p").first().text(), "The <p> element text should be parsed correctly from stream");
        assertEquals(baseUri, doc.baseUri(), "The baseUri should be set to the provided URI");
    }

    @Test
    @DisplayName("parse(InputStream, String charsetName, String baseUri, Parser) with HTML parser reads and normalizes fragment")
    public void test_TC22() throws Exception {
        String inputHtml = "<span/>";
        byte[] bytes = inputHtml.getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(bytes);
        String charset = "UTF-8";
        String baseUri = "http://stream2.example/";
        Parser htmlParser = Parser.htmlParser();
        Document doc = Jsoup.parse(in, charset, baseUri, htmlParser);
        assertEquals("<span></span>", doc.body().html(), "HTML parser should normalize <span/> to <span></span> in stream parse");
        assertEquals(baseUri, doc.baseUri(), "Stream parse with parser should set baseUri to the provided URI");
    }
}