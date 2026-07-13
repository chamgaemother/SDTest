package org.jsoup.nodes;

import org.jsoup.nodes.Comment;
import org.jsoup.nodes.CDataNode;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Element_children_2_Test {

    @Test
    @DisplayName("TC08: children() filters out mixed non-Element node types (TextNode, DataNode, Comment) and retains Element nodes in correct order")
    void test_TC08() {
        // GIVEN an element with mixed children: non-Element nodes and Element nodes
        Element el = new Element("div");
        el.appendChild(new TextNode("text"));             // non-Element: should be filtered out
        el.appendChild(new DataNode("data"));             // non-Element: should be filtered out
        el.appendChild(new Comment("cmt"));               // non-Element: should be filtered out
        Element span = new Element("span");
        el.appendChild(span);                              // Element: should be in result
        el.appendChild(new CDataNode("cdata"));           // non-Element: should be filtered out
        Element p = new Element("p");
        el.appendChild(p);                                 // Element: should be in result

        // WHEN retrieving children()
        Elements result = el.children();

        // THEN only the two Element children remain, in the original insertion order
        assertEquals(2, result.size(), "Expected exactly two Element children");  
        assertSame(span, result.get(0), "First child Element should be the span");  
        assertSame(p, result.get(1), "Second child Element should be the p");  
    }

    @Test
    @DisplayName("TC09: children() rebuilds cache when only non-Element children added after initial cache build")
    void test_TC09() {
        // GIVEN an element with one Element child and cache built by first children() call
        Element el = new Element("div");
        Element child = new Element("span");
        el.appendChild(child);                             // first Element child to cache
        Elements first = el.children();                    // cache built here

        // WHEN appending a non-Element node to invalidate the cache
        el.appendChild(new TextNode("more"));              // non-Element addition should trigger nodelistChanged
        Elements result = el.children();                   // cache should be rebuilt

        // THEN the new result is a different object, but still contains exactly the original Element child
        assertNotSame(first, result, "Cache should be invalidated and rebuilt after non-Element addition");  
        assertEquals(1, result.size(), "Only one Element child should remain");  
        assertSame(child, result.get(0), "The original Element child should still be present");  
    }
}