package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class Jsoup_parse_1_Test {

    @Test
    @DisplayName("TC09: parse(File, String) with null charsetName uses BOM/meta or defaults to UTF-8")
    public void test_TC09() throws IOException {
        // Branches: B0->B1->B4->B5: File overload, null charset triggers default
        Path temp = Files.createTempFile("jsoupTest", ".html");
        Files.write(temp, "<h1>Title</h1>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        String charset = null; // null to trigger default charset path
        Document doc = Jsoup.parse(file, charset);
        assertEquals("<h1>Title</h1>", doc.body().html());
    }

    @Test
    @DisplayName("TC10: parse(File, String, String, Parser) with custom Parser.xmlParser() applied")
    public void test_TC10() throws IOException {
        // Branches: B0->B2->B6->B7: File+parser overload, custom parser used
        Path temp = Files.createTempFile("xmlTest", ".xml");
        Files.write(temp, "<root>value</root>".getBytes(StandardCharsets.UTF_8));
        File file = temp.toFile();
        String charset = "UTF-8";
        String baseUri = "unused";
        Parser parser = Parser.xmlParser(); // use XML parser branch
        Document doc = Jsoup.parse(file, charset, baseUri, parser);
        assertEquals("value", doc.select("root").text());
    }

    @Test
    @DisplayName("TC11: parse(Path, String, String) with valid Path and explicit charsetName")
    public void test_TC11() throws IOException {
        // Branches: B0->B1->B3->B5: Path+charset+baseUri explicit path
        Path temp = Files.createTempFile("divTest", ".html");
        Files.write(temp, "<div>XY</div>".getBytes(StandardCharsets.UTF_8));
        String charset = "UTF-8";
        String baseUri = "http://base/";
        Document doc = Jsoup.parse(temp, charset, baseUri);
        assertEquals("<div>XY</div>", doc.body().html());
    }

    @Test
    @DisplayName("TC12: parse(Path, String) with null charsetName uses file path as baseUri")
    public void test_TC12() throws IOException {
        // Branches: B0->B1->B3->B4: Path+null charset, default baseUri = path.toAbsolutePath().toString()
        Path temp = Files.createTempFile("pTest", ".html");
        Files.write(temp, "<p>A</p>".getBytes(StandardCharsets.UTF_8));
        String charset = null; // null triggers default charset path
        Document doc = Jsoup.parse(temp, charset);
        assertEquals("<p>A</p>", doc.body().html());
    }

    @Test
    @DisplayName("TC13: parse(Path) default overload uses UTF-8 and file path as baseUri")
    public void test_TC13() throws IOException {
        // Branches: B0->B1->B3->B4: Path default overload, charset null and baseUri path
        Path temp = Files.createTempFile("spanTest", ".html");
        Files.write(temp, "<span>z</span>".getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(temp);
        assertEquals("<span>z</span>", doc.body().html());
    }

    @Test
    @DisplayName("TC14: parse(InputStream, String, String) reads and closes stream properly")
    public void test_TC14() throws IOException {
        // Branches: B0->B2->B5->B6: InputStream overload without parser
        byte[] bytes = "<b>B</b>".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        String charset = "UTF-8";
        String baseUri = "";
        Document doc = Jsoup.parse(in, charset, baseUri);
        assertEquals("<b>B</b>", doc.body().html());
        // stream closed by loader, reading further should give -1
        assertEquals(-1, in.read());
    }

    @Test
    @DisplayName("TC15: parse(InputStream, String, String, Parser) uses custom parser on stream")
    public void test_TC15() throws IOException {
        // Branches: B0->B2->B7->B8: InputStream+parser overload
        byte[] bytes = "<x>Y</x>".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        String charset = "UTF-8";
        String baseUri = "unused";
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(in, charset, baseUri, parser);
        assertEquals("Y", doc.select("x").text());
    }

    @Test
    @DisplayName("TC16: parse(URL, int) with malformed URL throws MalformedURLException")
    public void test_TC16() {
        // Branches: B0->B1->B3: URL overload, invalid protocol leads to exception
        assertThrows(MalformedURLException.class, () -> {
            URL url = new URL("ftp://invalid"); // creation succeeds
            Jsoup.parse(url, 1000); // should throw MalformedURLException for protocol
        });
    }
}