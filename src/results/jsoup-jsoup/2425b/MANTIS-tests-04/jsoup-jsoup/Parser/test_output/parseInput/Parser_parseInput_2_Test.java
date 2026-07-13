package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.ParseErrorList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_parseInput_2_Test {

    @Test
    @DisplayName("parseInput(String,String) with real HtmlTreeBuilder produces Document with expected element")
    public void test_TC14() {
        // GIVEN: html parser to drive HTML5 branch, input contains <p>Hello</p> to hit element creation branch
        Parser parser = Parser.htmlParser();
        String html = "<p>Hello</p>";
        String baseUri = "http://example/";
        // WHEN: parseInput(String, String) is invoked
        Document doc = parser.parseInput(html, baseUri);
        // THEN: Document should contain a <p> element with text 'Hello'
        Element p = doc.selectFirst("p");
        assertNotNull(p, "Expected a <p> element in the parsed document");
        assertEquals("Hello", p.text());
    }

    @Test
    @DisplayName("parseInput(Reader,String) with real XmlTreeBuilder produces Document with expected root element")
    public void test_TC15() {
        // GIVEN: xml parser to drive XML branch, input is Reader of <root><a>X</a></root> to create root and child
        Parser parser = Parser.xmlParser();
        Reader reader = new StringReader("<root><a>X</a></root>");
        String baseUri = "";
        // WHEN: parseInput(Reader, String) is invoked
        Document doc = parser.parseInput(reader, baseUri);
        // THEN: Document should contain a root element with child <a>
        assertFalse(doc.childNodes().isEmpty(), "Document should have a root element");
        assertEquals("root", doc.child(0).nodeName());
        Element a = doc.selectFirst("a");
        assertNotNull(a, "Expected an <a> element inside <root>");
        assertEquals("X", a.text());
    }

    @Test
    @DisplayName("parseInput(String,String) with trackErrors enabled captures mismatched-tag parse error")
    public void test_TC16() {
        // GIVEN: html parser with error tracking enabled to hit error-tracking branch on mismatched tags
        Parser parser = Parser.htmlParser().setTrackErrors(1);
        String html = "<div><span></div>"; // mismatched tags triggers error
        String baseUri = "http://x/";
        // WHEN: parseInput parses the mismatched HTML
        parser.parseInput(html, baseUri);
        // THEN: getErrors().size() == 1
        ParseErrorList errors = parser.getErrors();
        assertEquals(1, errors.size(), "Expected one parse error for mismatched tags");
    }

    @Test
    @DisplayName("parseInput(String,String) with trackPosition enabled assigns positions to nodes")
    public void test_TC17() throws Exception {
        // GIVEN: html parser with position tracking to hit position-assignment branch
        Parser parser = Parser.htmlParser().setTrackPosition(true);
        String html = "<p>Pos</p>";
        String baseUri = "";
        // WHEN: parseInput processes the input with position tracking
        Document doc = parser.parseInput(html, baseUri);
        Element p = doc.selectFirst("p");
        assertNotNull(p, "Expected a <p> element in the document");
        // THEN: first element has non-null source range start; use reflection to access Node.sourceRange().start
        // Reflectively get sourceRange() method
        Method sourceRangeMethod = Node.class.getDeclaredMethod("sourceRange");
        sourceRangeMethod.setAccessible(true);
        Object sourceRange = sourceRangeMethod.invoke(p);
        assertNotNull(sourceRange, "Expected sourceRange object to be non-null");
        // Reflectively get 'start' field from sourceRange
        Field startField = sourceRange.getClass().getDeclaredField("start");
        startField.setAccessible(true);
        Object startValue = startField.get(sourceRange);
        assertNotNull(startValue, "Expected start position to be non-null when trackPosition is enabled");
    }
}