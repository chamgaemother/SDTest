package org.jsoup.nodes;

import org.jsoup.nodes.Document.OutputSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
public class Document_outputSettings_0_Test {

    @Test
    @DisplayName("outputSettings() returns default OutputSettings instance when none has been set")
    public void test_TC01() {
        // GIVEN: a new Document with no custom output settings set (B0→B1 path: direct return)
        Document doc = new Document("http://example.com");
        
        // WHEN: retrieving the output settings
        OutputSettings settings = doc.outputSettings();
        
        // THEN: the returned settings should be non-null, idempotent, and have default values
        assertAll("Default OutputSettings properties",
            () -> assertNotNull(settings, "Expected non-null OutputSettings"),
            () -> assertSame(settings, doc.outputSettings(), "Expected same instance on repeated calls"),
            () -> assertEquals(StandardCharsets.UTF_8, settings.charset(), "Default charset should be UTF-8"),
            () -> assertEquals(OutputSettings.Syntax.html, settings.syntax(), "Default syntax should be html"),
            () -> assertTrue(settings.prettyPrint(), "Default prettyPrint should be true")
        );
    }

    @Test
    @DisplayName("outputSettings() returns the same instance after setting a custom OutputSettings")
    public void test_TC02() {
        // GIVEN: a new Document and a custom OutputSettings configured differently (setter-effect, identity)
        Document doc = new Document("http://example.com");
        OutputSettings custom = new OutputSettings()
            .indentAmount(5)    // customize indent amount
            .outline(true);     // customize outline mode
        // apply the custom settings via the setter
        doc.outputSettings(custom);
        
        // WHEN: retrieving the output settings
        OutputSettings settings = doc.outputSettings();
        
        // THEN: the returned instance must be the same object passed in and reflect its values
        assertAll("Custom OutputSettings identity and properties",
            () -> assertSame(custom, settings, "Expected returned instance to be the same custom instance"),
            () -> assertEquals(5, settings.indentAmount(), "Custom indentAmount should be preserved"),
            () -> assertTrue(settings.outline(), "Custom outline flag should be preserved")
        );
    }
}