package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
public class Parser_tagSet_0_Test {

    @DisplayName("Test tagSet method")
    @Test
    public void testTagSet() {
        // Assuming the method tagSet returns a Set<String> of tags
        Set<String> expectedTags = new HashSet<>(Arrays.asList("html", "head", "body"));
        Set<String> actualTags = Parser.getTagSet(); // Updated method name to getTagSet()
        assertEquals(expectedTags, actualTags, "The tagSet method should return the correct set of tags.");
    }
}