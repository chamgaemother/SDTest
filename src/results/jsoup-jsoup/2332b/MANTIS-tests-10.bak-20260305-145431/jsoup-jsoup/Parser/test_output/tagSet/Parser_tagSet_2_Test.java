package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.TreeBuilder;

import java.io.Reader;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class Parser_tagSet_2_Test {

    /**
     * TC08: Custom TreeBuilder stub’s tag set is returned by tagSet() (delegation branch)
     */
    @Test
    @DisplayName("Custom TreeBuilder stub’s tag set is returned by tagSet() (delegation branch)")
    public void test_TC08() throws Exception {
        // Arrange: stub TreeBuilder that defines its own tags ("X","Y","Z")
        class StubTreeBuilder extends TreeBuilder {
            private final Set<String> stubTags;
            StubTreeBuilder(Set<String> tags) {
                this.stubTags = tags;
            }
            @Override public ParseSettings defaultSettings() {
                return ParseSettings.preserveCase(false);
            }
            @Override public TreeBuilder newInstance() { return this; }
            @Override public org.jsoup.nodes.Document parse(Reader r, String b, Parser p) { return null; }
            @Override public List<org.jsoup.nodes.Node> parseFragment(Reader r, org.jsoup.nodes.Element ctx, String b, Parser p) { return null; }
            // provide stub tagSet for parser to delegate
            @Override public Set<String> tagSet() {
                return stubTags;
            }
            @Override
            public void process(org.jsoup.parser.Token token) {}
        }
        StubTreeBuilder stub = new StubTreeBuilder(new HashSet<>(List.of("X", "Y", "Z")));
        Parser parser = new Parser(stub);

        // Act: invoke private tagSet() via reflection, which should delegate to stub.tagSet(settings)
        Method m = Parser.class.getDeclaredMethod("tagSet");
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) m.invoke(parser);

        // Assert: exactly stub’s defined tags returned
        assertEquals(new HashSet<>(List.of("X", "Y", "Z")), result);
    }

    /**
     * TC09: tagSet() reflects preserveCase change applied after first call (settings mutation branch)
     */
    @Test
    @DisplayName("tagSet() reflects preserveCase change applied after first call (settings mutation branch)")
    public void test_TC09() throws Exception {
        // Arrange: htmlParser with default (lower-case) settings
        Parser parser = Parser.htmlParser();

        // Act 1: first call under default (lower-case) settings
        Method m = Parser.class.getDeclaredMethod("tagSet");
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> first = new HashSet<>((Set<String>) m.invoke(parser));
        // Mutate settings to preserve case
        parser.settings(ParseSettings.preserveCase(true));
        // Act 2: second call under preserveCase
        @SuppressWarnings("unchecked")
        Set<String> second = new HashSet<>((Set<String>) m.invoke(parser));

        // Assert:
        // first set contains typical lower-case tag "html" and not "HTML"
        assertTrue(first.contains("html") && !first.contains("HTML"),
                   "First call should have lower-case tags under default settings");
        // second set contains "HTML" and not "html" after preserveCase(true)
        assertTrue(second.contains("HTML") && !second.contains("html"),
                   "Second call should reflect preserveCase setting and return mixed-case tags");
    }

    /**
     * TC10: Modifying returned Set from tagSet() does not affect subsequent calls (defensive-copy branch)
     */
    @Test
    @DisplayName("Modifying returned Set from tagSet() does not affect subsequent calls (defensive-copy branch)")
    public void test_TC10() throws Exception {
        // Arrange: htmlParser with default settings
        Parser parser = Parser.htmlParser();
        Method m = Parser.class.getDeclaredMethod("tagSet");
        m.setAccessible(true);

        // Act 1: get a defensive copy, then clear it
        @SuppressWarnings("unchecked")
        Set<String> tags1 = new HashSet<>((Set<String>) m.invoke(parser));
        tags1.clear(); // mutate returned Set
        // Act 2: get a fresh tag set
        @SuppressWarnings("unchecked")
        Set<String> tags2 = new HashSet<>((Set<String>) m.invoke(parser));

        // Assert:
        // tags1 is empty after clear (local mutation)
        assertTrue(tags1.isEmpty(), "Cleared the returned set should be empty");
        // tags2 is unaffected and non-empty, contains typical tag "html"
        assertFalse(tags2.isEmpty(), "Subsequent call should not be affected by prior mutation");
        assertTrue(tags2.contains("html"), "Returned fresh set should contain 'html'");
    }
}