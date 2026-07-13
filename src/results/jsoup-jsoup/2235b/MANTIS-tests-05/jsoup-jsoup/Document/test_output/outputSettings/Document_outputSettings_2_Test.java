package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.Document.QuirksMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
public class Document_outputSettings_2_Test {

    @Test
    @DisplayName("outputSettings(OutputSettings) assigns new settings instance and allows chaining in call expression")
    public void test_TC05() {
        // GIVEN a fresh Document and a custom OutputSettings with non-default escapeMode and outline
        Document doc = new Document("http://example.com");
        OutputSettings custom = new OutputSettings()
            .escapeMode(EscapeMode.extended)
            .outline(true);
        // WHEN chaining the setter and another method to ensure chaining returns the same Document
        Document returned = doc.outputSettings(custom)  // path B1->B2: non-null setter branch
                                 .quirksMode(QuirksMode.limitedQuirks); // further chaining
        // THEN the returned instance is the original Document, and the getter returns the exact passed settings
        assertSame(doc, returned, "Setter should return the same Document instance for chaining");
        assertSame(custom, doc.outputSettings(), "Getter should return the passed-in settings instance");
        assertEquals(EscapeMode.extended, doc.outputSettings().escapeMode(),
            "EscapeMode should be preserved in the assigned settings");
        assertTrue(doc.outputSettings().outline(), "Outline flag should be preserved in the assigned settings");
    }

    @Test
    @DisplayName("outputSettings(null) throws IllegalArgumentException when called second time after valid setter")
    public void test_TC06() {
        // GIVEN a Document with a previously set non-null OutputSettings
        Document doc = new Document("http://example.com");
        OutputSettings custom = new OutputSettings();
        doc.outputSettings(custom); // establish a valid setter invocation
        // WHEN calling setter with null -> path B1->B3: null validation branch
        assertThrows(IllegalArgumentException.class, () -> doc.outputSettings(null),
            "Setting outputSettings to null should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("outputSettings(OutputSettings) with custom indent and maxPaddingWidth retains numeric properties")
    public void test_TC07() {
        // GIVEN a new Document and a settings instance with boundary numeric values
        Document doc = new Document("http://example.com");
        OutputSettings os = new OutputSettings()
            .indentAmount(0)      // boundary: zero indent
            .maxPaddingWidth(-1); // boundary: unlimited padding
        // WHEN assigning the custom settings -> path B1->B2->B4: non-null branch
        doc.outputSettings(os);
        // THEN getter returns the same instance and numeric properties are preserved
        assertSame(os, doc.outputSettings(), "Getter should return the same settings instance assigned");
        assertEquals(0, doc.outputSettings().indentAmount(), "Indent amount should be retained as 0");
        assertEquals(-1, doc.outputSettings().maxPaddingWidth(), "Max padding width should be retained as -1");
    }

    @Test
    @DisplayName("outputSettings() getter consistently returns same instance after multiple calls")
    public void test_TC08() {
        // GIVEN a fresh Document with default settings
        Document doc = new Document("http://example.com");
        // WHEN calling getter twice -> path B1->B2->B4 for getter
        OutputSettings firstGet = doc.outputSettings();
        OutputSettings secondGet = doc.outputSettings();
        // THEN both calls should yield the identical settings instance (idempotence)
        assertSame(firstGet, secondGet, "Repeated getter calls should return the same settings instance");
    }
}