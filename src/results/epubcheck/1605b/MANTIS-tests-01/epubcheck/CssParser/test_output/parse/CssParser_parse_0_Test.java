package org.idpf.epubcheck.util.css;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Locale;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssGrammarException;
import org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * JUnit 5 tests for CssParser.parse(Reader, String, CssErrorHandler, CssContentHandler).
 *  
 * Each test injects a fake TokenIterator via reflection into a spy CssParser
 * so as to drive the loop and branch behavior without invoking the real scanner.
 * handleAtRule and handleRuleSet are also spied via reflection to simulate calls
 * or exceptions as per each scenario.
 */
public class CssParser_parse_0_Test {

  /**
   * Utility: replace private scan(...) method on parserSpy to return our fake iterator.
   */
  private void stubScan(CssParser parserSpy, CssTokenIterator fakeIter) throws Exception {
    Method scanM = CssParser.class.getDeclaredMethod("scan", Reader.class, String.class, CssErrorHandler.class);
    scanM.setAccessible(true);
    // use Mockito to stub the private method:
    doReturn(fakeIter).when(parserSpy).getClass()
      .getDeclaredMethod("scan", Reader.class, String.class, CssErrorHandler.class)
      .invoke(parserSpy, any(Reader.class), anyString(), any(CssErrorHandler.class));
    // Note: above is conceptual; in real PowerMockito or ByteBuddy would be required.
  }

  /**
   * Utility: create a trivial CssTokenIterator that has no tokens.
   */
  private CssTokenIterator emptyIterator() {
    return new CssTokenIterator(null, CssToken.FILTER_S_CMNT) {
      public boolean hasNext() { return false; }
      public boolean hasNext(java.util.function.Predicate<CssToken> p) { return false; }
      public CssToken next() { throw new java.util.NoSuchElementException(); }
      public CssToken next(java.util.function.Predicate<CssToken> p) { throw new java.util.NoSuchElementException(); }
    };
  }

  /**
   * Utility: create a CssTokenIterator yielding exactly one token of given type.
   */
  private CssTokenIterator singleTokenIterator(CssToken.Type type) {
    final CssToken tok = new CssToken(type, "tok", CssLocation.create(1, 1));
    return new CssTokenIterator(java.util.Collections.singletonList(tok), CssToken.FILTER_S_CMNT_CDO_CDC) {
      private boolean used = false;
      public boolean hasNext() { return !used; }
      public boolean hasNext(java.util.function.Predicate<CssToken> p) { return !used && p.test(tok); }
      public CssToken next() {
        used = true;
        return tok;
      }
      public CssToken next(java.util.function.Predicate<CssToken> p) {
        if (!p.test(tok)) throw new java.util.NoSuchElementException();
        used = true;
        return tok;
      }
    };
  }

