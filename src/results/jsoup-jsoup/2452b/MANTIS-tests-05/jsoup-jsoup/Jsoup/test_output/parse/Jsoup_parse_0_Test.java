package org.jsoup;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_0_Test {

    @Test
    @DisplayName("parse(String html, String baseUri) returns Document when html non-empty and baseUri non-empty")
    public void test_TC01_O1() {
        String html = "<p>Hello</p>";
        String baseUri = "http://example.com/";
        Document doc = Jsoup.parse(html, baseUri);
        assertEquals("<p>Hello</p>", doc.body().html());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(String html) uses empty baseUri and returns Document with body fragment")
    public void test_TC02_O1() {
        String html = "<div>Test</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("<div>Test</div>", doc.body().html());
        assertEquals("", doc.baseUri());
    }

    @Test
    @DisplayName("parse(String html, String baseUri, Parser parser) invokes parser.parseInput when parser non-null")
    public void test_TC03_O2() {
        String html = "<span>XML</span>";
        String baseUri = "";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, baseUri, parser);
        assertEquals("<span>XML</span>", doc.body().html());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(String html, Parser parser) invokes parser.parseInput with empty baseUri")
    public void test_TC04_O3() {
        String html = "<b>Bold</b>";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(html, parser);
        assertEquals("<b>Bold</b>", doc.body().html());
        assertEquals("", doc.baseUri());
    }

    @Test
    @DisplayName("parse(File file, String charsetName, String baseUri) loads file when file exists and charset valid")
    public void test_TC05_O4() throws IOException {
        File temp = File.createTempFile("jsoupTest", ".html");
        temp.deleteOnExit();
        try (FileWriter fw = new FileWriter(temp)) {
            fw.write("<p>File</p>");
        }
        String charset = "UTF-8";
        String baseUri = "http://file.base/";
        Document doc = Jsoup.parse(temp, charset, baseUri);
        assertEquals("<p>File</p>", doc.body().html());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    @DisplayName("parse(File file, String charsetName) loads file defaulting baseUri to file absolute path")
    public void test_TC06_O4() throws IOException {
        File temp = File.createTempFile("jsoupTest2", ".html");
        temp.deleteOnExit();
        try (FileWriter fw = new FileWriter(temp)) {
            fw.write("<p>Local</p>");
        }
        String charset = "UTF-8";
        Document doc = Jsoup.parse(temp, charset);
        assertEquals("<p>Local</p>", doc.body().html());
        assertEquals(temp.getAbsolutePath(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(File file) loads file defaulting charset null and baseUri to file absolute path")
    public void test_TC07_O4() throws IOException {
        File temp = File.createTempFile("jsoupTest3", ".html");
        temp.deleteOnExit();
        try (FileWriter fw = new FileWriter(temp)) {
            fw.write("<p>Auto</p>");
        }
        Document doc = Jsoup.parse(temp);
        assertEquals("<p>Auto</p>", doc.body().html());
        assertEquals(temp.getAbsolutePath(), doc.baseUri());
    }

    @Test
    @DisplayName("parse(URL url,int timeoutMillis) throws MalformedURLException when URL protocol non-http")
    public void test_TC08_O5() throws Exception {
        URL url = new URL("ftp://example.com");
        int timeout = 1000;
        assertThrows(MalformedURLException.class, () -> Jsoup.parse(url, timeout));
    }

    @Test
    @DisplayName("parse(URL url,int timeoutMillis) throws SocketTimeoutException when connection times out")
    public void test_TC09_O5() throws Exception {
        URL url = new URL("http://slow.example.com");
        int timeout = 1;
        try (MockedStatic<HttpConnection> mocked = Mockito.mockStatic(HttpConnection.class)) {
            org.jsoup.Connection fakeCon = Mockito.mock(org.jsoup.Connection.class);
            mocked.when(() -> HttpConnection.connect(url)).thenReturn(fakeCon);
            Mockito.when(fakeCon.timeout(timeout)).thenReturn(fakeCon);
            Mockito.when(fakeCon.get()).thenThrow(new SocketTimeoutException("timeout"));
            assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, timeout));
        }
    }

    @Test
    @DisplayName("parse(InputStream in,String charsetName,String baseUri) throws IOException on null stream")
    public void test_TC10_O6() {
        InputStream in = null;
        String charset = null;
        String baseUri = "";
        assertThrows(NullPointerException.class, () -> Jsoup.parse(in, charset, baseUri));
    }

    @Test
    @DisplayName("parseBodyFragment(String bodyHtml,String baseUri) returns Document fragment under empty baseUri branch")
    public void test_TC11_O7() {
        String fragment = "<i>Italics</i>";
        Document doc = Jsoup.parseBodyFragment(fragment);
        assertEquals("<i>Italics</i>", doc.body().html());
        assertEquals("", doc.baseUri());
    }
}