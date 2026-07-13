package org.idpf.epubcheck.util.css;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Collections;
import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssToken;
import org.idpf.epubcheck.util.css.CssTokenList.CssTokenIterator;
import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssContentHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * JUnit5 tests for CssParser.parse(Reader, String, CssErrorHandler, CssContentHandler)
 * scenarios TC13 & TC14.
 */
@ExtendWith(MockitoExtension.class)
public class CssParser_parse_2_Test {

  /**
   * Scenario TC13: simulate an IOException inside handleAtRule branch.
   * Path B0→B1→B2→B4 ensures the first token is ATKEYWORD,
   * so parse() calls private handleAtRule(...) which we've stubbed to throw.
   * We verify startDocument() is called and IOException propagates,
   * so endDocument() must not be invoked.
   */
  @Test
  @DisplayName("TC13: IOException thrown by handleAtRule propagates and prevents endDocument")
  public void test_TC13() throws Exception {
    // Prepare a dummy ATKEYWORD token
    CssToken atTk = mock(CssToken.class);
    when(atTk.type).thenReturn(CssToken.Type.ATKEYWORD);
    // Stub iterator: hasNext once, next then false
    CssTokenIterator iter = mock(CssTokenIterator.class);
    when(iter.hasNext(any())).thenReturn(true);
    when(iter.next(any())).thenReturn(atTk);
    // Create spy parser to override scan() returning our stub iterator
    CssParser parser = spy(new CssParser());
    doReturn(iter).when(parser).scan(any(Reader.class), anyString(), any(CssErrorHandler.class));
    // Stub the private handleAtRule to throw IOException
    Method mHandleAt = CssParser.class.getDeclaredMethod(
        "handleAtRule", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class);
    mHandleAt.setAccessible(true);
    doThrow(new IOException("fail-handleAtRule"))
      .when(parser).getClass()
      .getDeclaredMethod("handleAtRule", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class)
      .invoke(parser, atTk, iter, null, null);
    // Mocks for handlers
    CssContentHandler doc = mock(CssContentHandler.class);
    CssErrorHandler err = mock(CssErrorHandler.class);
    Reader reader = new StringReader("");
    // Verify exception and invocation behavior
    assertThrows(IOException.class,
        () -> parser.parse(reader, "sys", err, doc),
        "Expected IOException from handleAtRule to propagate");
    verify(doc, times(1)).startDocument();
    verify(doc, never()).endDocument();
  }

  /**
   * Scenario TC14: simulate an IOException inside handleRuleSet branch.
   * Path B0→B1→B2→B3 ensures the first token is non-ATKEYWORD,
   * so parse() calls private handleRuleSet(...) which we've stubbed to throw.
   * We verify startDocument() is called and IOException propagates,
   * so endDocument() must not be invoked.
   */
  @Test
  @DisplayName("TC14: IOException thrown by handleRuleSet propagates and prevents endDocument")
  public void test_TC14() throws Exception {
    // Prepare a dummy non-ATKEYWORD token
    CssToken idTk = mock(CssToken.class);
    when(idTk.type).thenReturn(CssToken.Type.IDENT);
    // Stub iterator: hasNext once, next then false
    CssTokenIterator iter = mock(CssTokenIterator.class);
    when(iter.hasNext(any())).thenReturn(true);
    when(iter.next(any())).thenReturn(idTk);
    // Create spy parser to override scan() returning our stub iterator
    CssParser parser = spy(new CssParser());
    doReturn(iter).when(parser).scan(any(Reader.class), anyString(), any(CssErrorHandler.class));
    // Stub the private handleRuleSet to throw IOException
    Method mHandleRule = CssParser.class.getDeclaredMethod(
        "handleRuleSet", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class);
    mHandleRule.setAccessible(true);
    doThrow(new IOException("fail-handleRuleSet"))
      .when(parser).getClass()
      .getDeclaredMethod("handleRuleSet", CssToken.class, CssTokenIterator.class, CssContentHandler.class, CssErrorHandler.class)
      .invoke(parser, idTk, iter, null, null);
    // Mocks for handlers
    CssContentHandler doc = mock(CssContentHandler.class);
    CssErrorHandler err = mock(CssErrorHandler.class);
    Reader reader = new StringReader("");
    // Verify exception and invocation behavior
    assertThrows(IOException.class,
        () -> parser.parse(reader, "sys", err, doc),
        "Expected IOException from handleRuleSet to propagate");
    verify(doc, times(1)).startDocument();
    verify(doc, never()).endDocument();
  }
}