package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseInput_1_Test {
    // Stub TreeBuilder for tests
    static class StubTreeBuilder extends TreeBuilder {
        private final Document toReturn;
        StubTreeBuilder(Document toReturn) {
            this.toReturn = toReturn;
        }
        @Override
        public Document parse(Reader in, String baseUri, Parser parser) {
            return toReturn;
        }
        @Override
        public List<Node> parseFragment(Reader in, Element context, String baseUri, Parser parser) {
            throw new UnsupportedOperationException();
        }
        @Override
        public ParseSettings defaultSettings() {
            return new ParseSettings(true, true);
        }
        @Override
        public TreeBuilder newInstance() {
            return new StubTreeBuilder(toReturn);
        }
        @Override
        public TagSet defaultTagSet() {
            return TagSet.html();
        }
        @Override
        public String defaultNamespace() {
            return "";
        }
        @Override
        public void initialiseParse(Reader in, String baseUri, Parser parser) {
            // no-op
        }
        // Implementing the missing process method correctly with matching signature
        @Override
        public void process(Token token) {
            // no-op for stub
        }
    }

    @Test
    @DisplayName("parseInput(String, String) throws NullPointerException when baseUri is null")
    public void test_TC11() {
        // Path B0→B1→B2→B6: html non-null, baseUri null triggers NPE before parsing
        Parser parser = new Parser(new StubTreeBuilder(null));
        String html = "<p>test</p>";
        String baseUri = null;
        assertThrows(NullPointerException.class, () -> parser.parseInput(html, baseUri));
    }

    @Test
    @DisplayName("parseInput(String, String) returns null Document when TreeBuilder.parse returns null")
    public void test_TC12() {
        // Path B0→B1→B2→B4→B6: valid inputs, stub returns null → method returns null
        Document toReturn = null;
        StubTreeBuilder stub = new StubTreeBuilder(toReturn);
        Parser parser = new Parser(stub);
        String html = "<tag/>";
        String baseUri = "http://test/";
        Document result = parser.parseInput(html, baseUri);
        assertNull(result, "Expected null when stubbed TreeBuilder.parse returns null");
    }

    @Test
    @DisplayName("parseInput(String, String) unlocks lock after normal return")
    public void test_TC13() throws Exception {
        // Path B0→B1→B2→B4→B6: normal parse with delay, ensure lock is released
        Document dummy = Document.createShell("http://x/");
        // slow stub: sleeps then returns dummy
        StubTreeBuilder slowStub = new StubTreeBuilder(dummy) {
            @Override
            public Document parse(Reader in, String baseUri, Parser parser) {
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                return super.parse(in, baseUri, parser);
            }
        };
        Parser parser = new Parser(slowStub);
        String html = "<p>OK</p>";
        String baseUri = "http://x/";
        Document result = parser.parseInput(html, baseUri);
        // verify correct return
        assertSame(dummy, result, "Expected the dummy Document returned by stub");
        // use reflection to extract private lock field and check it's unlocked
        Field lockField = Parser.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        ReentrantLock lock = (ReentrantLock) lockField.get(parser);
        assertFalse(lock.isLocked(), "Lock should be released after parseInput returns");
    }
}