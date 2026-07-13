package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
public class Document_outputSettings_1_Test {

    @Test
    @DisplayName("TC04: Setter returns this and allows fluent chaining when called consecutively")
    void test_TC04() {
        // GIVEN: a new Document and two distinct OutputSettings instances
        Document doc = new Document("http://chain");
        Document.OutputSettings s1 = new Document.OutputSettings();
        Document.OutputSettings s2 = new Document.OutputSettings().prettyPrint(false);
        
        // WHEN: chaining calls to outputSettings should always return the same Document instance
        Document r1 = doc.outputSettings(s1); // first setter call, should set internal outputSettings to s1
        Document r2 = r1.outputSettings(s2);   // second setter call, should set internal outputSettings to s2
        
        // THEN: r1 and r2 are the same as the original Document, and internal settings updated to s2
        assertSame(doc, r1, "First call to outputSettings should return the same Document instance");
        assertSame(doc, r2, "Second call to outputSettings should return the same Document instance");
        assertSame(s2, doc.outputSettings(), "After chaining, the Document's outputSettings should be the last one passed (s2)");
    }

    @Test
    @DisplayName("TC05: Getter returns internal reference even after setter mutation")
    void test_TC05() {
        // GIVEN: a document and an OutputSettings with a non-default indent amount
        Document doc = new Document("http://example");
        Document.OutputSettings newOs = new Document.OutputSettings().indentAmount(5);
        // setter call to mutate internal state
        doc.outputSettings(newOs);

        // WHEN: retrieving the current settings
        Document.OutputSettings current = doc.outputSettings();

        // THEN: the getter returns the exact same instance, preserving mutable state
        assertSame(newOs, current, "Getter should return the exact same OutputSettings instance previously set");
        assertEquals(5, current.indentAmount(), "The indentAmount should reflect the mutation (5) made before");
    }
}