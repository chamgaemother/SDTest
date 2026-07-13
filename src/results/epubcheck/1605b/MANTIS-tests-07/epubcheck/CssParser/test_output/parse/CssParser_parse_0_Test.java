package org.idpf.epubcheck.util.css;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssGrammarException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Generated test class for CssParser.parse(Reader, String, CssErrorHandler, CssContentHandler)
 * covering scenarios TC01–TC10.
 */
public class CssParser_parse_0_Test {

  /**
   * A stubbed CssErrorHandler that does nothing.
   */
  static class StubErrorHandler implements CssErrorHandler {
    @Override public void error(CssGrammarException exception) { /* no-op */ }
    @Override public void error(CssException exception) { /* no-op */ }
  }

  /**
   * A stubbed CssContentHandler that tracks startDocument/endDocument calls.
   */
  static class StubContentHandler implements CssContentHandler {
    int startCount = 0;
    int endCount = 0;

    @Override public void startDocument() { startCount++; }
    @Override public void endDocument() { endCount++; }
    // other methods are no-ops:
    @Override public void selectors(java.util.List<CssSelector> selectors) { }
    @Override public void endSelectors(java.util.List<CssSelector> selectors) { }
    @Override public void declaration(CssDeclaration declaration) { }
    @Override public void startAtRule(CssAtRule rule) { }
    @Override public void endAtRule(String name) { }
  }

  /**
   * A minimal fake iterator supporting hasNext(filter) and next(filter).
   */
  static class FakeIterator implements CssTokenIterator {
    final CssToken[] tokens;
    int idx = 0;
    final RuntimeException nextException;
    final Predicate<CssToken> defaultFilter = t -> true;
    CssToken last;

    FakeIterator(CssToken[] tokens, RuntimeException nextException) {
      this.tokens = tokens;
      this.nextException = nextException;
    }

    @Override
    public boolean hasNext() { return idx < tokens.length; }

    @Override
    public CssToken next() {
      if (nextException != null && idx == tokens.length) {
        throw nextException;
      }
      if (!hasNext()) throw new NoSuchElementException();
      last = tokens[idx++];
      return last;
    }

    @Override
    public CssToken next(Predicate<CssToken> filter) {
      return next();
    }

    @Override
    public boolean hasNext(Predicate<CssToken> filter) {
      return hasNext();
    }
    @Override public int index() { return idx; }
    @Override public CssToken peek() { return idx < tokens.length ? tokens[idx] : null; }
    @Override public java.util.List<CssToken> list() { throw new UnsupportedOperationException(); }
    @Override public Predicate<CssToken> filter() { return defaultFilter; }
  }

  private Reader rdr(String s) { return new StringReader(s); }

  @Test
  @DisplayName("TC01: parse(): scanner yields no tokens, endDocument only (hasNext false)")
  void test_TC01() throws Exception {
    // No tokens => iterator.hasNext(false) immediately => startDocument/endDocument only
    FakeIterator it = new FakeIterator(new CssToken[0], null);
    CssParser p = new CssParser();
    StubContentHandler doc = new StubContentHandler();
    p.parse(rdr(""), "sid", new StubErrorHandler(), doc);
    assertEquals(1, doc.startCount, "startDocument should be called once");
    assertEquals(1, doc.endCount, "endDocument should be called once");
  }

  @Test
  @DisplayName("TC02: parse(): one token ATKEYWORD, handleAtRule invoked once then endDocument")
  void test_TC02() throws Exception {
    // Single ATKEYWORD token => branch to handleAtRule
    CssToken t = new CssToken(CssToken.Type.ATKEYWORD, "@x", new CssLocation(0, 0), 0);
    FakeIterator it = new FakeIterator(new CssToken[]{t}, null);
    CssParser p = new CssParser();
    StubContentHandler doc = new StubContentHandler();
    p.parse(rdr(""), "sid", new StubErrorHandler(), doc);
    assertEquals(1, doc.startCount);
    assertEquals(1, doc.endCount);
  }

  @Test
  @DisplayName("TC03: parse(): one token non-ATKEYWORD, handleRuleSet invoked once then endDocument")
  void test_TC03() throws Exception {
    // Single IDENT token => branch to handleRuleSet
    CssToken t = new CssToken(CssToken.Type.IDENT, "id", new CssLocation(0, 0), 0);
    FakeIterator it = new FakeIterator(new CssToken[]{t}, null);
    CssParser p = new CssParser();
    StubContentHandler doc = new StubContentHandler();
    p.parse(rdr(""), "sid", new StubErrorHandler(), doc);
    assertEquals(1, doc.startCount);
    assertEquals(1, doc.endCount);
  }

  @Test
  @DisplayName("TC10: parse(): mixed loop of 1 IDENT then next throws NoSuchElementException")
  void test_TC10() throws Exception {
    // First iteration: IDENT; second next() throws NSE
    CssToken t = new CssToken(CssToken.Type.IDENT, "x", new CssLocation(0, 0), 0);
    FakeIterator it = new FakeIterator(new CssToken[]{t}, new NoSuchElementException());
    CssParser p = new CssParser();
    StubContentHandler doc = new StubContentHandler();
    p.parse(rdr(""), "sid", new StubErrorHandler(), doc);
    assertEquals(1, doc.startCount);
    assertEquals(1, doc.endCount);
  }
}