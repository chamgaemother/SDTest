package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TreeBuilder;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
public class Parser_parseFragmentInput_1_Test {

    @Test
    @DisplayName("String overload propagates runtime exception thrown by TreeBuilder.parseFragment")
    public void test_TC07() {
        // Arrange: stub TreeBuilder that always throws IllegalStateException in parseFragment
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public ParseSettings defaultSettings() {
                return new ParseSettings(false, true);
            }
            @Override
            public TagSet defaultTagSet() {
                return new TagSet(TagSet.Html());
            }
            @Override
            public TreeBuilder newInstance() {
                return this;
            }
            @Override
            public String defaultNamespace() {
                return Parser.NamespaceHtml;
            }
            @Override
            public List<Node> parseFragment(Reader input, Element context, String baseUri, Parser parser) {
                // Simulate failure at deepest branch B4
                throw new IllegalStateException("forced");
            }
            @Override
            public void process(org.jsoup.parser.Token token) { 
                // Implemented to satisfy abstract method
                // No operation needed for this test
            }
            @Override
            protected void initialiseParse(Reader r, String baseUri, Parser parser) { /* no-op */ }
        };
        Parser parser = new Parser(stubBuilder);
        String frag = "<div>x</div>";
        Element ctx = new Element(org.jsoup.parser.Tag.valueOf("div"), "base", new org.jsoup.nodes.Attributes());
        // Act & Assert: expecting IllegalStateException propagated by string overload
        assertThrows(IllegalStateException.class, () -> parser.parseFragmentInput(frag, ctx, "base"),
            "Should propagate IllegalStateException from stubTreeBuilder.parseFragment");
    }

    @Test
    @DisplayName("Reader overload propagates runtime exception thrown by TreeBuilder.parseFragment")
    public void test_TC08() {
        // Arrange: stub TreeBuilder that always throws UnsupportedOperationException in parseFragment
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public ParseSettings defaultSettings() {
                return new ParseSettings(false, true);
            }
            @Override
            public TagSet defaultTagSet() {
                return new TagSet(TagSet.Html());
            }
            @Override
            public TreeBuilder newInstance() {
                return this;
            }
            @Override
            public String defaultNamespace() {
                return Parser.NamespaceHtml;
            }
            @Override
            public List<Node> parseFragment(Reader input, Element context, String baseUri, Parser parser) {
                // Simulate failure at deepest branch B4
                throw new UnsupportedOperationException("stub");
            }
            @Override
            public void process(org.jsoup.parser.Token token) { 
                // Implemented to satisfy abstract method
                // No operation needed for this test
            }
            @Override
            protected void initialiseParse(Reader r, String baseUri, Parser parser) { /* no-op */ }
        };
        Parser parser = new Parser(stubBuilder);
        Reader reader = new StringReader("<i>i</i>");
        Element ctx = new Element(org.jsoup.parser.Tag.valueOf("i"), "base", new org.jsoup.nodes.Attributes());
        // Act & Assert: expecting UnsupportedOperationException propagated by reader overload
        assertThrows(UnsupportedOperationException.class, () -> parser.parseFragmentInput(reader, ctx, "base"),
            "Should propagate UnsupportedOperationException from stubTreeBuilder.parseFragment");
    }

}