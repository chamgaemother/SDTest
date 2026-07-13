package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_0_Test {

    @Test
    @DisplayName("children() on element with no child nodes returns empty Elements (childNodeSize==0 branch)")
    public void test_TC01() {
        // GIVEN an element with no children => childNodeSize()==0 takes short-circuit path
        Element el = new Element("div");
        // WHEN
        Elements result = el.children();
        // THEN
        assertTrue(result.isEmpty(), "Expected no children for empty element");
    }

    @Test
    @DisplayName("children() on element with only non-element nodes returns empty Elements (filter yields zero)")
    public void test_TC02() {
        // GIVEN an element with a TextNode child => branch B1 false then loop over one node which is not Element
        Element el = new Element("p");
        el.appendChild(new TextNode("text"));
        // WHEN
        Elements result = el.children();
        // THEN no Element nodes should be in the result
        assertTrue(result.isEmpty(), "Expected no elements when only text nodes present");
    }

    @Test
    @DisplayName("children() on element with mixed child nodes returns only Element children")
    public void test_TC03() {
        // GIVEN a mix of TextNode and Element child => loop collects only the Element node
        Element el = new Element("ul");
        el.appendChild(new TextNode("foo"));
        el.appendChild(new Element("li"));
        // WHEN
        Elements result = el.children();
        // THEN exactly one child element li expected
        assertEquals(1, result.size(), "Expected one element child");
        assertEquals("li", result.get(0).tagName(), "Expected tagName of the child to be 'li'");
    }

    @Test
    @DisplayName("children() caches childElementsList: second call returns same list instance after no mutation")
    public void test_TC04() {
        // GIVEN an element with a single Element child => initial loop builds list of size 1
        Element el = new Element("div");
        el.appendChild(new Element("span"));
        // WHEN first call populates cache
        Elements first = el.children();
        // WHEN second call should return the same Elements wrapper if caching intended
        Elements second = el.children();
        // THEN verify identity
        assertSame(first, second, "Expected children() to return same Elements instance when no mutation occurred");
    }

    @Test
    @DisplayName("children() recomputes after mutation: cache invalidated by nodelistChanged")
    public void test_TC05() {
        // GIVEN an element with one child and its cache populated
        Element el = new Element("div");
        el.appendChild(new Element("a"));
        el.children(); // populate cache
        // WHEN appending another element invalidates cache and rebuilds list with two elements
        el.appendChild(new Element("b"));
        Elements result = el.children();
        // THEN expect two children and correct tag ordering
        assertEquals(2, result.size(), "Expected two element children after mutation");
        assertEquals("b", result.get(1).tagName(), "Expected second child tagName to be 'b'");
    }

    @Test
    @DisplayName("children() on element with nested element children returns only immediate Element children")
    public void test_TC06() {
        // GIVEN a nested element child: only immediate p should be returned, not span
        Element parent = new Element("div");
        Element child = new Element("p");
        child.appendChild(new Element("span"));
        parent.appendChild(child);
        // WHEN
        Elements result = parent.children();
        // THEN only the <p> child is returned
        assertEquals(1, result.size(), "Expected only immediate element child");
        assertEquals("p", result.get(0).tagName(), "Expected tagName of the child to be 'p'");
    }

    @Test
    @DisplayName("children() on element with multiple element children returns all children in order")
    public void test_TC07() {
        // GIVEN multiple Element children appended in order => loop collects all in same order
        Element el = new Element("ol");
        el.appendChild(new Element("li"));
        el.appendChild(new Element("li"));
        el.appendChild(new Element("li"));
        // WHEN
        Elements result = el.children();
        // THEN size and order preserved
        assertEquals(3, result.size(), "Expected three element children");
        assertEquals("li", result.get(0).tagName(), "First child should be 'li'");
        assertEquals("li", result.get(2).tagName(), "Last child should be 'li'");
    }
}