package org.jsoup;

import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("parse(String html) uses default empty baseUri and returns Document with correct content and empty baseUri")
    public void test_TC11() {
        String html = "<div>Hello</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("<div>Hello</div>", doc.body().html(), "Body HTML should match input fragment");
        assertEquals("", doc.baseUri(), "Default baseUri should be empty string");
    }

    @Test
    @DisplayName("parse(String html, Parser parser) uses provided parser and empty baseUri returns parsed Document")
    public void test_TC12() {
        String xml = "<root>xyz</root>";
        Parser xmlParser = Parser.xmlParser();
        Document doc = Jsoup.parse(xml, xmlParser);
        assertEquals("xyz", doc.select("root").text(), "XML parser should extract text from root element");
        assertEquals("", doc.baseUri(), "Default baseUri should be empty string when using overload");
    }

    @Test
    @DisplayName("parse(File file, String charsetName) loads file with explicit charset and uses file path as baseUri")
    public void test_TC13() throws IOException {
        Path p = Files.createTempFile("jsoup", ".html");
        try {
            Files.write(p, "<span>ABC</span>".getBytes(StandardCharsets.UTF_8));
            File file = p.toFile();
            Document doc = Jsoup.parse(file, "UTF-8");
            assertEquals("ABC", doc.select("span").text(), "Should read span text ABC");
            assertEquals(file.getAbsolutePath(), doc.baseUri(), "BaseUri should be file absolute path");
        } finally {
            Files.deleteIfExists(p);
        }
    }

    @Test
    @DisplayName("parse(File file) auto-detects charset and uses file path as baseUri")
    public void test_TC14() throws IOException {
        Path p = Files.createTempFile("jsoup2", ".html");
        try {
            Files.write(p, "<h2>XYZ</h2>".getBytes(StandardCharsets.UTF_8));
            File file = p.toFile();
            Document doc = Jsoup.parse(file);
            assertEquals("XYZ", doc.select("h2").text(), "Should read h2 text XYZ with default charset UTF-8");
            assertEquals(file.getAbsolutePath(), doc.baseUri(), "BaseUri should be file absolute path when no charset provided");
        } finally {
            Files.deleteIfExists(p);
        }
    }

    @Test
    @DisplayName("parse(File file, String charsetName, String baseUri, Parser parser) uses custom parser and provided baseUri")
    public void test_TC15() throws IOException {
        Path p = Files.createTempFile("jsoup3", ".xml");
        try {
            Files.write(p, "<elem>1</elem>".getBytes(StandardCharsets.UTF_8));
            File file = p.toFile();
            Parser xmlParser = Parser.xmlParser();
            String baseUri = "http://test/";
            Document doc = Jsoup.parse(file, "UTF-8", baseUri, xmlParser);
            assertEquals("1", doc.select("elem").text(), "XML parser should extract elem text 1");
            assertEquals(baseUri, doc.baseUri(), "BaseUri should match provided baseUri");
        } finally {
            Files.deleteIfExists(p);
        }
    }

    @Test
    @DisplayName("parse(Path path) auto-detects charset and uses path.toAbsolutePath as baseUri")
    public void test_TC16() throws IOException {
        Path p = Files.createTempFile("jsoup4", ".html");
        try {
            Files.write(p, "<b>Hi</b>".getBytes(StandardCharsets.UTF_8));
            Document doc = Jsoup.parse(p);
            assertEquals("Hi", doc.select("b").text(), "Should read b text Hi");
            assertEquals(p.toAbsolutePath().toString(), doc.baseUri(), "BaseUri should be absolute path string of Path");
        } finally {
            Files.deleteIfExists(p);
        }
    }

    @Test
    @DisplayName("parse(InputStream in, String charsetName, String baseUri, Parser parser) uses alternate parser on stream")
    public void test_TC17() throws IOException {
        byte[] data = "<item>5</item>".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(data)) {
            Parser xmlParser = Parser.xmlParser();
            String baseUri = "http://u/";
            Document doc = Jsoup.parse(in, "UTF-8", baseUri, xmlParser);
            assertEquals("5", doc.select("item").text(), "XML parser should extract item text 5");
            assertEquals(baseUri, doc.baseUri(), "BaseUri should match provided baseUri for stream overload");
        }
    }

    @Test
    @DisplayName("parse(URL url, int timeoutMillis) throws SocketTimeoutException on timeout")
    public void test_TC18() throws Exception {
        URL url = new URL("http://example.com");
        try (MockedStatic<HttpConnection> mockStatic = Mockito.mockStatic(HttpConnection.class)) {
            Connection mockCon = mock(Connection.class);
            when(mockCon.timeout(any(Integer.class))).thenReturn(mockCon);
            when(mockCon.get()).thenThrow(new SocketTimeoutException("timeout"));
            mockStatic.when(() -> HttpConnection.connect(eq(url))).thenReturn(mockCon);
            assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, 10), "Should throw SocketTimeoutException on timeout");
        }
    }

    @Test
    @DisplayName("parse(File file, String charsetName, String baseUri) with invalid charsetName throws IOException")
    public void test_TC19() throws IOException {
        Path p = Files.createTempFile("jsoup5", ".html");
        try {
            Files.write(p, "<p>Oops</p>".getBytes(StandardCharsets.UTF_8));
            File file = p.toFile();
            assertThrows(IOException.class, () -> Jsoup.parse(file, "NO_SUCH_CHARSET", "http://x"), "Should throw IOException for unsupported charset");
        } finally {
            Files.deleteIfExists(p);
        }
    }
}