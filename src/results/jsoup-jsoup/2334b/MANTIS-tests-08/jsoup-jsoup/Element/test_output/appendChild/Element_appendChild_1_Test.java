package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Element_appendChild_1_Test {

    @Test
    @DisplayName("appendChild returns this for chaining when multiple children appended")
    public void test_TC05() {
        // Path B0→B1→B2: calling appendChild twice, both times going through normal branch
        Element el = new Element("div");
        TextNode a = new TextNode("a");
        TextNode b = new TextNode("b");

        Element r1 = el.appendChild(a); // branch B1: non-null child, added
        Element r2 = el.appendChild(b); // branch B1 again, second append

        // The method should return the same instance for chaining
        assertSame(el, r1, "First appendChild should return the original element for chaining");
        assertSame(el, r2, "Second appendChild should also return the original element for chaining");
    }

    @Test
    @DisplayName("appendChild invalidates shadowChildrenRef cache when childElementsList had been called")
    public void test_TC06() throws Exception {
        // Path B0→B1→B2: warm up the shadow cache via children(), then appendChild invalidates it, and childElementsList recomputes
        Element parent = new Element("ul");
        // call children() to populate and possibly cache shadowChildrenRef (though empty initially)
        parent.children();

        // prepare a TextNode to append
        TextNode n = new TextNode("item");
        // Updated the type of returned to TextNode as per the error guide
        TextNode returned = (TextNode) parent.appendChild(n); // Corrected type casting
        assertSame(parent, returned, "appendChild should still return the parent element");

        // Now invoke the package-private childElementsList via reflection to get fresh computation
        Method childElementsList = Element.class.getDeclaredMethod("childElementsList");
        childElementsList.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Element> elems = (List<Element>) childElementsList.invoke(parent);

        // After invalidation and recompute, exactly one child element entry should exist
        assertEquals(1, elems.size(), "childElementsList should report one element after appendChild invalidated cache");
        // Ensure that the entry is not a TextNode (childElementsList filters only Element instances)
        assertFalse(elems.get(0) instanceof TextNode, "childElementsList returned an Element, not a TextNode");
    }
}