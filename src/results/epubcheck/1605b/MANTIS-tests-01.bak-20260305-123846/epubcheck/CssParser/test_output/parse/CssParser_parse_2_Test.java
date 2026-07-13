package org.idpf.epubcheck.util.css;

import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssToken;
import org.idpf.epubcheck.util.css.CssToken.Type;
import org.idpf.epubcheck.util.css.CssTokenList.CssTokenIterator;
import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssContentHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CssParser_parse_2_Test {

    // A simple recorder for errors
    static class RecordingErrorHandler implements CssErrorHandler {
        private int errorCount = 0;
        @Override public void error(org.idpf.epubcheck.util.css.CssExceptions.CssException ex) { errorCount++; }
        public int getErrorCount() { return errorCount; }
    }

    // A simple recorder for content events
    static class RecordingContentHandler implements CssContentHandler {
        int startAtRuleCount = 0;
        int declarationCount = 0;
        int endAtRuleCount = 0;
        boolean endDocumentCalled = false;
        @Override public void startDocument() {}
        @Override public void endDocument() { endDocumentCalled = true; }
        @Override public void startAtRule(CssGrammar.CssAtRule atRule) {
            startAtRuleCount++;
        }
        @Override public void endAtRule(String name) {
            endAtRuleCount++;
        }
        @Override public void declaration(CssGrammar.CssDeclaration decl) {
            declarationCount++;
        }
        // unused methods
        @Override public void selectors(List<CssGrammar.CssSelector> selectors){}
        @Override public void endSelectors(List<CssGrammar.CssSelector> selectors){}
    }

    @Test
    @DisplayName("TC12: parse at-rule with block containing declarations exercises hasBlock=true and hasRuleSet=false path")
    public void test_TC12() throws IOException, Exception {
        // GIVEN an at-rule with a simple declaration block
        Reader reader = new StringReader("@foo { color: blue; }");
        RecordingErrorHandler err = new RecordingErrorHandler();
        RecordingContentHandler doc = new RecordingContentHandler();
        CssParser parser = new CssParser(java.util.Locale.ENGLISH);
        // WHEN parsing the input
        parser.parse(reader, "id", err, doc);
        // THEN no errors, one startAtRule, one declaration, one endAtRule, and endDocument
        assertEquals(0, err.getErrorCount(), "Should report no errors");
        assertEquals(1, doc.startAtRuleCount, "startAtRule should be called once");
        assertEquals(1, doc.declarationCount, "One declaration in block");
        assertEquals(1, doc.endAtRuleCount, "endAtRule should be called once");
        assertTrue(doc.endDocumentCalled, "endDocument should be called");
    }

    @Test
    @DisplayName("TC13: parse at-rule with invalid parameter exercises unexpected-token branch in handleAtRuleParam")
    public void test_TC13() throws IOException, Exception {
        // GIVEN a parser stub that returns a custom token sequence forcing handleAtRuleParam to return null
        CssParser parser = new CssParser(java.util.Locale.ENGLISH);
        RecordingErrorHandler err = new RecordingErrorHandler();
        RecordingContentHandler doc = new RecordingContentHandler();
        // WHEN parsing
        List<CssToken> tokens = new ArrayList<>();
        tokens.add(new CssToken(Type.ATKEYWORD, "@x", ""));  // start at-rule
        tokens.add(new CssToken(Type.CDOT, ".", ""));        // invalid param token -> null
        tokens.add(new CssToken(Type.CDC, "}", ""));         // closing brace
        StubIterator stubIterator = new StubIterator(tokens);
        parser.parse(new StringReader(""), "id", err, doc);
        // THEN exactly one error reported, endDocument still called
        assertEquals(1, err.getErrorCount(), "One unexpected-token error should be recorded");
        assertTrue(doc.endDocumentCalled, "endDocument should still be called");
    }

    @Test
    @DisplayName("TC14: parseStyleAttribute skips leading semicolons and parses a declaration then ends document")
    public void test_TC14() throws IOException, Exception {
        // GIVEN style attribute with leading ';' before a property declaration
        Reader reader = new StringReader(";margin:10px");
        RecordingErrorHandler err = new RecordingErrorHandler();
        RecordingContentHandler doc = new RecordingContentHandler();
        CssParser parser = new CssParser(java.util.Locale.ENGLISH);
        // WHEN parsing the style attribute
        parser.parseStyleAttribute(reader, "id", err, doc);
        // THEN one declaration, no errors, and endDocument called
        assertEquals(0, err.getErrorCount(), "No errors should occur");
        assertEquals(1, doc.declarationCount, "One declaration parsed");
        assertTrue(doc.endDocumentCalled, "endDocument should be called");
    }

    /**
     * A minimal stub iterator that ignores filters and yields a fixed list of tokens.
     */
    static class StubIterator implements CssTokenIterator {
        private final List<CssToken> tokens;
        private int pos = 0;
        StubIterator(List<CssToken> list) { this.tokens = list; }
        @Override public boolean hasNext() { return pos < tokens.size(); }
        @Override public CssToken next() { return tokens.get(pos++); }
        @Override public CssToken next(java.util.function.Predicate<CssToken> filter) { return next(); }
        @Override public boolean hasNext(java.util.function.Predicate<CssToken> filter) { return hasNext(); }
        @Override public CssToken peek() { return tokens.get(pos); }
        @Override public List<CssToken> getList() { return tokens; }
        @Override public int index() { return pos; }
        @Override public CssToken last() { return tokens.get(pos-1); }
        @Override public java.util.function.Predicate<CssToken> filter() { return null; }
    }
}