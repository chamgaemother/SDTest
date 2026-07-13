package org.jsoup.nodes;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Element_children_0_Test {

    @Test
    @DisplayName("TC01: children() on element with no child nodes returns empty Elements (childNodeSize==0 branch)")
    public void test_TC01() {
        // GIVEN an element with no children to trigger childNodeSize()==0 branch
        Element parent = new Element("div");
        // WHEN
        Elements result = parent.children();
        // THEN should be empty
        assertEquals(0, result.size(), "Expected no child elements when none were appended");
    }

    @Test
    @DisplayName("TC02: children() returns single child element when exactly one Element child present (childNodeSize>0, loop-1)")
    public void test_TC02() {
        // GIVEN one Element child and no others; childNodeSize()>0 and one loop iteration over childNodes
        Element parent = new Element("div");
        Element childP = parent.appendElement("p");
        // WHEN
        Elements result = parent.children();
        // THEN exactly that <p> should be present
        assertEquals(1, result.size(), "Expected exactly one child element");
        assertEquals("p", result.get(0).tagName(), "Expected the child tag to be <p>");
        assertSame(childP, result.get(0), "Expected the same instance of the appended <p> element");
    }

    @Test
    @DisplayName("TC03: children() filters out non-Element nodes and returns only Element children")
    public void test_TC03() {
        // GIVEN mix of Element and TextNode children; ensures loop over 3 nodes and filters out non-Element
        Element parent = new Element("div");
        Element a = parent.appendElement("a");
        parent.appendText("x");
        Element span = parent.appendElement("span");
        // WHEN
        Elements result = parent.children();
        // THEN only <a> and <span> remain, in correct order
        assertEquals(2, result.size(), "Expected two element children only");
        assertEquals("a", result.get(0).tagName(), "First child should be <a>");
        assertEquals("span", result.get(1).tagName(), "Second child should be <span>");
    }

    @Test
    @DisplayName("TC04: children() caches childElementsList; second call uses cache without rebuilding")
    public void test_TC04() throws Exception {
        // GIVEN one Element child; first call builds cache (shadowChildrenRef!=null afterwards)
        Element parent = new Element("div");
        parent.appendElement("p");
        // WHEN: call package-private childElementsList reflectively twice to inspect caching
        Method m = Element.class.getDeclaredMethod("childElementsList");
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Element> firstList = (List<Element>) m.invoke(parent);
        @SuppressWarnings("unchecked")
        List<Element> secondList = (List<Element>) m.invoke(parent);
        // THEN both references should be identical, indicating cache reuse
        assertSame(firstList, secondList, "Expected childElementsList to return same cached list on second call");
    }

    @Test
    @DisplayName("TC05: children() invalidates cache after node list change and rebuilds list including new child")
    public void test_TC05() {
        // GIVEN a parent with two Element children appended sequentially, which should invalidate cache on second append
        Element parent = new Element("div");
        Element p = parent.appendElement("p");      // builds initial cache with [p]
        Element span = parent.appendElement("span"); // this call invalidates cache internally
        // WHEN
        Elements result = parent.children();
        // THEN children() should reflect both <p> and <span>
        assertEquals(2, result.size(), "Expected two children after appending second element");
        assertEquals("p", result.get(0).tagName(), "First child should remain <p>");
        assertEquals("span", result.get(1).tagName(), "Second child should now be <span>");
    }
}