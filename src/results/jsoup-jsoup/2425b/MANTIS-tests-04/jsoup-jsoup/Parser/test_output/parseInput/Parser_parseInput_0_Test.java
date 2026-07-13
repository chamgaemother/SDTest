package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.helper.Validate;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.TagSet;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_0_Test {

    // A stub TreeBuilder that can return a preset Document or throw a runtime exception
    static class StubTreeBuilder extends TreeBuilder {
        private final Document toReturn;
        private final RuntimeException toThrow;
        private final Parser ownerParser;

        StubTreeBuilder(Document toReturn) {
            this.toReturn = toReturn;
            this.toThrow = null;
            this.ownerParser = null;
        }

        StubTreeBuilder(RuntimeException toThrow) {
            this.toReturn = null;
            this.toThrow = toThrow;
            this.ownerParser = null;
        }

        @Override
        public Document parse(Reader input, String baseUri, Parser parser) {
            if (toThrow != null) throw toThrow;
            return toReturn;
        }

        @Override
        public List<Node> parseFragment(Reader reader, Element context, String baseUri, Parser parser) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ParseSettings defaultSettings() {
            return new ParseSettings(true, true);
        }

        @Override
        public TagSet defaultTagSet() {
            return TagSet.html();
        }

        @Override
        public String defaultNamespace() {
            return NamespaceHtml;
        }

        @Override
        public TreeBuilder newInstance() {
            if (toThrow != null) return new StubTreeBuilder(toThrow);
            return new StubTreeBuilder(toReturn);
        }

        @Override
        public void process(org.jsoup.parser.Token token) {
            // implement method logic
        }
    }

    // A Reader stub that throws IOException on any read
    static class IOStubReader extends Reader {
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            throw new IOException("stub I/O error");
        }
        @Override public void close() throws IOException {}
    }

    @Test
    @DisplayName("parseInput(String, String) returns Document for valid HTML and baseUri")
    void test_TC01_O1() {
        // branch-normal: stub builder returns dummy doc
        Document dummy = Document.createShell("");
        Parser parser = new Parser(new StubTreeBuilder(dummy));
        String html = "<p>Hi</p>";
        String baseUri = "http://x/";
        Document result = parser.parseInput(html, baseUri);
        assertSame(dummy, result, "Expected the stub document to be returned unchanged");
    }

    @Test
    @DisplayName("parseInput(String, String) throws UncheckedIOException when Reader.read() fails")
    void test_TC02_O1() {
        // branch-exception: StringReader wrapper around IOStubReader to simulate Reader exception
        Parser parser = new Parser(new org.jsoup.parser.HtmlTreeBuilder());
        Reader faulty = new IOStubReader();
        // call parseInput(Reader,String) via reflection since overload used when html=null isn't relevant
        assertThrows(UncheckedIOException.class, () -> parser.parseInput(faulty, "http://x/"));
        // lock should be unlocked after exception
        ReentrantLock lock = extractLock(parser);
        assertFalse(lock.isLocked(), "Lock must be released after exception");
    }

    @Test
    @DisplayName("parseInput(String, String) throws NullPointerException when html is null")
    void test_TC03_O1() {
        // branch-null-input: null html should immediately NPE
        Parser parser = new Parser(new org.jsoup.parser.HtmlTreeBuilder());
        assertThrows(NullPointerException.class, () -> parser.parseInput((String) null, "http://x/"));
    }

    @Test
    @DisplayName("parseInput(String, String) returns Document for empty HTML string")
    void test_TC04_O1() {
        // boundary-empty: empty html still reaches treeBuilder.parse
        Document emptyDoc = Document.createShell("");
        Parser parser = new Parser(new StubTreeBuilder(emptyDoc));
        Document result = parser.parseInput("", "http://x/");
        assertSame(emptyDoc, result);
    }

    @Test
    @DisplayName("parseInput(Reader, String) returns Document for valid Reader and baseUri")
    void test_TC05_O2() {
        // branch-normal: stub builder returns preset doc
        Document docR = Document.createShell("");
        Parser parser = new Parser(new StubTreeBuilder(docR));
        Reader input = new StringReader("<a/>");
        String baseUri = "http://u/";
        Document result = parser.parseInput(input, baseUri);
        assertSame(docR, result);
    }

    @Test
    @DisplayName("parseInput(Reader, String) throws NullPointerException when Reader is null")
    void test_TC06_O2() {
        // branch-null-input: null reader should NPE
        Parser parser = new Parser(new org.jsoup.parser.HtmlTreeBuilder());
        assertThrows(NullPointerException.class, () -> parser.parseInput((Reader) null, "http://u/"));
    }

    @Test
    @DisplayName("parseInput(Reader, String) throws NullPointerException when baseUri is null")
    void test_TC07_O2() {
        // branch-null-baseUri: null baseUri leads to NPE inside parse
        Document dummy = Document.createShell("");
        Parser parser = new Parser(new StubTreeBuilder(dummy));
        Reader input = new StringReader("<p/>");
        assertThrows(NullPointerException.class, () -> parser.parseInput(input, null));
    }

    @Test
    @DisplayName("parseInput(Reader, String) propagates RuntimeException from treeBuilder.parse")
    void test_TC08_O2() {
        // branch-exception: stub builder throws IllegalStateException
        IllegalStateException ex = new IllegalStateException("stub");
        Parser parser = new Parser(new StubTreeBuilder(ex));
        Reader input = new StringReader("x");
        assertThrows(IllegalStateException.class, () -> parser.parseInput(input, "u"));
    }

    @Test
    @DisplayName("parseInput(Reader, String) returns Document when Reader has no content")
    void test_TC09_O2() {
        // boundary-empty-reader: empty reader but stub builder returns emptyDoc
        Document emptyDoc = Document.createShell("");
        Parser parser = new Parser(new StubTreeBuilder(emptyDoc));
        Reader input = new StringReader("");
        Document result = parser.parseInput(input, "http://v/");
        assertSame(emptyDoc, result);
    }

    @Test
    @DisplayName("parseInput(Reader, String) closes lock even when treeBuilder.parse throws")
    void test_TC10_O2() {
        // lock-finally: stub builder throws custom exception, lock must unlock in finally
        RuntimeException custom = new RuntimeException("custom");
        Parser parser = new Parser(new StubTreeBuilder(custom));
        Reader input = new StringReader("z");
        assertThrows(RuntimeException.class, () -> parser.parseInput(input, "uri"));
        ReentrantLock lock = extractLock(parser);
        assertFalse(lock.isLocked(), "Lock should be unlocked even after exception");
    }

    // Helper to extract private lock via reflection
    private static ReentrantLock extractLock(Parser parser) {
        try {
            Field lockField = Parser.class.getDeclaredField("lock");
            lockField.setAccessible(true);
            return (ReentrantLock) lockField.get(parser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}