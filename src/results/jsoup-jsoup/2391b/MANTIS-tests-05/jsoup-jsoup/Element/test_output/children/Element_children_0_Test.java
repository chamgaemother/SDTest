package org.jsoup.nodes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jsoup.select.Elements;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for org.jsoup.nodes.Element.children() method.
 */
public class Element_children_0_Test {

    @Test
    @DisplayName("children() returns empty Elements when no child nodes exist (childNodeSize==0)")
    public void test_TC01() {
        // GIVEN: an Element with no children (childNodes is EmptyNodeList)
        Element el = new Element("div");
        // WHEN: calling children()
        Elements result = el.children();
        // THEN: expect an empty Elements list (size 0)
        // Branch: childNodeSize()==0 -> returns EmptyChildren list
        assertEquals(0, result.size(), "Expected no child elements when none appended");
    }

    @Test
    @DisplayName("children() returns one-element Elements when exactly one Element child exists")
    public void test_TC02() {
        // GIVEN: a parent Element with one Element child appended
        Element parent = new Element("div");
        Element child = new Element("span");
        parent.appendChild(child);
        // WHEN: calling children()
        Elements result = parent.children();
        // THEN: expect size == 1 and the exact same child
        // Branch: childNodeSize()>0 and exactly one Element node in list
        assertAll(
            () -> assertEquals(1, result.size(), "Expected exactly one child element"),
            () -> assertSame(child, result.get(0), "Expected the appended child element to be returned")
        );
    }

    @Test
    @DisplayName("children() filters out non-Element nodes and returns only Element children")
    public void test_TC03() {
        // GIVEN: a parent Element with mixed child node types: TextNode, Element p, Comment, Element span
        Element parent = new Element("div");
        TextNode t = new TextNode("text");
        Element c1 = new Element("p");
        Comment comment = new Comment("cmt");
        Element c2 = new Element("span");
        parent.appendChild(t);
        parent.appendChild(c1);
        parent.appendChild(comment);
        parent.appendChild(c2);
        // WHEN: calling children(), should skip TextNode and Comment
        Elements result = parent.children();
        // THEN: expect only the two Element children in original order
        // Loop: iterates all childNodes, filters instanceof Element
        assertAll(
            () -> assertEquals(2, result.size(), "Expected two element children only"),
            () -> assertSame(c1, result.get(0), "First Element child should be c1"),
            () -> assertSame(c2, result.get(1), "Second Element child should be c2")
        );
    }

    @Test
    @DisplayName("children() returns unmodifiable Elements list independent of later child modifications")
    public void test_TC04() {
        // GIVEN: a parent Element with two child Elements and take a snapshot via children()
        Element parent = new Element("div");
        Element c1 = new Element("p");
        Element c2 = new Element("span");
        parent.appendChild(c1);
        parent.appendChild(c2);
        Elements snapshot = parent.children();
        // WHEN: modifying parent by appending another child after snapshot
        parent.appendChild(new Element("b"));
        // THEN: snapshot remains unchanged (size 2, contains only c1 and c2)
        // Immutability: children() returns a new Elements copy unaffected by later mutations
        assertAll(
            () -> assertEquals(2, snapshot.size(), "Snapshot should remain size 2 after parent modification"),
            () -> assertSame(c1, snapshot.get(0), "Snapshot first element should still be c1"),
            () -> assertSame(c2, snapshot.get(1), "Snapshot second element should still be c2")
        );
    }
}