  /**
   * TC01: loop-0 → B0→B1→B8.
   * Stub scan() → empty iterator, so hasNext()==false immediately.
   * Expect startDocument() & endDocument() called once on doc; no handle* calls.
   */
  @Test
  @DisplayName("TC01: zero tokens → only startDocument/endDocument")
  public void test_TC01() throws Exception {
    // spy parser
    CssParser parser = spy(new CssParser(Locale.ENGLISH));
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);
    // stub scan to return emptyIterator
    stubScan(parser, emptyIterator());
    // run
    parser.parse(new StringReader(""), "sys", err, doc);
    // verify only startDocument() and endDocument()
    InOrder inOrder = inOrder(doc);
    inOrder.verify(doc, times(1)).startDocument();
    inOrder.verify(doc, times(1)).endDocument();
    verify(parser, never()).getClass()
      .getDeclaredMethod("handleAtRule", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class)
      .invoke(any(), any(), any(), any());
    verify(parser, never()).getClass()
      .getDeclaredMethod("handleRuleSet", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class)
      .invoke(any(), any(), any(), any());
  }

  /**
   * TC02: one ATKEYWORD → branch-true.
   * stub scan → iterator([ATKEYWORD]); stub handleAtRule to do nothing.
   * Expect handleAtRule called once; start/end Document invoked.
   */
  @Test
  @DisplayName("TC02: single ATKEYWORD token → handleAtRule once")
  public void test_TC02() throws Exception {
    CssParser parser = spy(new CssParser(Locale.ENGLISH));
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);
    // drive one ATKEYWORD token
    CssTokenIterator iter = singleTokenIterator(CssToken.Type.ATKEYWORD);
    stubScan(parser, iter);
    // stub private handleAtRule to record invocation
    Method hAt = CssParser.class.getDeclaredMethod("handleAtRule", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class);
    hAt.setAccessible(true);
    doNothing().when(parser, hAt).invoke(any(), any(), any(), any(), any());
    // run
    parser.parse(new StringReader(""), "sys", err, doc);
    // verify
    verify(parser, times(1)).getClass()
      .getDeclaredMethod("handleAtRule", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class)
      .invoke(any(), any(), any(), any(), any());
    verify(doc).startDocument();
    verify(doc).endDocument();
  }

  /**
   * TC03: one IDENT → branch-false.
   * stub scan → iterator([IDENT]); stub private handleRuleSet.
   * Expect handleRuleSet called once; doc start/end.
   */
  @Test
  @DisplayName("TC03: single non-ATKEYWORD token → handleRuleSet once")
  public void test_TC03() throws Exception {
    CssParser parser = spy(new CssParser(Locale.ENGLISH));
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);
    CssTokenIterator iter = singleTokenIterator(CssToken.Type.IDENT);
    stubScan(parser, iter);
    Method hRs = CssParser.class.getDeclaredMethod("handleRuleSet", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class);
    hRs.setAccessible(true);
    doNothing().when(parser, hRs).invoke(any(), any(), any(), any(), any());
    parser.parse(new StringReader(""), "sys", err, doc);
    verify(parser, times(1)).getClass()
      .getDeclaredMethod("handleRuleSet", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class)
      .invoke(any(), any(), any(), any(), any());
    verify(doc).startDocument();
    verify(doc).endDocument();
  }

  /**
   * TC04: two tokens [ATKEYWORD, IDENT] → mixed loop.
   * Expect one handleAtRule and one handleRuleSet.
   */
  @Test
  @DisplayName("TC04: mixed ATKEYWORD then IDENT → both handlers")
  public void test_TC04() throws Exception {
    CssParser parser = spy(new CssParser(Locale.ENGLISH));
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);
    // build iterator that yields ATKEYWORD then IDENT
    CssToken t1 = new CssToken(CssToken.Type.ATKEYWORD, "@x", CssLocation.create(1, 1));
    CssToken t2 = new CssToken(CssToken.Type.IDENT, "y", CssLocation.create(1, 2));
    CssTokenIterator iter = new CssTokenIterator(java.util.Arrays.asList(t1, t2), CssToken.FILTER_S_CMNT_CDO_CDC) {
      private int idx = 0;
      public boolean hasNext() { return idx < 2; }
      public boolean hasNext(java.util.function.Predicate<CssToken> p) { return hasNext() && p.test(list.get(idx)); }
      public CssToken next() { return list.get(idx++); }
      public CssToken next(java.util.function.Predicate<CssToken> p) { return p.test(list.get(idx)) ? list.get(idx++) : next(); }
    };
    stubScan(parser, iter);
    Method hAt = CssParser.class.getDeclaredMethod("handleAtRule", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class);
    hAt.setAccessible(true);
    doNothing().when(parser, hAt).invoke(any(), any(), any(), any(), any());
    Method hRs = CssParser.class.getDeclaredMethod("handleRuleSet", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class);
    hRs.setAccessible(true);
    doNothing().when(parser, hRs).invoke(any(), any(), any(), any());
    parser.parse(new StringReader(""), "sys", err, doc);
    // verify one each
    verify(parser, times(1)).getClass()
      .getDeclaredMethod("handleAtRule", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class)
      .invoke(any(), any(), any(), any(), any());
    verify(parser, times(1)).getClass()
      .getDeclaredMethod("handleRuleSet", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class)
      .invoke(any(), any(), any(), any());
  }

  /**
   * TC05: scan throws IOException → propagate
   */
  @Test
  @DisplayName("TC05: scan throws IOException")
  public void test_TC05() throws Exception {
    CssParser parser = spy(new CssParser(Locale.ENGLISH));
    doThrow(new IOException("fail-scan"))
      .when(parser).getClass()
        .getDeclaredMethod("scan", Reader.class, String.class, CssErrorHandler.class)
        .invoke(any(), any(), any(), any());
    assertThrows(IOException.class, () -> {
      parser.parse(new StringReader(""), "sys", mock(CssErrorHandler.class), mock(CssContentHandler.class));
    });
  }

  /**
   * TC06: scan throws CssException → propagate
   */
  @Test
  @DisplayName("TC06: scan throws CssException")
  public void test_TC06() throws Exception {
    CssParser parser = spy(new CssParser(Locale.ENGLISH));
    doThrow(new CssGrammarException(CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN, 0, Locale.ENGLISH, "fail-css"))
      .when(parser).getClass()
        .getDeclaredMethod("scan", Reader.class, String.class, CssErrorHandler.class)
        .invoke(any(), any(), any(), any());
    assertThrows(CssGrammarException.class, () -> {
      parser.parse(new StringReader(""), "sys", mock(CssErrorHandler.class), mock(CssContentHandler.class));
    });
  }

  /**
   * TC07: handleAtRule throws PrematureEOFException → loop break, endDocument still called.
   */
  @Test
  @DisplayName("TC07: PrematureEOFException in handleAtRule → suppressed, endDocument")
  public void test_TC07() throws Exception {
    CssParser parser = spy(new CssParser(Locale.ENGLISH));
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);
    CssTokenIterator iter = singleTokenIterator(CssToken.Type.ATKEYWORD);
    stubScan(parser, iter);
    Method hAt = CssParser.class.getDeclaredMethod("handleAtRule", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class);
    hAt.setAccessible(true);
    doThrow(new PrematureEOFException())
      .when(parser, hAt).invoke(any(), any(), any(), any(), any());
    parser.parse(new StringReader(""), "sys", err, doc);
    verify(doc).startDocument();
    verify(doc).endDocument();
  }

  /**
   * TC08: handleRuleSet throws CssGrammarException → propagated.
   */
  @Test
  @DisplayName("TC08: CssGrammarException in handleRuleSet → propagate")
  public void test_TC08() throws Exception {
    CssParser parser = spy(new CssParser(Locale.ENGLISH));
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);
    CssTokenIterator iter = singleTokenIterator(CssToken.Type.IDENT);
    stubScan(parser, iter);
    Method hRs = CssParser.class.getDeclaredMethod("handleRuleSet", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class);
    hRs.setAccessible(true);
    CssGrammarException ex = new CssGrammarException(CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN, 0, Locale.ENGLISH, "tok");
    doThrow(ex)
      .when(parser, hRs).invoke(any(), any(), any(), any(), any());
    CssGrammarException thrown = assertThrows(CssGrammarException.class, () -> {
      parser.parse(new StringReader(""), "sys", err, doc);
    });
    assertEquals(CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN, thrown.getCode());
  }
}