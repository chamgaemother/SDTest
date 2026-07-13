package org.jsoup;

import org.jsoup.parser.Parser;
import org.jsoup.helper.DataUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("parse(html=null, parser non-null) throws IllegalArgumentException for null html with Parser overload")
    public void test_TC27() {
        // html is null to trigger IllegalArgumentException in Parser.parseInput branch for null html
        String html = null;
        Parser parser = Parser.htmlParser();
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parse(html, parser);
        });
    }

    @Test
    @DisplayName("parse(html non-empty, baseUri non-empty, parser=null) throws IllegalArgumentException for null parser")
    public void test_TC28() {
        // parser is null to trigger IllegalArgumentException before parsing
        String html = "<p>Y</p>";
        String baseUri = "http://x/";
        Parser parser = null;
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parse(html, baseUri, parser);
        });
    }

    @Test
    @DisplayName("parse(File, charsetName invalid, baseUri) throws IOException for unsupported charset")
    public void test_TC29() throws IOException {
        // provide a real temporary file with valid HTML but invalid charset to trigger IOException in DataUtil.load
        File temp = File.createTempFile("jsoup", ".html");
        temp.deleteOnExit();
        Files.write(temp.toPath(), "<p>Q</p>".getBytes("UTF-8"));
        String charset = "INVALID-CHARSET";
        String baseUri = "u";
        assertThrows(IOException.class, () -> {
            Jsoup.parse(temp, charset, baseUri);
        });
    }

    @Test
    @DisplayName("parse(Path, charsetName invalid) throws IOException for invalid charset in Path overload")
    public void test_TC30() throws IOException {
        // Path exists with simple HTML but invalid charset triggers IOException
        Path p = Files.createTempFile("jsoup", ".html");
        p.toFile().deleteOnExit();
        Files.write(p, "<div>Z</div>".getBytes("UTF-8"));
        String charset = "NO-SUCH-CHARSET";
        assertThrows(IOException.class, () -> {
            Jsoup.parse(p, charset);
        });
    }

    @Test
    @DisplayName("parse(InputStream, charsetName invalid, baseUri) throws IOException for unsupported charset on stream")
    public void test_TC31() {
        // InputStream over HTML bytes but invalid charset triggers IOException in DataUtil.load
        byte[] data = "<p>S</p>".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        String charset = "BAD-CHARSET";
        String baseUri = "http://b/";
        assertThrows(IOException.class, () -> {
            Jsoup.parse(in, charset, baseUri);
        });
    }

    @Test
    @DisplayName("parse(InputStream, charsetName valid, baseUri, parser=null) throws IllegalArgumentException for null parser")
    public void test_TC32() {
        // parser null on overload with parser parameter triggers IllegalArgumentException
        InputStream in = new ByteArrayInputStream("<x/>".getBytes());
        String charset = null; // null charset is acceptable, but null parser should cause IAE
        String baseUri = "u";
        Parser parser = null;
        assertThrows(IllegalArgumentException.class, () -> {
            Jsoup.parse(in, charset, baseUri, parser);
        });
    }
}