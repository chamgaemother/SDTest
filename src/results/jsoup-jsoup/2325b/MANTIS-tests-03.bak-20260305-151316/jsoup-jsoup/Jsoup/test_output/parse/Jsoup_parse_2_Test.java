package org.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.HttpStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC20: parse(InputStream, charsetName, baseUri) without Parser returns parsed Document and closes stream")
    public void test_TC20() throws IOException {
        // GIVEN a stream containing simple HTML, explicit charset, and a base URI
        byte[] htmlBytes = "<p>In</p>".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(htmlBytes);
        String charset = "UTF-8";
        String baseUri = "http://base/";

        // WHEN parsing via Jsoup.parse(InputStream, charsetName, baseUri)
        Document doc = Jsoup.parse(in, charset, baseUri);
        // Inline comment: B2→B5 branch reached because charsetName != null and no BOM

        // THEN the document should have the correct baseUri and body HTML
        Assertions.assertEquals("http://base/", doc.baseUri(), 
            "Expected baseUri to be preserved");
        Assertions.assertEquals("<p>In</p>", doc.body().html(),
            "Expected body HTML to match input content");

        // AND the input stream should be closed (IOException on read)
        Assertions.assertThrows(IOException.class, () -> in.read(),
            "Expected stream.read() to throw after stream is closed by parse");
    }

    @Test
    @DisplayName("TC21: parse(InputStream, null charsetName, baseUri) auto-detects charset and returns parsed Document")
    public void test_TC21() throws IOException {
        // GIVEN a stream starting with UTF-8 BOM followed by HTML, null charset triggers auto-detect
        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] content = "<div>BOM</div>".getBytes(StandardCharsets.UTF_8);
        byte[] inBytes = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, inBytes, 0, bom.length);
        System.arraycopy(content, 0, inBytes, bom.length, content.length);
        ByteArrayInputStream in = new ByteArrayInputStream(inBytes);
        String charset = null;
        String baseUri = "base";

        // WHEN parsing with null charset to enter BOM-detection branch
        Document doc = Jsoup.parse(in, charset, baseUri);
        // Inline comment: B3→B6 branch taken since charsetName is null and BOM present

        // THEN baseUri and body HTML match expectations
        Assertions.assertEquals("base", doc.baseUri(),
            "Expected baseUri to be preserved for null charset");
        Assertions.assertEquals("<div>BOM</div>", doc.body().html(),
            "Expected BOM to be stripped and content parsed correctly");

        // AND the input stream should be closed
        Assertions.assertThrows(IOException.class, () -> in.read(),
            "Expected stream.read() to throw after parse closed the stream");
    }

    @Test
    @DisplayName("TC22: parse(URL, timeoutMillis) throws HttpStatusException when HTTP status is non-OK")
    public void test_TC22() throws Exception {
        // GIVEN a URL and a mocked Connection that throws HttpStatusException on get()
        URL url = new URL("http://error.test");
        Connection mockCon = Mockito.mock(Connection.class);
        // inline: B7→B8 path to set timeout
        Mockito.when(mockCon.timeout(1000)).thenReturn(mockCon);
        Mockito.when(mockCon.get())
               .thenThrow(new HttpStatusException("404 not OK", 404, url.toString()));
        // Mock static HttpConnection.connect
        try (MockedStatic<HttpConnection> mocked = Mockito.mockStatic(HttpConnection.class)) {
            mocked.when(() -> HttpConnection.connect(url)).thenReturn(mockCon);

            // WHEN calling Jsoup.parse(url, timeout)
            // THEN expect HttpStatusException
            Assertions.assertThrows(org.jsoup.HttpStatusException.class, () -> Jsoup.parse(url, 1000),
                "Expected HttpStatusException to be thrown on non-OK HTTP status");
        }
    }

    @Test
    @DisplayName("TC23: parse(URL, timeoutMillis) throws UnsupportedMimeTypeException when response mime type unsupported")
    public void test_TC23() throws Exception {
        // GIVEN a URL and a mocked Connection that throws UnsupportedMimeTypeException on get()
        URL url = new URL("http://bad.mime");
        Connection mockCon = Mockito.mock(Connection.class);
        // inline: B7→B8 path to set timeout
        Mockito.when(mockCon.timeout(500)).thenReturn(mockCon);
        Mockito.when(mockCon.get())
               .thenThrow(new UnsupportedMimeTypeException("application/zip", null)); // Corrected constructor usage
        // Mock static HttpConnection.connect
        try (MockedStatic<HttpConnection> mocked = Mockito.mockStatic(HttpConnection.class)) {
            mocked.when(() -> HttpConnection.connect(url)).thenReturn(mockCon);

            // WHEN calling Jsoup.parse(url, timeout)
            // THEN expect UnsupportedMimeTypeException
            Assertions.assertThrows(UnsupportedMimeTypeException.class, () -> Jsoup.parse(url, 500),
                "Expected UnsupportedMimeTypeException on unsupported MIME type");
        }
    }
}