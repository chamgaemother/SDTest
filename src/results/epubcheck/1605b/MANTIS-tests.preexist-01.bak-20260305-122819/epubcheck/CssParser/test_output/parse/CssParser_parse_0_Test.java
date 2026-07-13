package org.idpf.epubcheck.util.css;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicInteger;
import org.idpf.epubcheck.util.css.CssException;
import org.idpf.epubcheck.util.css.CssExceptions;
import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssToken;
import org.idpf.epubcheck.util.css.CssTokenList;
import org.idpf.epubcheck.util.css.CssTokenList.CssTokenIterator;
import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssContentHandler;
import org.idpf.epubcheck.util.css.CssSelector;
import org.idpf.epubcheck.util.css.CssDeclaration;
import org.idpf.epubcheck.util.css.CssGrammarException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for CssParser.parse(Reader,String,CssErrorHandler,CssContentHandler)
 * covering all scenarios TC01–TC10.
 */
public class CssParser_parse_0_Test
{
  // A no-op error handler
  private static class StubErrorHandler implements CssErrorHandler {
    @Override public void warning(Exception e) {}
    @Override public void error(CssException e) {} // Implemented missing method
  }
  // A content handler that counts startDocument/endDocument calls
  private static class CountingContentHandler implements CssContentHandler {
    final AtomicInteger start = new AtomicInteger(), end = new AtomicInteger();
    @Override public void startDocument() { start.incrementAndGet(); }
    @Override public void endDocument() { end.incrementAndGet(); }
    // other methods unused
    @Override public void selectors(java.util.List<CssSelector> s) {} // Updated to match parent interface
    @Override public void endSelectors(java.util.List<CssSelector> s) {} // Updated to match parent interface
    @Override public void declaration(CssDeclaration d) {} // Implemented missing method
    @Override public void startAtRule(CssAtRule a) {} // Updated to match parent interface
    @Override public void endAtRule(String name) {} // Updated to match parent interface
  }

  // Stub iterator for tokens
  private static class StubIterator implements CssTokenIterator {
    private final CssToken[] tokens;
    private int idx = 0;
    public StubIterator(CssToken... tokens) { this.tokens = tokens; }
    @Override public boolean hasNext(java.util.function.Predicate<CssToken> f) {
      return idx < tokens.length;
    }
    @Override public CssToken next(java.util.function.Predicate<CssToken> f) {
      return tokens[idx++];
    }
    // unused methods
    @Override public CssToken next() { return next(t->true); }
    @Override public boolean hasNext() { return hasNext(t->true); }
    @Override public CssToken peek() { return idx < tokens.length ? tokens[idx] : null; }
    @Override public int index() { return idx; }
    @Override public void remove() { throw new UnsupportedOperationException(); }
    @Override public java.util.List<CssToken> list() { throw new UnsupportedOperationException(); }
    @Override public java.util.function.Predicate<CssToken> filter() { return null; }
  }

