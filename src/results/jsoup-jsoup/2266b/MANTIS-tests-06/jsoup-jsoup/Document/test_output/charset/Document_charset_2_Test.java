package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for Document.charset method, covering HTML syntax with existing <meta charset> update behavior.
 */
public class Document_charset_2_Test {

    @Test
    @DisplayName("TC09: charset(html) on HTML syntax with existing <meta charset> only updates its attribute and removes no obsolete elements")
    public void test_TC09() {
        // Arrange: create a shell document and append an existing <meta charset> element
        Document doc = Document.createShell("http://example");
        // precondition: html syntax, existing meta[charset]
        org.jsoup.nodes.Element existing = doc.head()
                .appendElement("meta")
                .attr("charset", "OLD");
        assertEquals("OLD", existing.attr("charset"), "Sanity check: precondition meta charset should be OLD");

        // Act: set to US-ASCII, triggers updateMetaCharsetElement(true) and ensureMetaCharsetElement for HTML branch
        doc.charset(StandardCharsets.US_ASCII);

        // Assert: updated attribute on the existing meta[charset] and no obsolete meta[name=charset]
        org.jsoup.nodes.Element updated = doc.head().selectFirst("meta[charset]");
        assertNotNull(updated, "Expected an existing <meta charset> element to be present after charset change");
        assertEquals(
                StandardCharsets.US_ASCII.displayName(),
                updated.attr("charset"),
                "The meta charset attribute should be updated to the new charset's display name"
        );
        // Ensure no obsolete <meta name=charset> elements were removed (none should exist)
        assertTrue(
                doc.select("meta[name=charset]").isEmpty(),
                "No <meta name=charset> elements should remain after updating charset"
        );
    }
}