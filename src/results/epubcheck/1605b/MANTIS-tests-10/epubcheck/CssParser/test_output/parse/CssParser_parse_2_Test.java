package org.idpf.epubcheck.util.css;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssGrammarException;
import org.idpf.epubcheck.util.css.CssErrorCode; // Importing CssErrorCode
import org.idpf.epubcheck.util.css.CssDeclaration; // Importing CssDeclaration
import org.idpf.epubcheck.util.css.CssErrorHandler; // Importing CssErrorHandler
import org.idpf.epubcheck.util.css.CssContentHandler; // Importing CssContentHandler
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for CssParser.parse(Reader, String, CssErrorHandler, CssContentHandler).
 */
public class CssParser_parse_2_Test {

  @Test
  @DisplayName("TC14: ATKEYWORD with invalid parameter triggers error path in handleAtRuleParam and skips at-rule without startAtRule")
  public void test_TC14() throws IOException {
    // GIVEN an at-rule with invalid parameter, so handleAtRuleParam returns null
    Reader reader = new StringReader("@foo %;"); // '%' is not a valid at-rule param token
    CssParser parser = new CssParser(Locale.ROOT);
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    // WHEN parsing
    try {
      parser.parse(reader, "id", err, doc);
    } catch (CssException e) {
      // ignore exceptions for this negative path
    }

    // THEN error() is called with unexpected token grammar error
    verify(err).error(argThat(ex -> ex instanceof CssGrammarException
        && ((CssGrammarException) ex).getCode() == CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN));
    // And no at-rule is started or ended for 'foo'
    verify(doc, never()).startAtRule(any());
    verify(doc, never()).endAtRule(any());
    // But document end is still invoked
    verify(doc).endDocument();
  }

  @Test
  @DisplayName("TC15: ATKEYWORD with block containing declarations exercises hasBlock=true and handleDeclarationBlock path")
  public void test_TC15() throws IOException, CssException {
    // GIVEN an at-rule with a block and valid declaration inside
    Reader reader = new StringReader("@foo { color: blue; }");
    CssParser parser = new CssParser(Locale.ROOT);
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    // WHEN parsing
    parser.parse(reader, "id", err, doc);

    // THEN startAtRule and endAtRule are called with name 'foo'
    verify(doc).startAtRule(argThat(name -> "foo".equals(name)));
    // And a declaration event is emitted for the property inside the block
    verify(doc).declaration(any(CssDeclaration.class));
    verify(doc).endAtRule(argThat(name -> "foo".equals(name)));
    // No errors should have been reported
    verify(err, never()).error(any());
    // And document end is invoked
    verify(doc).endDocument();
  }
}