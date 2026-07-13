package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.select.Elements;
import org.jsoup.nodes.TextNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class Element_children_0_Test {

    @Test
    @DisplayName("children() on element with zero child nodes returns empty Elements (childNodeSize == 0 branch)")
    public void test_TC01() {
        // Given an element with no children: triggers childNodeSize() == 0 in children()
        Element el = new Element("div");
        // When
        Elements result = el.children();
        // Then: expect empty Elements
        assertEquals(0, result.size(), "Expected no child elements when none were added");
    }

    @Test
    @DisplayName("children() filters out non-Element child nodes and returns empty when only TextNode children exist (childNodeSize > 0 and no element children)")
    public void test_TC02() {
        // Given an element with a single TextNode child: childNodeSize()>0 but no Element instances
        Element el = new Element("p");
        el.appendText("text"); // adds TextNode, not Element
        // When
        Elements result = el.children();
        // Then: expect empty Elements because text nodes are filtered out
        assertEquals(0, result.size(), "Expected no Element children when only TextNode was added");
    }

    @Test
    @DisplayName("children() returns single child Element when one element child present (childNodeSize > 0, one Element iteration yields one)")
    public void test_TC03() {
        // Given a parent element with one child Element: iteration should yield one element
        Element parent = new Element("ul");
        Element li = parent.appendElement("li"); // adds an Element child
        // When
        Elements result = parent.children();
        // Then: expect exactly the appended child
        assertEquals(1, result.size(), "Expected exactly one Element child");
        assertEquals(li, result.get(0), "Expected the child at index 0 to be the appended li element");
    }

    @Test
    @DisplayName("children() returns multiple child Elements in document order when mixed Node children present (filters and preserves order)")
    public void test_TC04() {
        // Given a div with mixed children: TextNode, Element span, TextNode, Element em
        Element div = new Element("div");
        div.appendChild(new TextNode("a")); // non-Element, should be filtered out
        Element e1 = div.appendElement("span"); // first Element child
        div.appendText("b"); // another TextNode
        Element e2 = div.appendElement("em"); // second Element child
        // When
        Elements result = div.children();
        // Then: only the two Element children in order
        assertEquals(2, result.size(), "Expected two Element children filtered from mixed nodes");
        assertEquals(e1, result.get(0), "First child should be the <span> element");
        assertEquals(e2, result.get(1), "Second child should be the <em> element");
    }

    @Test
    @DisplayName("children() returns fresh Elements list each call, independent of cache (calling twice yields distinct Collections)")
    public void test_TC05() {
        // Given a parent element with initial one child Element: establishes cache on first children()
        Element parent = new Element("ol");
        Element first = parent.appendElement("li");
        // When: first call populates cache
        Elements result1 = parent.children();
        // Then: result1 has size 1
        assertEquals(1, result1.size(), "Expected one child in first invocation");
        // When: modify children by adding another li
        Element second = parent.appendElement("li");
        // And call children() again: should reflect new child, not stale cache
        Elements result2 = parent.children();
        // Then: result2 has size 2, showing fresh list
        assertEquals(2, result2.size(), "Expected two children in second invocation after appending another child");
    }
}