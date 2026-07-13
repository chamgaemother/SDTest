package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("TC07: parseInput(Reader, String) propagates RuntimeException from custom TreeBuilder.parse")
    public void test_TC07() {
        // Arrange: stub TreeBuilder whose parse method always throws IllegalStateException
        TreeBuilder stubBuilder = new TreeBuilder() {
            @Override
            public ParseSettings defaultSettings() {
                // Provide a valid ParseSettings for Parser construction
                return new ParseSettings(true, true);
            }

            @Override
            public TreeBuilder newInstance() {
                return this;
            }

            @Override
            public Document parse(Reader r, String baseUri, Parser parser) {
                // Simulate runtime failure inside parseInput
                throw new IllegalStateException("fail-runtime");
            }

            @Override
            public List<Node> parseFragment(Reader r, Element context, String baseUri, Parser parser) {
                return null;
            }

            @Override
            public void process(org.jsoup.parser.Token token) {
                // Implement logic as required by TreeBuilder
                // No operation needed for this test
            }
        };
        Parser parser = new Parser(stubBuilder);
        Reader input = new StringReader("<p>test</p>");
        String baseUri = "http://x";

        // Act & Assert: expect the IllegalStateException with the original message to propagate
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            // Branch B0->B4->B6: lock acquired, then stubBuilder.parse throws
            parser.parseInput(input, baseUri);
        });
        // Verify that original exception message is preserved
        org.junit.jupiter.api.Assertions.assertEquals("fail-runtime", ex.getMessage());
    }
}