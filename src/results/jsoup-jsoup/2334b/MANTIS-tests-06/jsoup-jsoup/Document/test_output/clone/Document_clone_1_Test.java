package org.jsoup.nodes;

import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JUnit5 tests for Document.clone() behavior, especially exception propagation when parser.clone() fails.
 */
public class Document_clone_1_Test {

    @Test
    @DisplayName("TC02: clone() propagates RuntimeException when parser.clone() throws")
    public void test_TC02() {
        // GIVEN: a Document with a stub Parser whose clone() throws a RuntimeException
        Document doc = new Document("ns", "http://base");
        Parser failingParser = new Parser("http://base") { // Using a valid constructor with a base URI
            @Override
            public Parser clone() {
                // simulate failure in parser.clone()
                throw new RuntimeException("fail");
            }
        };
        doc.parser(failingParser);

        // WHEN & THEN: calling clone() should propagate the RuntimeException with the same message
        RuntimeException ex = assertThrows(RuntimeException.class, () -> doc.clone());
        // verify the exception message is the original one from parser.clone()
        assert ex.getMessage().equals("fail");
    }
}