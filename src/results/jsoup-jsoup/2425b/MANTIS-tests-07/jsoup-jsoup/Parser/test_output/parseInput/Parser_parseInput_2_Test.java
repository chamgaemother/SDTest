package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.concurrent.locks.ReentrantLock;
import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("parseInput(Reader,String) releases lock and propagates IllegalStateException from TreeBuilder.parse")
    public void test_TC09() throws Exception {
        // Arrange a stub TreeBuilder that always throws to drive exception path and lock release
        TreeBuilder stub = new TreeBuilder() {
            @Override public Document parse(Reader in, String uri, Parser parser) {
                throw new IllegalStateException("stub-fail");
            }
            @Override public void process(org.jsoup.parser.Token token) {} // Implementing abstract method
            @Override public TreeBuilder newInstance() { return this; }
            @Override public ParseSettings defaultSettings() { return ParseSettings.preserveCase(); }
            @Override public TagSet defaultTagSet() { return TagSet.Html(); }
        };
        Parser parser = new Parser(stub);
        Reader reader = new StringReader("<html></html>");

        // Act & Assert: exception is thrown and after call lock must be released
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(reader, "http://example.com"));
        assertEquals("stub-fail", ex.getMessage());

        // Reflection to access private lock field and verify it's not held
        Field lockField = Parser.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        ReentrantLock lock = (ReentrantLock) lockField.get(parser);
        assertFalse(lock.isLocked(), "Lock should be released after exception");
    }

    @Test
    @DisplayName("parseInput(String,String) releases lock and propagates IllegalStateException from TreeBuilder.parse")
    public void test_TC10() throws Exception {
        // Arrange a stub TreeBuilder that throws to cover string-overload path and ensure lock released
        TreeBuilder stub = new TreeBuilder() {
            @Override public Document parse(Reader in, String uri, Parser parser) {
                throw new IllegalStateException("stub-fail-str");
            }
            @Override public void process(org.jsoup.parser.Token token) {} // Implementing abstract method
            @Override public TreeBuilder newInstance() { return this; }
            @Override public ParseSettings defaultSettings() { return ParseSettings.preserveCase(); }
            @Override public TagSet defaultTagSet() { return TagSet.Html(); }
        };
        Parser parser = new Parser(stub);
        String html = "<p>x</p>";

        // Act & Assert: invoking string overload should propagate and release lock
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> parser.parseInput(html, "http://example.com"));
        assertEquals("stub-fail-str", ex.getMessage());

        // Verify lock is not held via reflection
        Field lockField = Parser.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        ReentrantLock lock = (ReentrantLock) lockField.get(parser);
        assertFalse(lock.isLocked(), "Lock should be released after exception in string overload");
    }

    @Test
    @DisplayName("parseInput(Reader,null) via Reader overload throws NullPointerException inside TreeBuilder.parse")
    public void test_TC11() throws Exception {
        // Using real HtmlTreeBuilder and null baseUri to trigger NPE inside parse
        Parser parser = new Parser(new HtmlTreeBuilder());
        Reader reader = new StringReader("<div>t</div>");
        String baseUri = null;

        // Act & Assert: parsing with null baseUri should cause NPE and release lock
        assertThrows(NullPointerException.class,
            () -> parser.parseInput(reader, baseUri));

        // Confirm lock is not held after exception
        Field lockField = Parser.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        ReentrantLock lock = (ReentrantLock) lockField.get(parser);
        assertFalse(lock.isLocked(), "Lock should be released after NullPointerException");
    }
}