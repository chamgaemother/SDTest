package org.idpf.epubcheck.util.css;

import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssException;
import org.idpf.epubcheck.util.css.CssGrammar;
import org.idpf.epubcheck.util.css.CssGrammarException;
import org.idpf.epubcheck.util.css.CssToken;
import org.idpf.epubcheck.util.css.CssTokenList;
import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssDeclaration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class CssParser_parse_1_Test {

    // A fake iterator to simulate hasNext/next behavior for tokens
    static class FakeIterator implements CssTokenList.CssTokenIterator {
        private final CssToken[] tokens;
        private final RuntimeException toThrow;
        private int index = -1;

        FakeIterator(CssToken[] tokens, RuntimeException toThrow) {
            this.tokens = tokens;
            this.toThrow = toThrow;
        }

        @Override
        public boolean hasNext() {
            return toThrow == null && index + 1 < tokens.length;
        }

        @Override
        public CssToken next() {
            throw new UnsupportedOperationException("Use next(Predicate) instead");
        }

        @Override
        public CssToken next(java.util.function.Predicate<CssToken> filter) {
            if (toThrow != null) {
                // simulate immediate failure on next()
                throw toThrow;
            }
            if (++index < tokens.length) {
                return tokens[index];
            }
            throw new NoSuchElementException();
        }

        // unused methods for this test suite
        @Override public int index() { return index; }
        @Override public java.util.List<CssToken> list() { return java.util.Arrays.asList(tokens); }
        @Override public CssToken last() { return tokens[index]; }
        @Override public CssToken peek() { return tokens[index]; }
        @Override public java.util.function.Predicate<CssToken> filter() { return null; }
    }

    static class StubErrorHandler implements CssErrorHandler {
        @Override
        public void error(CssGrammarException e) {
            // ignore errors
        }
    }

    @Test
    @DisplayName("TC11: parse(): iterates twice over two IDENT tokens, calling handleRuleSet each time")
    void test_TC11() throws Exception {
        // Prepare two IDENT tokens so hasNext true twice then false, triggering two ruleSet branches
        CssToken t1 = new CssToken(CssToken.Type.IDENT, "a", "", 0);
        CssToken t2 = new CssToken(CssToken.Type.IDENT, "b", "", 1);
        FakeIterator fakeIt = new FakeIterator(new CssToken[]{t1, t2}, null);

        // stub document to count startDocument, endDocument and selectors calls
        AtomicInteger startCount = new AtomicInteger();
        AtomicInteger endCount = new AtomicInteger();
        AtomicInteger rulesetCount = new AtomicInteger();

        CssParser parser = new CssParser() {
            @Override
            protected CssTokenList.CssTokenIterator scan(Reader reader, String systemID, CssErrorHandler err) {
                return fakeIt;
            }
        };

        CssContentHandler doc = new CssContentHandler() {
            @Override public void startDocument() { startCount.incrementAndGet(); }
            @Override public void endDocument() { endCount.incrementAndGet(); }
            @Override public void selectors(java.util.List<CssGrammar.CssSelector> selectors) { rulesetCount.incrementAndGet(); }
            // other methods no-op
            @Override public void endSelectors(java.util.List<CssGrammar.CssSelector> selectors) {}
            @Override public void declaration(CssDeclaration decl) {}
            @Override public void startAtRule(CssGrammar.CssAtRule a) {}
            @Override public void endAtRule(String name) {}
        };

        // Execute parse, two IDENTs => two calls to handleRuleSet via selectors()
        parser.parse(new StringReader(""), "sid", new StubErrorHandler(), doc);

        // Assertions
        assertEquals(1, startCount.get(), "startDocument should be called once");
        assertEquals(1, endCount.get(), "endDocument should be called once");
        assertEquals(2, rulesetCount.get(), "Two rule sets should have been processed");
    }

    @Test
    @DisplayName("TC12: parse(): next(filter) throws NoSuchElementException on first iteration, triggers catch and exits")
    void test_TC12() throws Exception {
        // FakeIterator that throws NSE immediately
        FakeIterator fakeIt = new FakeIterator(new CssToken[0], new NoSuchElementException());

        AtomicInteger startCount = new AtomicInteger();
        AtomicInteger endCount = new AtomicInteger();

        CssParser parser = new CssParser() {
            @Override
            protected CssTokenList.CssTokenIterator scan(Reader reader, String systemID, CssErrorHandler err) {
                return fakeIt;
            }
        };

        CssContentHandler doc = new CssContentHandler() {
            @Override public void startDocument() { startCount.incrementAndGet(); }
            @Override public void endDocument() { endCount.incrementAndGet(); }
            @Override public void selectors(java.util.List<CssGrammar.CssSelector> selectors) {}
            @Override public void endSelectors(java.util.List<CssGrammar.CssSelector> selectors) {}
            @Override public void declaration(CssDeclaration decl) {}
            @Override public void startAtRule(CssGrammar.CssAtRule a) {}
            @Override public void endAtRule(String name) {}
        };

        // The NoSuchElementException is caught internally; parse completes normally
        parser.parse(new StringReader(""), "sid", new StubErrorHandler(), doc);

        assertEquals(1, startCount.get(), "startDocument should still be called once");
        assertEquals(1, endCount.get(), "endDocument should still be called once");
    }

    @Test
    @DisplayName("TC13: parse(): scan throws IOException, propagates IOException")
    void test_TC13() {
        CssParser parser = new CssParser() {
            @Override
            protected CssTokenList.CssTokenIterator scan(Reader reader, String systemID, CssErrorHandler err) throws IOException {
                throw new IOException("io err");
            }
        };
        // parse should propagate IOException
        IOException ex = assertThrows(IOException.class, () ->
            parser.parse(new StringReader(""), "sid", new StubErrorHandler(), doc -> {})
        );
        assertTrue(ex.getMessage().contains("io err"));
    }

    @Test
    @DisplayName("TC14: parse(): scan throws CssException, propagates CssException")
    void test_TC14() {
        CssParser parser = new CssParser() {
            @Override
            protected CssTokenList.CssTokenIterator scan(Reader reader, String systemID, CssErrorHandler err) throws CssException {
                throw new CssGrammarException(org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode.GRAMMAR_PREMATURE_EOF,
                                              null, java.util.Locale.getDefault(), ";");
            }
        };
        // parse should propagate CssException
        assertThrows(CssException.class, () ->
            parser.parse(new StringReader(""), "sid", new StubErrorHandler(), doc -> {})
        );
    }
}