package org.jsoup.nodes;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for org.jsoup.nodes.Document#outputSettings(OutputSettings)
 */
public class Document_outputSettings_2_Test {

    @Test
    @DisplayName("TC05: outputSettings assigns the exact instance (no cloning) so subsequent mutations affect the document")
    public void test_TC05() {
        // GIVEN a fresh Document and an OutputSettings instance configured to prettyPrint(true)
        Document doc = new Document("baseUri");
        OutputSettings settings = new OutputSettings().prettyPrint(true);
        // The path B0→B1→B2→B3→B4 corresponds to entering outputSettings, validating non-null, assigning, and returning.
        
        // WHEN we assign the settings instance and then mutate it
        doc.outputSettings(settings);  // B1: Validate.notNull(settings) passes since settings != null
        settings.prettyPrint(false);   // mutation on the same instance
        
        // THEN the document should reflect the same instance change
        // If implementation wrongly clones, this assertion will fail.
        assertFalse(doc.outputSettings().prettyPrint(),
            "Expected document to hold the same OutputSettings reference so prettyPrint=false is seen");
    }
}