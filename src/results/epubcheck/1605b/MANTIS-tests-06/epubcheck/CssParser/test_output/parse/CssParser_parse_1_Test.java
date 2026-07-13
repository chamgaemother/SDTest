package org.idpf.epubcheck.util.css;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.idpf.epubcheck.util.css.CssExceptions.CssGrammarException;
import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode;
import org.idpf.epubcheck.util.css.CssExceptions.PrematureEOFException;
import org.idpf.epubcheck.util.css.CssSelector;
import org.idpf.epubcheck.util.css.CssGrammar.CssAtRule;
import org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CssParser_parse_1_Test {

  // A test implementation of CssErrorHandler that collects error codes
  static class TestErrorHandler implements CssErrorHandler {
    final List<CssErrorCode> errors = new ArrayList<>();
    @Override public void error(CssGrammarException e) {
      errors.add(e.getErrorCode());
    }
    @Override public void error(CssException e) { /* handle exception */ }
  }

  // A test implementation of CssContentHandler that tracks document events
  static class TestContentHandler implements CssContentHandler {
    int startDocumentCount = 0;
    int endDocumentCount = 0;
    final List<List<CssSelector>> selectorsList = new ArrayList<>();
    final List<String> startedAtRuleNames = new ArrayList<>();
    final List<String> endedAtRuleNames = new ArrayList<>();

    @Override public void startDocument() { startDocumentCount++; }
    @Override public void endDocument() { endDocumentCount++; }
    @Override public void selectors(List<CssSelector> selectors) { selectorsList.add(selectors); }
    @Override public void endSelectors(List<CssSelector> selectors) { /* no-op */ }
    @Override public void declaration(CssDeclaration decl) { /* no-op */ }
    @Override public void startAtRule(CssAtRule atRule) {
      // Record the at-rule name when starting
      startedAtRuleNames.add(atRule.getName().orElse(null));
    }
    @Override public void endAtRule(String name) {
      endedAtRuleNames.add(name);
    }
  }

  @Test
  @DisplayName("handles selectors==null branch when selector parsing fails")
  public void test_TC08() throws IOException, CssException {
    // Design: ":invalid{}" has an invalid selector, forcing selectors==null in handleRuleSet
    String css = ":invalid{}";
    CssParser parser = new CssParser(Locale.ENGLISH);
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();
    StringReader reader = new StringReader(css);

    parser.parse(reader, "id", err, doc);

    // No selectors should have been emitted because selector parsing failed and selectors==null leads to skip
    assertTrue(doc.selectorsList.isEmpty(), "Expected no selectors emitted when selectors==null");
    // startDocument and endDocument must each be called exactly once
    assertEquals(1, doc.startDocumentCount, "startDocument should be called once");
    assertEquals(1, doc.endDocumentCount, "endDocument should be called once");
  }

  @Test
  @DisplayName("handles unexpected ')' before '{' in rule-set (GRAMMAR_UNEXPECTED_TOKEN)")
  public void test_TC09() throws IOException, CssException {
    // Design: "h1){}" contains a closing parenthesis immediately after selector, triggering GRAMMAR_UNEXPECTED_TOKEN
    String css = "h1){}";
    CssParser parser = new CssParser(Locale.ENGLISH);
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();
    StringReader reader = new StringReader(css);

    parser.parse(reader, "id", err, doc);

    // The error handler should record GRAMMAR_UNEXPECTED_TOKEN
    assertTrue(err.errors.contains(CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN),
        "Expected GRAMMAR_UNEXPECTED_TOKEN when ')' appears before '{'");
    // The document should still start and end normally
    assertEquals(1, doc.startDocumentCount, "startDocument should be called once even on error");
    assertEquals(1, doc.endDocumentCount, "endDocument should be called once even on error");
  }

  @Test
  @DisplayName("premature EOF inside at-rule parameters triggers PrematureEOFException and ends document")
  public void test_TC10() throws IOException, CssException {
    // Design: "@x" is an incomplete at-rule missing ';' or '{', causing EOF inside handleAtRule
    String css = "@x";
    CssParser parser = new CssParser(Locale.ENGLISH);
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();
    StringReader reader = new StringReader(css);

    parser.parse(reader, "id", err, doc);

    // The handler should record a GRAMMAR_PREMATURE_EOF error for missing ';' or '{'
    assertTrue(err.errors.contains(CssErrorCode.GRAMMAR_PREMATURE_EOF),
        "Expected GRAMMAR_PREMATURE_EOF when at-rule parameters end prematurely");
    // The content handler should have startAtRule and endAtRule invoked with name "x"
    assertEquals(List.of("x"), doc.startedAtRuleNames,
        "startAtRule should be called with name 'x'");
    assertEquals(List.of("x"), doc.endedAtRuleNames,
        "endAtRule should be called with name 'x'");
    // endDocument must still be called once
    assertEquals(1, doc.endDocumentCount, "endDocument should be called once after premature EOF");
  }
}