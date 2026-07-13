package org.idpf.epubcheck.util.css;

import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssContentHandler;
import org.idpf.epubcheck.util.css.CssException;
import org.idpf.epubcheck.util.css.CssGrammarException;
import org.idpf.epubcheck.util.css.CssToken;
import org.idpf.epubcheck.util.css.CssTokenList.CssTokenIterator;
import org.idpf.epubcheck.util.css.CssSelectorConstructFactory;
import org.idpf.epubcheck.util.css.CssSelector;
import org.idpf.epubcheck.util.css.CssDeclaration;
import org.idpf.epubcheck.util.css.CssTokenList.Filters;
import org.idpf.epubcheck.util.css.CssGrammar.CssAtRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CssParser_parse_2_Test {

    // A simple Recording handler to capture calls for assertions
    static class RecordingContentHandler implements CssContentHandler {
        List<String> calls = new ArrayList<>();
        @Override public void startDocument() { calls.add("startDocument"); }
        @Override public void endDocument() { calls.add("endDocument"); }
        @Override public void startAtRule(CssAtRule atRule) {
            calls.add("startAtRule(" + atRule.getName().get() + ")"); }
        @Override public void endAtRule(String name) { calls.add("endAtRule(" + name + ")"); }
        @Override public void selectors(List<CssSelector> selectors) { /* not needed */ }
        @Override public void endSelectors(List<CssSelector> selectors) { /* not needed */ }
        @Override public void declaration(CssDeclaration decl) { calls.add("declaration"); }
    }

    // A simple Recording error handler that does nothing
    static class RecordingErrorHandler implements CssErrorHandler {
        List<CssException> errors = new ArrayList<>();
        @Override public void error(CssException e) { errors.add(e); }
        @Override public void warning(CssGrammarException e) { /* not used */ }
    }

    @Test
    @DisplayName("parse at-rule with block containing a property declaration exercises hasBlock=true and handleDeclarationBlock path in handleAtRule")
    void test_TC10() throws IOException, CssException {
        // Input has an at-rule foo with a declaration color:red; so hasBlock=true and goes through handleDeclarationBlock
        Reader reader = new StringReader("@foo{color:red;}\n");
        RecordingErrorHandler err = new RecordingErrorHandler();
        RecordingContentHandler doc = new RecordingContentHandler();
        // Use English locale to avoid locale variations
        CssParser parser = new CssParser(Locale.ENGLISH);
        parser.parse(reader, "id", err, doc);
        // Verify sequence of calls
        List<String> expected = List.of(
            "startDocument",
            "startAtRule(foo)",
            "declaration",
            "endAtRule(foo)",
            "endDocument"
        );
        assertEquals(expected, doc.calls);
        // No errors expected
        assertEquals(0, err.errors.size());
    }

    @Test
    @DisplayName("parse ruleset where selector list factory returns null triggers early return path in handleRuleSet")
    void test_TC11() throws Exception {
        // Input defines a simple ruleset a{}; stub selector factory to return null to trigger early exit
        Reader reader = new StringReader("a{}\n");
        RecordingErrorHandler err = new RecordingErrorHandler();
        RecordingContentHandler doc = new RecordingContentHandler();
        CssParser parser = new CssParser(Locale.ENGLISH);
        // Use reflection to replace private cssSelectorFactory with stub returning null
        Field factoryField = CssParser.class.getDeclaredField("cssSelectorFactory");
        factoryField.setAccessible(true);
        // Stub that always returns null to simulate selector error
        CssSelectorConstructFactory stubFactory = new CssSelectorConstructFactory(Locale.ENGLISH) {
            @Override
            public List<CssSelector> createSelectorList(CssToken start, CssTokenIterator iter, CssErrorHandler err) {
                return null;
            }
        };
        factoryField.set(parser, stubFactory);
        // Execute parse, expecting only startDocument and endDocument
        parser.parse(reader, "id", err, doc);
        List<String> expected = List.of(
            "startDocument",
            "endDocument"
        );
        assertEquals(expected, doc.calls);
        // Should not throw, but record that no selectors or declarations added
        assertEquals(0, err.errors.size());
    }
}