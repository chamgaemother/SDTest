package org.jsoup;

import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(String html) uses empty baseUri and returns Document with body content")
    public void test_TC09() {
        String html = "<div>Test</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("<div>Test</div>", doc.body().html(), "Body HTML should match input fragment");
        assertEquals("", doc.baseUri(), "Base URI should be empty string by default");
    }

    @Test
    @DisplayName("parseBodyFragment(String bodyHtml, String baseUri) returns Document fragment with preserved baseUri")
    public void test_TC10() {
        String frag = "<p>Frag</p>";
        String base = "http://bs/";
        Document doc = Jsoup.parseBodyFragment(frag, base);
        assertEquals("<p>Frag</p>", doc.body().html(), "Fragment HTML should match input");
        assertEquals(base, doc.baseUri(), "Base URI should be preserved as provided");
    }

    @Test
    @DisplayName("parseBodyFragment(String bodyHtml) uses empty baseUri and returns Document fragment")
    public void test_TC11() {
        String frag = "<span>X</span>";
        Document doc = Jsoup.parseBodyFragment(frag);
        assertEquals("<span>X</span>", doc.body().html(), "Fragment HTML should match input");
        assertEquals("", doc.baseUri(), "Base URI should be empty string by default");
    }

    @Test
    @DisplayName("parse(File file, null charsetName, String baseUri) loads HTML file and sets baseUri")
    public void test_TC12() throws IOException {
        Path tmpPath = Files.createTempFile("jsoupTest", ".html");
        tmpPath.toFile().deleteOnExit();
        String content = "<h1>Hdr</h1>";
        try (FileOutputStream out = new FileOutputStream(tmpPath.toFile())) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
        File f = tmpPath.toFile();
        String baseUri = "http://base/";
        Document doc = Jsoup.parse(f, null, baseUri);
        assertEquals(content, doc.body().html(), "Body HTML should match file content");
        assertEquals(baseUri, doc.baseUri(), "Base URI should be as provided");
    }

    @Test
    @DisplayName("parse(File file) throws IOException when file does not exist")
    public void test_TC13() {
        File f = new File("/nonexistent/none.html");
        assertThrows(IOException.class, () -> Jsoup.parse(f), "Parsing missing file should throw IOException");
    }

    @Test
    @DisplayName("parse(URL url, int timeoutMillis) throws MalformedURLException for non-HTTP protocol")
    public void test_TC14() {
        assertThrows(MalformedURLException.class, () -> {
            URL url = new URL("ftp://example.com/");
            Jsoup.parse(url, 1000);
        }, "Non-HTTP/HTTPS URL should throw MalformedURLException");
    }

    @Test
    @DisplayName("parse(InputStream in, String charsetName, String baseUri) loads stream and returns Document")
    public void test_TC15() throws IOException {
        String inputHtml = "<i>It</i>";
        InputStream in = new ByteArrayInputStream(inputHtml.getBytes(StandardCharsets.UTF_8));
        String baseUri = "http://xyz/";
        Document doc = Jsoup.parse(in, "UTF-8", baseUri);
        assertEquals(inputHtml, doc.body().html(), "Body HTML should match stream content");
        assertEquals(baseUri, doc.baseUri(), "Base URI should be preserved as provided");
    }
}