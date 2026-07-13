package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.CDataNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
public class Element_children_2_Test {

    @Test
    @DisplayName("children() on Element with initialized but empty childNodes uses short-circuit and does not cache")
    void test_TC08() throws Exception {
        // GIVEN an element with its childNodes initialized to a NodeList (non-EmptyNodes)
        Element el = new Element("div");
        // calling ensureChildNodes transitions childNodes from EmptyNodes to a real list
        // satisfies B0->B1 (short-circuit path for children()) if no children
        el.ensureChildNodes();

        // WHEN
        Elements result = el.children();

        // THEN: no children present => empty result
        assertTrue(result.isEmpty(), "Expected no child elements, got " + result.size());
        // and the internal cache shadowChildrenRef must remain null since children() short-circuits
        Field shadowRefField = Element.class.getDeclaredField("shadowChildrenRef");
        shadowRefField.setAccessible(true);
        Object shadowRef = shadowRefField.get(el);
        assertNull(shadowRef, "shadowChildrenRef should remain null when no children are present");
    }

    @Test
    @DisplayName("children() filters out DataNode and CDataNode, including only Element children in build loop")
    void test_TC09() {
        // GIVEN a parent with mixed child node types: DataNode, CDataNode, and an Element
        Element parent = new Element("div");
        // Append a data node (should be filtered out)
        parent.appendChild(new DataNode("data"));
        // Append a cdata node (should be filtered out)
        parent.appendChild(new CDataNode("cdata"));
        // Append a real Element (should be included)
        Element child = new Element("span");
        parent.appendChild(child);

        // WHEN collecting children Elements: B0->B3 loops over childNodes, only Element instances are retained
        Elements result = parent.children();

        // THEN exactly one child element, and it must be the same instance appended
        assertEquals(1, result.size(), "Expected exactly one child element after filtering out non-Element nodes");
        assertSame(child, result.get(0), "The returned child should be the exact Element instance appended");
    }
}