package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC07: parse(File, String charsetName, String baseUri) reads UTF-8 file and resolves relative link")
    public void test_TC07() throws Exception {
        // Given: temp file with a relative href, explicit UTF-8 charset to follow B11→B12 branch
        Path temp = Files.createTempFile("tc07", ".html");
        String html = "<a href=\"/x\">X</a>";
        Files.write(temp, html.getBytes("UTF-8"));
        File file = temp.toFile();
        // When
        Document doc = Jsoup.parse(file, "UTF-8", "http://base/");
        // Then: absUrl resolves '/x' against baseUri
        String absHref = doc.select("a").first().absUrl("href");
        assertEquals("http://base/x", absHref);
    }

    @Test
    @DisplayName("TC08: parse(File, null charsetName, String baseUri) auto-detects charset when null is passed")
    public void test_TC08() throws Exception {
        // Given: temp file with meta charset and body, charsetName null triggers auto-detect (B11→B12)
        Path temp = Files.createTempFile("tc08", ".html");
        String html = "<meta charset=\"UTF-8\"><p>Auto</p>";
        Files.write(temp, html.getBytes("UTF-8"));
        File file = temp.toFile();
        // When
        Document doc = Jsoup.parse(file, null, "http://irrelevant/");
        // Then: body html preserved
        assertEquals("<p>Auto</p>", doc.body().html());
    }

    @Test
    @DisplayName("TC09: parse(File) default overload infers baseUri and charset null")
    public void test_TC09() throws Exception {
        // Given: temp file without specifying charset or baseUri (B13→B14)
        Path temp = Files.createTempFile("tc09", ".html");
        String html = "<div>Default</div>";
        Files.write(temp, html.getBytes());
        File file = temp.toFile();
        // When
        Document doc = Jsoup.parse(file);
        // Then: body html matches
        assertEquals("<div>Default</div>", doc.body().html());
    }

    @Test
    @DisplayName("TC10: parse(File, String charsetName, String baseUri, Parser) with xmlParser preserves case")
    public void test_TC10() throws Exception {
        // Given: temp file and xmlParser to preserve tag name casing (B15→B16)
        Path temp = Files.createTempFile("tc10", ".xml");
        String html = "<Tag>case</Tag>";
        Files.write(temp, html.getBytes("UTF-8"));
        File file = temp.toFile();
        // When
        Document doc = Jsoup.parse(file, "UTF-8", "", Parser.xmlParser());
        // Then: tagName preserved
        assertEquals("Tag", doc.select("Tag").first().tagName());
    }

    @Test
    @DisplayName("TC11: parse(Path, String charsetName, String baseUri) reads file via Path")
    public void test_TC11() throws Exception {
        // Given: Path input with explicit charset and baseUri (B17→B18)
        Path path = Files.createTempFile("tc11", ".html");
        String html = "<span>Path</span>";
        Files.write(path, html.getBytes("UTF-8"));
        // When
        Document doc = Jsoup.parse(path, "UTF-8", "http://u/");
        // Then: body html matches
        assertEquals("<span>Path</span>", doc.body().html());
    }

    @Test
    @DisplayName("TC12: parse(Path, null charsetName) infers charset and uses path as baseUri")
    public void test_TC12() throws Exception {
        // Given: Path input, charsetName null triggers default charset detection (B17→B19)
        Path path = Files.createTempFile("tc12", ".html");
        String html = "<p>NullC</p>";
        Files.write(path, html.getBytes());
        // When
        Document doc = Jsoup.parse(path, null);
        // Then: body html matches
        assertEquals("<p>NullC</p>", doc.body().html());
    }

    @Test
    @DisplayName("TC13: parse(Path) default overload reads file via Path")
    public void test_TC13() throws Exception {
        // Given: Path input default overload (B20→B21)
        Path path = Files.createTempFile("tc13", ".html");
        String html = "<b>Def</b>";
        Files.write(path, html.getBytes());
        // When
        Document doc = Jsoup.parse(path);
        // Then: body html matches
        assertEquals("<b>Def</b>", doc.body().html());
    }

    @Test
    @DisplayName("TC14: parse(Path, String charsetName, String baseUri, Parser) xmlParser preserves tag names")
    public void test_TC14() throws Exception {
        // Given: xmlParser on Path input to keep element case (B22→B23)
        Path path = Files.createTempFile("tc14", ".xml");
        String html = "<Xml>one</Xml>";
        Files.write(path, html.getBytes("UTF-8"));
        // When
        Document doc = Jsoup.parse(path, "UTF-8", "", Parser.xmlParser());
        // Then: tagName preserved
        assertEquals("Xml", doc.select("Xml").first().tagName());
    }

    @Test
    @DisplayName("TC15: parse(InputStream, null charsetName, String baseUri) closes stream and parses body fragment")
    public void test_TC15() throws Exception {
        // Given: InputStream and null charset triggers default (B24→B25)
        final String content = "<p>Stream</p>";
        class TrackInput extends FilterInputStream {
            boolean closed = false;
            TrackInput(InputStream in) { super(in); }
            @Override public void close() throws IOException { closed = true; super.close(); }
        }
        TrackInput in = new TrackInput(new ByteArrayInputStream(content.getBytes()));
        // When
        Document doc = Jsoup.parse(in, null, "");
        // Then: body html matches and stream closed
        assertEquals("<p>Stream</p>", doc.body().html());
        assertTrue(in.closed, "InputStream should be closed after parsing");
    }

    @Test
    @DisplayName("TC16: parse(InputStream, String charsetName, String baseUri, Parser) xmlParser handles fragment")
    public void test_TC16() throws Exception {
        // Given: xmlParser on InputStream for fragment parsing (B26→B27)
        String content = "<Fragment/>";
        InputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
        // When
        Document doc = Jsoup.parse(in, "UTF-8", "", Parser.xmlParser());
        // Then: fragment element tagName
        assertEquals("Fragment", doc.select("Fragment").first().tagName());
    }

    @Test
    @DisplayName("TC17: parse(File, String invalidCharset, String baseUri) throws IOException for invalid charset")
    public void test_TC17() throws Exception {
        // Given: temp file with content and invalid charset leads to load error (B11→Berror)
        Path temp = Files.createTempFile("tc17", ".html");
        Files.write(temp, "<p>X</p>".getBytes("UTF-8"));
        File file = temp.toFile();
        // When / Then: invalid charset should cause IOException
        assertThrows(IOException.class, () -> Jsoup.parse(file, "INVALID-CHARSET", ""));
    }
}