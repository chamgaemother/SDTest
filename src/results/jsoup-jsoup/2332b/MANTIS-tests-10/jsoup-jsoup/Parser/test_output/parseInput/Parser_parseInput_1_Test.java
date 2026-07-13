package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jspecify.annotations.Nullable;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;
import org.jsoup.parser.HtmlTreeBuilder;
import org.jsoup.parser.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Parser.parseInput methods based on provided scenarios.
 */
public class Parser_parseInput_1_Test {

    @Test
    @DisplayName("TC08: Reader overload propagates IllegalArgumentException from treeBuilder.parse")
    public void test_TC08() {
        // Inline fake TreeBuilder that always throws IllegalArgumentException in parse → covers path B0→B3→B6
        abstract class FakeTreeBuilderThrowingIllegalArgument extends TreeBuilder {
            @Override
            public ParseSettings defaultSettings() {
                // use real default settings for minimal setup
                return new HtmlTreeBuilder().defaultSettings();
            }

            @Override
            public Document parse(Reader in, String baseUri, Parser parser) {
                // scenario: throw IllegalArgumentException to propagate
                throw new IllegalArgumentException("bad arg");
            }

            @Override
            public List<Node> parseFragment(Reader in, @Nullable Element ctxt, String baseUri, Parser parser) {
                return null;
            }

            @Override
            protected void process(Token token) { /* implementation */ }
        }

        Parser p = new Parser(new FakeTreeBuilderThrowingIllegalArgument());
        Reader in = new StringReader("<div>x</div>");
        String baseUri = "uri";

        // Expect IllegalArgumentException with message "bad arg"
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> p.parseInput(in, baseUri));
        assertEquals("bad arg", ex.getMessage());
    }

    @Test
    @DisplayName("TC09: Reader overload returns null when treeBuilder.parse returns null Document")
    public void test_TC09() {
        // Inline fake TreeBuilder that returns null Document → covers path B0→B3→B5
        abstract class FakeTreeBuilderReturningNullDocument extends TreeBuilder {
            @Override
            public ParseSettings defaultSettings() {
                return new HtmlTreeBuilder().defaultSettings();
            }

            @Override
            public Document parse(Reader in, String baseUri, Parser parser) {
                // scenario: return null
                return null;
            }

            @Override
            public List<Node> parseFragment(Reader in, @Nullable Element ctxt, String baseUri, Parser parser) {
                return null;
            }

            @Override
            protected void process(Token token) { /* implementation */ }
        }

        Parser p = new Parser(new FakeTreeBuilderReturningNullDocument());
        Reader in = new StringReader("<p>test</p>");
        String baseUri = "http://ex";

        // Expect return null
        Document result = p.parseInput(in, baseUri);
        assertNull(result, "Expected null Document when treeBuilder.parse returns null");
    }

    @Test
    @DisplayName("TC10: String overload propagates IOException thrown when reading from Reader in custom TreeBuilder")
    public void test_TC10() {
        // Inline fake TreeBuilder that forces an IOException when reading from the provided Reader
        abstract class FakeTreeBuilderReadingInput extends TreeBuilder {
            @Override
            public ParseSettings defaultSettings() {
                return new HtmlTreeBuilder().defaultSettings();
            }

            @Override
            public Document parse(Reader in, String baseUri, Parser parser) throws IOException {
                // reading first char triggers IOException from faulty Reader
                in.read();
                // unreachable if exception is thrown
                return new Document(baseUri);
            }

            @Override
            public List<Node> parseFragment(Reader in, @Nullable Element ctxt, String baseUri, Parser parser) {
                return null;
            }

            @Override
            protected void process(Token token) { /* implementation */ }
        }

        // Create a Parser with the fake TreeBuilder
        Parser p = new Parser(new FakeTreeBuilderReadingInput());
        String html = "xyz";
        String baseUri = "uri";

        // To inject a Reader that throws IOException, we override parseInput(String, String) via reflection:
        try {
            // get the Reader-overload method
            java.lang.reflect.Method parseInputReader = Parser.class.getMethod("parseInput", Reader.class, String.class);
            parseInputReader.setAccessible(true);
            // create a Reader that always throws IOException on read
            Reader faulty = new Reader() {
                @Override
                public int read(char[] cbuf, int off, int len) throws IOException {
                    throw new IOException("read failed");
                }
                @Override
                public void close() {
                    // no-op
                }
            };
            // assert that invoking parseInput(faulty, baseUri) throws IOException
            IOException ex = assertThrows(IOException.class,
                () -> parseInputReader.invoke(p, faulty, baseUri));
            assertEquals("read failed", ex.getCause().getMessage());
        } catch (NoSuchMethodException e) {
            fail("parseInput(Reader, String) method not found via reflection");
        }
    }
}