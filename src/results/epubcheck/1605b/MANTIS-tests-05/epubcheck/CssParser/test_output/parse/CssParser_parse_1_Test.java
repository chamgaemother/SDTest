package org.idpf.epubcheck.util.css;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssGrammarException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JUnit5 tests for CssParser.parse(Reader,String,CssErrorHandler,CssContentHandler)
 */
public class CssParser_parse_1_Test {

  /**
   * A simple handler that records error events.
   */
  static class RecordingErrorHandler implements CssErrorHandler {
    int errorCount = 0;
    List<CssException> errors = new ArrayList<>();

    @Override
    public void error(CssException exception) {
      errorCount++;
      errors.add(exception);
    }
  }

  /**
   * A simple content handler that records parse events.
   */
  static class RecordingContentHandler implements CssContentHandler {
    int startDocumentCount = 0;
    int endDocumentCount = 0;
    int startAtRuleCount = 0;
    int endAtRuleCount = 0;
    String lastAtRuleName = null;
    // unused selectors methods
    @Override public void startDocument() { startDocumentCount++; }
    @Override public void endDocument() { endDocumentCount++; }
    @Override public void startAtRule(org.idpf.epubcheck.util.css.CssGrammar.CssAtRule atRule) {
      startAtRuleCount++; lastAtRuleName = atRule.getName().orNull(); }
    @Override public void endAtRule(String atRuleName) { endAtRuleCount++; }
    // stubs for other interface methods
    @Override public void selectors(List<org.idpf.epubcheck.util.css.CssGrammar.CssSelector> selectors) {}
    @Override public void endSelectors(List<org.idpf.epubcheck.util.css.CssGrammar.CssSelector> selectors) {}
    @Override public void declaration(org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration declaration) {}
  }

  @Test
  @DisplayName("parse handles at-rule without block (MATCH_SEMI_OPENBRACE with ';' only)")
  public void test_TC05() throws IOException, CssException {
    // Given a CSS input with an at-rule ending in ';' so hasBlock == false
    String css = "@import url('x.css');";
    Reader reader = new StringReader(css);
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();

    // When parse is invoked
    new CssParser(Locale.ENGLISH).parse(reader, "import.css", err, doc);

    // Then startDocument -> B0, no loop body exception -> B8
    assertEquals(1, doc.startDocumentCount, "startDocument should be called once");
    // sub-path B2->B4 sees ATKEYWORD, handleAtRule branch, MATCH_SEMI_OPENBRACE applies setting hasBlock=false
    assertEquals(1, doc.startAtRuleCount, "startAtRule should be called once for the import rule");
    assertEquals("import", doc.lastAtRuleName, "the at-rule name should be 'import'");
    assertEquals(1, doc.endAtRuleCount, "endAtRule should be called once");
    assertEquals(1, doc.endDocumentCount, "endDocument should be called once");
    assertEquals(0, err.errorCount, "no errors should be reported");
  }

  @Test
  @DisplayName("parse handles invalid selector group yielding selectors==null path in handleRuleSet")
  public void test_TC06() throws IOException, CssException {
    // Given an empty ruleset "{};" triggers cssSelectorFactory.createSelectorList -> null selectors
    String css = "{};";
    Reader reader = new StringReader(css);
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();

    // When parse is invoked
    new CssParser(Locale.ENGLISH).parse(reader, "invalid.css", err, doc);

    // Then parse should report at least one error for invalid selector and finish document
    assertTrue(err.errorCount >= 1, "an error should be reported for missing selector");
    // no selectors should be emitted
    // RecordingContentHandler does not record selectors calls, but it would not call startAtRule nor selectors
    // we at least check no at-rule was started
    assertEquals(0, doc.startAtRuleCount, "no at-rule should be started");
    assertEquals(1, doc.startDocumentCount, "startDocument should be called once even on invalid selector");
    assertEquals(1, doc.endDocumentCount, "endDocument should be called once");
  }

  @Test
  @DisplayName("parse handles unexpected closing parenthesis branch in handleRuleSet (MATCH_CLOSEPAREN applies)")
  public void test_TC07() throws IOException, CssException {
    // Given a selector group containing ")" right after selector start triggers unexpected ')'
    String css = "h1){};";
    Reader reader = new StringReader(css);
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();

    // When parse is invoked
    new CssParser(Locale.ENGLISH).parse(reader, "paren.css", err, doc);

    // Then error handler should record a CssGrammarException for unexpected ')'
    assertTrue(err.errorCount >= 1, "an unexpected token ')' error should be reported");
    boolean foundUnexpected = err.errors.stream()
      .anyMatch(e -> e instanceof CssGrammarException && e.getMessage().contains(")"));
    assertTrue(foundUnexpected, "CssGrammarException for unexpected ')' should be present");
    // no selectors or rules should be completed
    assertEquals(0, doc.startAtRuleCount, "no at-rule should be started on unexpected ')'");
    assertEquals(1, doc.startDocumentCount, "startDocument should be called once");
    assertEquals(1, doc.endDocumentCount, "endDocument should be called once");
  }
}