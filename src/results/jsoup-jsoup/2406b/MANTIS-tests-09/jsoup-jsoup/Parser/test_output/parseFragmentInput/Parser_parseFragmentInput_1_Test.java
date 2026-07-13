package org.jsoup.parser;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.TagSet;
import org.jsoup.parser.TreeBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Parser_parseFragmentInput_1_Test {

    // A minimal stub TreeBuilder to satisfy abstract methods
    private static abstract class StubBuilder extends TreeBuilder {
        protected StubBuilder() {
            // no-op
        }
        @Override
        public ParseSettings defaultSettings() {
            return new ParseSettings(true, true);
        }
        @Override
        public TagSet defaultTagSet() {
            return TagSet.Html();
        }
        @Override
        public String defaultNamespace() {
            return Parser.NamespaceHtml;
        }
        @Override
        public TreeBuilder newInstance() {
            try {
                return this.getClass().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public List<Node> parse(Reader input, String baseUri) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void process(org.jsoup.parser.Token token) {} // Updated to match superclass method
    }

    @Test
    @DisplayName("String-overload: non-null fragment, non-null context, non-null baseUri returns parsed nodes")
    public void test_TC11() {
        TreeBuilder stub = new StubBuilder() {
            @Override
            public List<Node> parseFragment(Reader fragment, Element context, String baseUri) {
                return List.of(new TextNode("hello"));
            }
            @Override
            public void process(org.jsoup.parser.Token token) {} // Implemented process method
        };
        Parser parser = new Parser(stub);
        String frag = "<p>test</p>";
        Element ctx = new Element("div");
        String uri = "base";
        List<Node> result = parser.parseFragmentInput(frag, ctx, uri);
        assertEquals(1, result.size(), "Should return one node");
        assertTrue(result.get(0) instanceof TextNode, "Returned node should be TextNode");
        assertEquals("hello", ((TextNode) result.get(0)).text(), "TextNode text must match stub");
    }

    @Test
    @DisplayName("String-overload: null context is forwarded to TreeBuilder.parseFragment")
    public void test_TC12() {
        TreeBuilder stub = new StubBuilder() {
            @Override
            public List<Node> parseFragment(Reader fragment, Element context, String baseUri) {
                assertNull(context, "Context must be forwarded as null");
                return Collections.emptyList();
            }
            @Override
            public void process(org.jsoup.parser.Token token) {} // Implemented process method
        };
        Parser parser = new Parser(stub);
        List<Node> result = parser.parseFragmentInput("<b/>", null, "base");
        assertTrue(result.isEmpty(), "Should return empty list when stub returns no nodes");
    }

    @Test
    @DisplayName("String-overload: null baseUri is forwarded to TreeBuilder.parseFragment")
    public void test_TC13() {
        class RecBuilder extends StubBuilder {
            String lastBaseUri;
            @Override
            public List<Node> parseFragment(Reader fragment, Element context, String baseUri) {
                lastBaseUri = baseUri;
                return Collections.emptyList();
            }
            @Override
            public void process(org.jsoup.parser.Token token) {} // Implemented process method
        }
        RecBuilder stub = new RecBuilder();
        Parser parser = new Parser(stub);
        List<Node> result = parser.parseFragmentInput("<i/>", new Element("i"), null);
        assertTrue(result.isEmpty(), "Should return empty list on stub");
        assertNull(stub.lastBaseUri, "Stub should record null baseUri");
    }

    @Test
    @DisplayName("String-overload: TreeBuilder.parseFragment throws IOException, propagates UncheckedIOException")
    public void test_TC14() {
        TreeBuilder stub = new StubBuilder() {
            @Override
            public List<Node> parseFragment(Reader fragment, Element context, String baseUri) {
                try {
                    fragment.read(); // may throw IOException
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                throw new UncheckedIOException(new IOException("stub")); // ensure exception
            }
            @Override
            public void process(org.jsoup.parser.Token token) {} // Implemented process method
        };
        Parser parser = new Parser(stub);
        StringReader r = new StringReader("err");
        UncheckedIOException ex = assertThrows(UncheckedIOException.class,
            () -> parser.parseFragmentInput("err", new Element("p"), "base"),
            "Should propagate UncheckedIOException");
        assertNotNull(ex.getCause(), "Cause must be the original IOException");
        assertTrue(ex.getCause() instanceof IOException, "Cause should be IOException");
    }

    @Test
    @DisplayName("Reader-overload: non-null Reader, non-null context, non-null baseUri returns parsed nodes")
    public void test_TC15() {
        TreeBuilder stub = new StubBuilder() {
            @Override
            public List<Node> parseFragment(Reader fragment, Element context, String baseUri) {
                return List.of(new Comment("cmt"));
            }
            @Override
            public void process(org.jsoup.parser.Token token) {} // Implemented process method
        };
        Parser parser = new Parser(stub);
        Reader r = new StringReader("<--->");
        List<Node> result = parser.parseFragmentInput(r, new Element("span"), "base");
        assertEquals(1, result.size(), "Should return one node");
        assertTrue(result.get(0) instanceof Comment, "Returned node should be Comment");
        assertEquals("cmt", ((Comment) result.get(0)).getData(), "Comment data must match stub");
    }

    @Test
    @DisplayName("Reader-overload: null context is forwarded to TreeBuilder.parseFragment")
    public void test_TC16() {
        TreeBuilder stub = new StubBuilder() {
            @Override
            public List<Node> parseFragment(Reader fragment, Element context, String baseUri) {
                assertNull(context, "Context must be null");
                return Collections.emptyList();
            }
            @Override
            public void process(org.jsoup.parser.Token token) {} // Implemented process method
        };
        Parser parser = new Parser(stub);
        Reader r = new StringReader("<tag/>");
        List<Node> result = parser.parseFragmentInput(r, null, "base");
        assertTrue(result.isEmpty(), "Should return empty list for null-context stub");
    }

    @Test
    @DisplayName("Reader-overload: null baseUri is forwarded to TreeBuilder.parseFragment")
    public void test_TC17() {
        class RecBuilder extends StubBuilder {
            String lastBaseUri;
            @Override
            public List<Node> parseFragment(Reader fragment, Element context, String baseUri) {
                lastBaseUri = baseUri;
                return Collections.emptyList();
            }
            @Override
            public void process(org.jsoup.parser.Token token) {} // Implemented process method
        }
        RecBuilder stub = new RecBuilder();
        Parser parser = new Parser(stub);
        Reader r = new StringReader("data");
        List<Node> result = parser.parseFragmentInput(r, new Element("div"), null);
        assertTrue(result.isEmpty(), "Should return empty list");
        assertNull(stub.lastBaseUri, "Stub should record null baseUri");
    }

    @Test
    @DisplayName("Reader-overload: TreeBuilder.parseFragment throws IOException, propagates UncheckedIOException")
    public void test_TC18() {
        TreeBuilder stub = new StubBuilder() {
            @Override
            public List<Node> parseFragment(Reader fragment, Element context, String baseUri) {
                try {
                    fragment.read();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                throw new UncheckedIOException(new IOException("err"));
            }
            @Override
            public void process(org.jsoup.parser.Token token) {} // Implemented process method
        };
        Parser parser = new Parser(stub);
        Reader r = new StringReader("err");
        UncheckedIOException ex = assertThrows(UncheckedIOException.class,
            () -> parser.parseFragmentInput(r, new Element("p"), "base"),
            "Should propagate UncheckedIOException");
        assertNotNull(ex.getCause(), "Cause must be the original IOException");
        assertTrue(ex.getCause() instanceof IOException, "Cause should be IOException");
    }
}