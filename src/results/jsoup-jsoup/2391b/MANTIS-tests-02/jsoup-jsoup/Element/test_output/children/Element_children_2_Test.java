package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Attributes;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
public class Element_children_2_Test {

    @Test
    @DisplayName("children() on element with only DataNode child takes non-zero childNodeSize path and yields empty list")
    void test_TC07() {
        // GIVEN: an element with a single DataNode child -> childNodeSize > 0, but no Element children
        Element e = new Element("div");
        e.appendChild(new DataNode("data"));
        // WHEN: retrieving children (filtered to Element nodes)
        Elements result = e.children(); // This line remains unchanged as children() method is valid
        // THEN: result is non-null and empty, since DataNode does not count as child element
        assertNotNull(result, "children() should never return null");
        assertEquals(0, result.size(), "No Element children should yield empty Elements list");
    }

    @Test
    @DisplayName("children() uses fresh build when cachedChildren returns non-null ref but modCount mismatches (stale cache)")
    void test_TC08() throws Exception {
        // GIVEN: parent with one real child <span>, and a stale cache injected via attributes.userData
        Element parent = new Element("div");
        Element child = parent.appendElement("span");
        // Manually set up stale cache: wrong mod count triggers rebuild path
        Attributes attrs = parent.attributes();
        Map<String, Object> userData = attrs.userData();
        // Create a fake cached list containing our child, but with stale mod count (-1)
        WeakReference<List<Element>> ref = new WeakReference<>(List.of(child));
        userData.put("jsoup.childEls", ref);
        userData.put("jsoup.childElsMod", Integer.valueOf(-1));
        // Precondition: cachedChildren() returns non-null ref but modCount mismatches
        // WHEN: retrieving children should ignore stale cache and rebuild from actual childNodes
        Elements result = parent.children(); // This line remains unchanged as children() method is valid
        // THEN: result reflects actual children (size 1, same instance)
        assertEquals(1, result.size(), "Should rebuild children list when cache stale");
        assertSame(child, result.get(0), "Rebuilt children list should contain the actual child element");
    }
}