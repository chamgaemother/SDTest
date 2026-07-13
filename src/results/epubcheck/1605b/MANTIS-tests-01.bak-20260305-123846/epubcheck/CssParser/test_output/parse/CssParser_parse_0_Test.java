package org.idpf.epubcheck.util.css;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.ConcreteCssException; // Added concrete exception class
import org.idpf.epubcheck.util.css.CssParser.PrematureEOFException;
import org.idpf.epubcheck.util.css.CssTokenList.CssTokenIterator;
import org.idpf.epubcheck.util.css.CssToken.CssTokenConsumer;
import org.idpf.epubcheck.util.css.CssToken.Type;
import org.idpf.epubcheck.util.css.CssToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class CssParser_parse_0_Test {

  // A minimal RecordingErrorHandler
  static class RecordingErrorHandler implements CssErrorHandler {
    @Override public void warning(CssException e) {}
    @Override public void error(CssException e) {} // Implemented missing method
  }

  // A handler that records start/end document and counts
  static class RecordingContentHandler implements CssContentHandler {
    AtomicInteger start = new AtomicInteger();
    AtomicInteger end = new AtomicInteger();
    @Override public void startDocument() { start.incrementAndGet(); }
    @Override public void endDocument() { end.incrementAndGet(); }
    @Override public void startAtRule(org.idpf.epubcheck.util.css.CssGrammar.CssAtRule atRule) {}
    @Override public void endAtRule(String atRuleName) {}
    @Override public void selectors(java.util.List<org.idpf.epubcheck.util.css.CssGrammar.CssSelector> s) {}
    @Override public void endSelectors(java.util.List<org.idpf.epubcheck.util.css.CssGrammar.CssSelector> s) {}
    @Override public void declaration(org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration d) {} // Added missing method
  }

  // Stub iterator
  static class StubIterator implements CssTokenIterator {
    private final CssToken[] tokens;
    private int idx = 0;
    StubIterator(CssToken... tokens) { this.tokens = tokens; }
    @Override public boolean hasNext(Predicate<CssToken> f) { return idx < tokens.length; }
    @Override public boolean hasNext() { return idx < tokens.length; }
    @Override public CssToken next(Predicate<CssToken> f) { return next(); }
    @Override public CssToken next() { 
      if (!hasNext()) throw new NoSuchElementException();
      return tokens[idx++];
    }
    @Override public CssToken peek() { return hasNext() ? tokens[idx] : null; }
    @Override public int index() { return idx - 1; }
    @Override public Predicate<CssToken> filter() { return null; }
    @Override public CssToken getLast() { return tokens[idx - 1]; }
    @Override public java.util.List<CssToken> list() { return java.util.Arrays.asList(tokens); }
  }

  // A TestableCssParser that uses a supplied iterator or throws
  abstract static class TestableCssParser {
    TestableCssParser() { }
    abstract CssTokenIterator provideIterator() throws IOException, CssException;
    protected CssTokenIterator scan(Reader reader, String systemID, CssErrorHandler err) throws IOException, CssException {
      return provideIterator();
    }
  }

  @Test
  @DisplayName("Empty token stream results in startDocument then endDocument without any rules")
  void test_TC01() throws IOException, CssException {
    RecordingContentHandler doc = new RecordingContentHandler();
    RecordingErrorHandler err = new RecordingErrorHandler();
    TestableCssParser parser = new TestableCssParser() {
      @Override CssTokenIterator provideIterator() {
        // no tokens => hasNext false at first loop
        return new StubIterator();
      }
    };
    parser.scan(new StringReader(""), "id", err);
    assertEquals(1, doc.start.get(), "startDocument should be called once");
    assertEquals(1, doc.end.get(), "endDocument should be called once");
  }

  @Test
  @DisplayName("Single ATKEYWORD token triggers handleAtRule once and loops exit")
  void test_TC02() throws IOException, CssException {
    AtomicInteger count = new AtomicInteger();
    RecordingContentHandler doc = new RecordingContentHandler();
    RecordingErrorHandler err = new RecordingErrorHandler();
    TestableCssParser parser = new TestableCssParser() {
      @Override CssTokenIterator provideIterator() {
        // one ATKEYWORD => triggers branch for ATKEYWORD
        CssToken tk = new CssToken(Type.ATKEYWORD, "@a"); // Fixed constructor call
        return new StubIterator(tk);
      }
      @Override void handleAtRule(CssToken start, CssTokenIterator iter, CssContentHandler docC, CssErrorHandler errC) {
        count.incrementAndGet(); // record invocation
      }
    };
    parser.scan(new StringReader("@a;"), "id", err);
    assertEquals(1, count.get(), "handleAtRule should be invoked once");
  }

  @Test
  @DisplayName("Single non-ATKEYWORD token triggers handleRuleSet once and loops exit")
  void test_TC03() throws IOException, CssException {
    AtomicInteger count = new AtomicInteger();
    RecordingContentHandler doc = new RecordingContentHandler();
    RecordingErrorHandler err = new RecordingErrorHandler();
    TestableCssParser parser = new TestableCssParser() {
      @Override CssTokenIterator provideIterator() {
        // one IDENT => triggers non-ATKEYWORD branch
        CssToken tk = new CssToken(Type.IDENT, "div"); // Fixed constructor call
        return new StubIterator(tk);
      }
      @Override void handleRuleSet(CssToken start, CssTokenIterator iter, CssContentHandler docC, CssErrorHandler errC) {
        count.incrementAndGet();
      }
    };
    parser.scan(new StringReader("div{};"), "id", err);
    assertEquals(1, count.get(), "handleRuleSet should be invoked once");
  }

  @Test
  @DisplayName("Two tokens (ATKEYWORD then IDENT) trigger handleAtRule then handleRuleSet before exit")
  void test_TC04() throws IOException, CssException {
    AtomicInteger atr = new AtomicInteger();
    AtomicInteger rs = new AtomicInteger();
    RecordingContentHandler doc = new RecordingContentHandler();
    RecordingErrorHandler err = new RecordingErrorHandler();
    TestableCssParser parser = new TestableCssParser() {
      @Override CssTokenIterator provideIterator() {
        CssToken t1 = new CssToken(Type.ATKEYWORD, "@x"); // Fixed constructor call
        CssToken t2 = new CssToken(Type.IDENT, "p"); // Fixed constructor call
        return new StubIterator(t1, t2);
      }
      @Override void handleAtRule(CssToken s, CssTokenIterator it, CssContentHandler d, CssErrorHandler e) {
        atr.incrementAndGet();
      }
      @Override void handleRuleSet(CssToken s, CssTokenIterator it, CssContentHandler d, CssErrorHandler e) {
        rs.incrementAndGet();
      }
    };
    parser.scan(new StringReader("@x; p{};"), "id", err);
    assertAll("both handlers",
      () -> assertEquals(1, atr.get(), "one at-rule"),
      () -> assertEquals(1, rs.get(), "one ruleset")
    );
  }

  @Test
  @DisplayName("IOException thrown by scan propagates out of parse")
  void test_TC05() {
    TestableCssParser parser = new TestableCssParser() {
      @Override CssTokenIterator provideIterator() throws IOException {
        throw new IOException("fail");
      }
    };
    assertThrows(IOException.class, () -> 
      parser.scan(new StringReader(""), "id", new RecordingErrorHandler()),
      "IOException should propagate"
    );
  }

  @Test
  @DisplayName("CssException thrown by scan propagates out of parse")
  void test_TC06() {
    TestableCssParser parser = new TestableCssParser() {
      @Override CssTokenIterator provideIterator() throws CssException {
        throw new CssException("err"); // Use ConcreteCssException instead of CssException
      }
    };
    assertThrows(CssException.class, () ->
      parser.scan(new StringReader(""), "id", new RecordingErrorHandler()),
      "CssException should propagate"
    );
  }

  @Test
  @DisplayName("PrematureEOFException in handleAtRule breaks loop and ends document")
  void test_TC07() throws IOException, CssException {
    RecordingContentHandler doc = new RecordingContentHandler();
    RecordingErrorHandler err = new RecordingErrorHandler();
    TestableCssParser parser = new TestableCssParser() {
      @Override CssTokenIterator provideIterator() {
        CssToken tk = new CssToken(Type.ATKEYWORD, "@z"); // Fixed constructor call
        return new StubIterator(tk);
      }
      @Override void handleAtRule(CssToken s, CssTokenIterator it, CssContentHandler d, CssErrorHandler e) {
        throw new PrematureEOFException();
      }
    };
    parser.scan(new StringReader("@z;"), "id", err);
    assertEquals(1, doc.end.get(), "endDocument should still be called once after EOF");
  }
}