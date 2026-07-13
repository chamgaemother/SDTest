package org.idpf.epubcheck.util.css;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssGrammarException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * JUnit 5 tests for CssParser.parse(...) covering scenarios TC01–TC10.
 */
public class CssParser_parse_0_Test {

  @Test
  @DisplayName("TC01: Empty stylesheet triggers zero-iteration loop (hasNext false)")
  public void test_TC01() throws Exception {
    // GIVEN: empty reader yields no tokens so loop zero times
    Reader reader = new StringReader("");
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    // WHEN
    new CssParser().parse(reader, "id", err, doc);

    // THEN: startDocument then endDocument, no error
    InOrder inOrder = inOrder(doc, err);
    inOrder.verify(doc).startDocument();
    inOrder.verify(doc).endDocument();
    verifyNoInteractions(err);
  }

  @Test
  @DisplayName("TC02: Single AT-rule with no block ends loop after first iteration")
  public void test_TC02() throws Exception {
    // GIVEN: input starts with ATKEYWORD so handleAtRule path; ends with ';'
    Reader reader = new StringReader("@charset \"UTF-8\";");
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    // WHEN
    new CssParser().parse(reader, "id", err, doc);

    // THEN: parser must call startAtRule("charset") before endAtRule("charset")
    InOrder inOrder = inOrder(doc);
    inOrder.verify(doc).startDocument();
    inOrder.verify(doc).startAtRule("charset");
    inOrder.verify(doc).endAtRule("charset");
    inOrder.verify(doc).endDocument();
  }

  @Test
  @DisplayName("TC03: Single ruleset triggers handleRuleSet path when token type != ATKEYWORD")
  public void test_TC03() throws Exception {
    // GIVEN: input "h1 { color:red; }" leads to a RULESET path (type IDENT)
    Reader reader = new StringReader("h1 { color:red; }");
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    // WHEN
    new CssParser().parse(reader, "id", err, doc);

    // THEN: must call selectors, declaration, endSelectors
    verify(doc).startDocument();
    verify(doc).selectors(anyList());
    verify(doc, atLeastOnce()).declaration(any());
    verify(doc).endSelectors(anyList());
    verify(doc).endDocument();
  }

  @Test
  @DisplayName("TC04: Two top-level constructs exercises ATRULE then RULESET")
  public void test_TC04() throws Exception {
    // GIVEN: first token ATKEYWORD, then a ruleset, so two iterations
    Reader reader = new StringReader("@import url(x.css); h2 {margin:0;}");
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    // WHEN
    new CssParser().parse(reader, "id", err, doc);

    // THEN: sequence must be startAtRule->endAtRule->selectors->endSelectors->endDocument
    InOrder inOrder = inOrder(doc);
    inOrder.verify(doc).startDocument();
    inOrder.verify(doc).startAtRule("import");
    inOrder.verify(doc).endAtRule("import");
    inOrder.verify(doc).selectors(anyList());
    inOrder.verify(doc).endSelectors(anyList());
    inOrder.verify(doc).endDocument();
  }

  @Test
  @DisplayName("TC05: PrematureIOException in scan propagates IOException path")
  public void test_TC05() throws Exception {
    // GIVEN: Reader that throws IOException on read()
    Reader reader = new Reader() {
      @Override public int read(char[] cbuf, int off, int len) throws IOException {
        throw new IOException("read failure");
      }
      @Override public void close() throws IOException {}
    };
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    // WHEN & THEN: IOException thrown before startDocument
    assertThrows(IOException.class, () -> new CssParser().parse(reader, "id", err, doc));
    verify(doc, never()).startDocument();
  }

  @Test
  @DisplayName("TC06: CssException in scan propagates CssException path")
  public void test_TC06() throws Exception {
    // GIVEN: subclass CssParser to override scan() and throw CssException
    CssParser parser = new CssParser() {
      @Override
      @SuppressWarnings("unchecked")
      public CssTokenIterator scan(Reader r, String s, CssErrorHandler e) throws IOException, CssException {
        throw new CssException("grammar failure");
      }
    };
    // WHEN & THEN
    Reader reader = new StringReader("bad");
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);
    assertThrows(CssException.class, () -> {
      // invoke public parse which calls private scan
      parser.parse(reader, "id", err, doc);
    });
    verifyNoInteractions(doc);
  }

  @Test
  @DisplayName("TC07: PrematureEOF during handleRuleSet breaks loop and ends document")
  public void test_TC07() throws Exception {
    // GIVEN: incomplete ruleset "h1 { color" triggers NoSuchElementException in handleRuleSet
    Reader reader = new StringReader("h1 { color");
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    // WHEN
    new CssParser().parse(reader, "id", err, doc);

    // THEN: error called once with GRAMMAR_PREMATURE_EOF, endDocument still called
    verify(err).error(any(CssGrammarException.class));
    verify(doc).startDocument();
    verify(doc).endDocument();
  }

  @Test
  @DisplayName("TC08: Handle selector-list error path when selectors==null in handleRuleSet")
  public void test_TC08() throws Exception {
    // GIVEN: stub factory to return null for selectors, input has a ruleset
    CssParser parser = spy(new CssParser());
    // stub createSelectorList to return null
    doReturn(null).when(parser).cssSelectorFactory.createSelectorList(any(), any(), any());
    Reader reader = new StringReader("x { }");
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    // WHEN
    parser.parse(reader, "id", err, doc);

    // THEN: no selectors or declarations, only start/end document
    verify(doc).startDocument();
    verify(doc).endDocument();
    verify(doc, never()).selectors(anyList());
    verify(doc, never()).declaration(any());
  }

  @Test
  @DisplayName("TC09: Use CssSource overload delegates to Reader overload")
  public void test_TC09() throws Exception {
    // GIVEN: stub CssSource to supply empty Reader and systemID
    CssSource src = new CssSource() {
      @Override public Reader newReader() { return new StringReader(""); }
      @Override public String getSystemID() { return "sys"; }
    };
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    // WHEN
    new CssParser().parse(src, err, doc);

    // THEN: behaves like empty reader scenario
    InOrder inOrder = inOrder(doc);
    inOrder.verify(doc).startDocument();
    inOrder.verify(doc).endDocument();
    verifyNoInteractions(err);
  }

  @Test
  @DisplayName("TC10: Mixed two rulesets exercises multiple branch-false iterations")
  public void test_TC10() throws Exception {
    // GIVEN: two sequential rulesets "p{a:1;} div{b:2;}"
    Reader reader = new StringReader("p{a:1;} div{b:2;}");
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    // WHEN
    new CssParser().parse(reader, "id", err, doc);

    // THEN: selectors and endSelectors each called twice
    verify(doc, times(2)).selectors(anyList());
    verify(doc, times(2)).endSelectors(anyList());
    verify(doc).endDocument();
  }

}