package org.idpf.epubcheck.util.css;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssGrammarException;
import org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssContentHandler;
public class CssParser_parse_1_Test {

  @Test
  @DisplayName("TC11: ATKEYWORD param error path when handleAtRuleParam returns null triggers error branch and skips to semicolon")
  public void test_TC11() throws IOException, CssException {
    // GIVEN a reader starting with an at-rule whose parameter is invalid ('%') causing handleAtRuleParam to return null
    Reader reader = new StringReader("@foo %;");
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    CssParser parser = new CssParser(Locale.ROOT);

    // WHEN parsing
    parser.parse(reader, "id", err, doc);

    // THEN error() is called with GRAMMAR_UNEXPECTED_TOKEN and document still ends
    verify(err).error(argThat(ex -> ex instanceof CssGrammarException
        && ((CssGrammarException) ex).getCode() == CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN));
    // startAtRule should be called before handling param
    verify(doc).startAtRule(argThat(name -> "foo".equals(name)));
    // endAtRule should be called upon completing the at-rule despite the error
    verify(doc).endAtRule(argThat(name -> "foo".equals(name)));
    // endDocument should be invoked once at the end
    verify(doc).endDocument();
  }

  @Test
  @DisplayName("TC12: ATKEYWORD with empty block exercises hasBlock=true and hasRuleSet=false branch, invoking handleDeclarationBlock empty")
  public void test_TC12() throws IOException, CssException {
    // GIVEN a reader with an at-rule that has an empty block '{}', so hasBlock=true and no inner ruleset
    Reader reader = new StringReader("@foo{}");
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    CssParser parser = new CssParser(Locale.ROOT);

    // WHEN parsing
    parser.parse(reader, "id", err, doc);

    // THEN startAtRule and endAtRule for 'foo' are invoked, no errors
    verify(doc).startAtRule(argThat(name -> "foo".equals(name)));
    verify(doc).endAtRule(argThat(name -> "foo".equals(name)));
    verify(err, never()).error(any());
    verify(doc).endDocument();
  }

  @Test
  @DisplayName("TC13: ATKEYWORD with block containing nested ruleset exercises hasRuleSet=true branch and nested handleRuleSet")
  public void test_TC13() throws IOException, CssException {
    // GIVEN a reader with '@media screen { h1{color:red;} }' invoking nested ruleset branch
    Reader reader = new StringReader("@media screen { h1{color:red;} }");
    CssErrorHandler err = mock(CssErrorHandler.class);
    CssContentHandler doc = mock(CssContentHandler.class);

    CssParser parser = new CssParser(Locale.ROOT);

    // WHEN parsing
    parser.parse(reader, "id", err, doc);

    // THEN startAtRule for 'media', selectors and endSelectors for inner ruleset, and endAtRule
    verify(doc).startAtRule(argThat(name -> "media".equals(name)));
    // selectors() and endSelectors() should be invoked for the nested ruleset
    verify(doc).selectors(any(List.class));
    verify(doc).endSelectors(any(List.class));
    verify(doc).endAtRule(argThat(name -> "media".equals(name)));
    verify(err, never()).error(any());
    verify(doc).endDocument();
  }
}