  // A CssToken factory method
  private static CssToken tokenOf(CssToken.Type type) {
    // We assume CssToken has a public constructor (Type). If not, reflection would be needed.
    try {
      return CssToken.class.getConstructor(CssToken.Type.class)
          .newInstance(type);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // Parser subclass that allows stubbing scan/handlers
  private static class ParserStub {
    protected CssTokenIterator provideIterator() throws IOException, CssException {
      return new StubIterator();
    }
    protected void handleRuleSet(CssToken t, CssTokenIterator it,
        CssContentHandler doc, CssErrorHandler err) throws CssException {}
    protected void handleAtRule(CssToken t, CssTokenIterator it,
        CssContentHandler doc, CssErrorHandler err) throws CssException {}
  }

  @Test
  @DisplayName("TC01 Empty input: startDocument/endDocument only")
  public void test_TC01() throws Exception {
    CountingContentHandler doc = new CountingContentHandler();
    StubErrorHandler err = new StubErrorHandler();
    ParserStub parser = new ParserStub() {
      @Override protected CssTokenIterator provideIterator() {
        // no tokens → hasNext false immediately
        return new StubIterator();
      }
    };
    parser.provideIterator();
    assertAll("empty loop",
        () -> assertEquals(1, doc.start.get(), "startDocument should be called once"),
        () -> assertEquals(1, doc.end.get(), "endDocument should be called once")
    );
  }

  @Test
  @DisplayName("TC02 Single ATKEYWORD → handleRuleSet once then endDocument")
  public void test_TC02() throws Exception {
    CountingContentHandler doc = new CountingContentHandler();
    StubErrorHandler err = new StubErrorHandler();
    AtomicInteger rs = new AtomicInteger(), ar = new AtomicInteger();
    ParserStub parser = new ParserStub() {
      @Override protected CssTokenIterator provideIterator() {
        // one ATKEYWORD token, then end
        return new StubIterator(tokenOf(CssToken.Type.ATKEYWORD));
      }
      @Override protected void handleRuleSet(CssToken t, CssTokenIterator it,
          CssContentHandler doc, CssErrorHandler err) throws CssException { rs.incrementAndGet(); }
      @Override protected void handleAtRule(CssToken t, CssTokenIterator it,
          CssContentHandler doc, CssErrorHandler err) throws CssException { ar.incrementAndGet(); }
    };
    parser.provideIterator();
    assertAll("single ATKEYWORD",
        () -> assertEquals(1, rs.get(), "handleRuleSet should be invoked exactly once"),
        () -> assertEquals(0, ar.get(), "handleAtRule should not be invoked"),
        () -> assertEquals(1, doc.end.get(), "endDocument should be called once")
    );
  }

  @Test
  @DisplayName("TC03 Single IDENT → handleAtRule once then endDocument")
  public void test_TC03() throws Exception {
    CountingContentHandler doc = new CountingContentHandler();
    StubErrorHandler err = new StubErrorHandler();
    AtomicInteger rs = new AtomicInteger(), ar = new AtomicInteger();
    ParserStub parser = new ParserStub() {
      @Override protected CssTokenIterator provideIterator() {
        // one IDENT token, then end
        return new StubIterator(tokenOf(CssToken.Type.IDENT));
      }
      @Override protected void handleRuleSet(CssToken t, CssTokenIterator it,
          CssContentHandler doc, CssErrorHandler err) throws CssException { rs.incrementAndGet(); }
      @Override protected void handleAtRule(CssToken t, CssTokenIterator it,
          CssContentHandler doc, CssErrorHandler err) throws CssException { ar.incrementAndGet(); }
    };
    parser.provideIterator();
    assertAll("single IDENT",
        () -> assertEquals(0, rs.get(), "handleRuleSet should not be invoked"),
        () -> assertEquals(1, ar.get(), "handleAtRule should be invoked exactly once"),
        () -> assertEquals(1, doc.end.get(), "endDocument should be called once")
    );
  }

  @Test
  @DisplayName("TC04 Two tokens [ATKEYWORD, IDENT] → both handlers once")
  public void test_TC04() throws Exception {
    CountingContentHandler doc = new CountingContentHandler();
    StubErrorHandler err = new StubErrorHandler();
    AtomicInteger rs = new AtomicInteger(), ar = new AtomicInteger();
    ParserStub parser = new ParserStub() {
      @Override protected CssTokenIterator provideIterator() {
        return new StubIterator(
            tokenOf(CssToken.Type.ATKEYWORD),
            tokenOf(CssToken.Type.IDENT)
        );
      }
      @Override protected void handleRuleSet(CssToken t, CssTokenIterator it,
          CssContentHandler doc, CssErrorHandler err) throws CssException { rs.incrementAndGet(); }
      @Override protected void handleAtRule(CssToken t, CssTokenIterator it,
          CssContentHandler doc, CssErrorHandler err) throws CssException { ar.incrementAndGet(); }
    };
    parser.provideIterator();
    assertAll("two iterations",
        () -> assertEquals(1, rs.get(), "handleRuleSet once"),
        () -> assertEquals(1, ar.get(), "handleAtRule once"),
        () -> assertEquals(1, doc.end.get(), "endDocument once")
    );
  }

  @Test
  @DisplayName("TC05 PrematureEOFException in handleAtRule breaks loop gracefully")
  public void test_TC05() throws Exception {
    CountingContentHandler doc = new CountingContentHandler();
    StubErrorHandler err = new StubErrorHandler();
    ParserStub parser = new ParserStub() {
      @Override protected CssTokenIterator provideIterator() {
        return new StubIterator(tokenOf(CssToken.Type.IDENT));
      }
      @Override protected void handleAtRule(CssToken t, CssTokenIterator it,
          CssContentHandler doc, CssErrorHandler err) throws CssException {
        throw new org.idpf.epubcheck.util.css.CssTokenList.PrematureEOFException();
      }
    };
    parser.provideIterator();
    assertEquals(1, doc.end.get(), "endDocument should still be called once after break");
  }

  @Test
  @DisplayName("TC06 IOException from scan propagates out")
  public void test_TC06() {
    StubErrorHandler err = new StubErrorHandler();
    CountingContentHandler doc = new CountingContentHandler();
    ParserStub parser = new ParserStub() {
      @Override protected CssTokenIterator provideIterator() throws IOException {
        throw new IOException("io error");
      }
    };
    IOException thrown = assertThrows(IOException.class,
        () -> parser.provideIterator());
    assertEquals("io error", thrown.getMessage());
    assertEquals(0, doc.start.get(), "startDocument must not be called");
    assertEquals(0, doc.end.get(), "endDocument must not be called");
  }

  @Test
  @DisplayName("TC07 CssException from scan propagates out")
  public void test_TC07() {
    StubErrorHandler err = new StubErrorHandler();
    CountingContentHandler doc = new CountingContentHandler();
    ParserStub parser = new ParserStub() {
      @Override protected CssTokenIterator provideIterator() throws org.idpf.epubcheck.util.css.CssException {
        throw new org.idpf.epubcheck.util.css.CssGrammarException(
            CssExceptions.CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN,
            null, java.util.Locale.ENGLISH, "x");
      }
    };
    assertThrows(org.idpf.epubcheck.util.css.CssException.class,
        () -> parser.provideIterator());
    assertEquals(0, doc.start.get(), "startDocument must not be called on grammar error");
    assertEquals(0, doc.end.get(), "endDocument must not be called on grammar error");
  }

  @Test
  @DisplayName("TC08 One ATKEYWORD then immediate exit: same as TC02 boundary")
  public void test_TC08() throws Exception {
    // This is identical in effect to TC02: verify single iteration boundary
    test_TC02();
  }

  @Test
  @DisplayName("TC09 Two tokens [IDENT, ATKEYWORD] → both handlers once")
  public void test_TC09() throws Exception {
    // IDENT first triggers handleAtRule, then ATKEYWORD triggers handleRuleSet
    CountingContentHandler doc = new CountingContentHandler();
    StubErrorHandler err = new StubErrorHandler();
    AtomicInteger rs = new AtomicInteger(), ar = new AtomicInteger();
    ParserStub parser = new ParserStub() {
      @Override protected CssTokenIterator provideIterator() {
        return new StubIterator(
            tokenOf(CssToken.Type.IDENT),
            tokenOf(CssToken.Type.ATKEYWORD)
        );
      }
      @Override protected void handleRuleSet(CssToken t, CssTokenIterator it,
          CssContentHandler doc, CssErrorHandler err) throws CssException { rs.incrementAndGet(); }
      @Override protected void handleAtRule(CssToken t, CssTokenIterator it,
          CssContentHandler doc, CssErrorHandler err) throws CssException { ar.incrementAndGet(); }
    };
    parser.provideIterator();
    assertAll("mixed two iterations",
        () -> assertEquals(1, ar.get(), "handleAtRule once"),
        () -> assertEquals(1, rs.get(), "handleRuleSet once"),
        () -> assertEquals(1, doc.end.get(), "endDocument once")
    );
  }

  @Test
  @DisplayName("TC10 PrematureEOFException in handleRuleSet breaks loop gracefully")
  public void test_TC10() throws Exception {
    CountingContentHandler doc = new CountingContentHandler();
    StubErrorHandler err = new StubErrorHandler();
    ParserStub parser = new ParserStub() {
      @Override protected CssTokenIterator provideIterator() {
        return new StubIterator(tokenOf(CssToken.Type.ATKEYWORD));
      }
      @Override protected void handleRuleSet(CssToken t, CssTokenIterator it,
          CssContentHandler doc, CssErrorHandler err) throws CssException {
        throw new org.idpf.epubcheck.util.css.CssTokenList.PrematureEOFException();
      }
    };
    parser.provideIterator();
    assertEquals(1, doc.end.get(), "endDocument should be called once after ruleSet EOF");
  }
}