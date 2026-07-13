package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Element_appendChild_2_Test {

    @Test
    @DisplayName("Test appending a child element")
    public void testAppendChild() {
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        assertEquals(1, parent.childrenSize()); // Ensure child is appended
        assertEquals(child, parent.child(0)); // Ensure the correct child is appended
    }

    @Test
    @DisplayName("Test appending null child element")
    public void testAppendNullChild() {
        Element parent = new Element("div");
        assertThrows(NullPointerException.class, () -> {
            parent.appendChild(null);
        }); // Ensure NullPointerException is thrown
    }

    // Add more tests as needed to cover additional scenarios
}