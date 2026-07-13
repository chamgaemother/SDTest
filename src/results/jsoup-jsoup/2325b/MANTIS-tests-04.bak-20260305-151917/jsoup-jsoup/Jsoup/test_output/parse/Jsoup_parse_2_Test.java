package org.jsoup;

import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Jsoup_parse_2_Test {

    @Test
    @DisplayName("TC18: parseBodyFragment(bodyHtml) uses default empty baseUri and returns Document with fragment in body")
    public void test_TC18() {
        String fragment = "<span>foo</span>";
        Document doc = Jsoup.parseBodyFragment(fragment);
        assertEquals(fragment, doc.body().html());
    }

    @Test
    @DisplayName("TC19: parseBodyFragment(bodyHtml, baseUri) uses provided baseUri and resolves relative links in fragment")
    public void test_TC19() {
        String fragment = "<a href=\"page.html\">x</a>";
        String base = "http://example.com/path/";
        Document doc = Jsoup.parseBodyFragment(fragment, base);
        String resolved = doc.select("a").first().attr("href");
        assertEquals("http://example.com/path/page.html", resolved,
                "Relative href should be resolved against the provided baseUri");
    }

    @Test
    @DisplayName("TC20: parse(html, parser) invokes Parser.parseInput exactly once with empty baseUri")
    public void test_TC20() {
        final List<String> recordedInputs = new ArrayList<>();
        Parser stub = new Parser() {
            public Parser() {}
            @Override
            public Document parseInput(String html, String baseUri) {
                recordedInputs.add(html + "|" + baseUri);
                return Parser.htmlParser().parseInput(html, baseUri);
            }
        };
        String html = "<div>test</div>";
        Document doc = Jsoup.parse(html, stub);
        assertEquals(1, recordedInputs.size(), "parseInput should be called exactly once");
        assertEquals(html + "|", recordedInputs.get(0),
                "Parser.parseInput should receive the html and empty baseUri");
        assertEquals("", doc.baseUri(), "Resulting Document should have empty baseUri");
    }

    @Test
    @DisplayName("TC21: parse(html, baseUri, parser) propagates runtime exception from custom parser")
    public void test_TC21() {
        Parser errorParser = new Parser() {
            public Parser() {}
            @Override
            public Document parseInput(String html, String baseUri) {
                throw new RuntimeException("fail");
            }
        };
        String html = "<p/>";
        String base = "http://x/";
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> Jsoup.parse(html, base, errorParser));
        assertEquals("fail", ex.getMessage(), "Exception message should propagate from parser");
    }

    @Test
    @DisplayName("TC22: parse(URL, timeoutMillis) throws SocketTimeoutException when connection exceeds timeout")
    public void test_TC22() {
        try {
            URL url = new URL("http://127.0.0.1:9/");
            int timeout = 100;
            assertThrows(SocketTimeoutException.class, () -> Jsoup.parse(url, timeout),
                    "A SocketTimeoutException should be thrown when the connection exceeds the timeout");
        } catch (IOException e) {
            fail("Setup of URL should not fail: " + e.getMessage());
        }
    }
}