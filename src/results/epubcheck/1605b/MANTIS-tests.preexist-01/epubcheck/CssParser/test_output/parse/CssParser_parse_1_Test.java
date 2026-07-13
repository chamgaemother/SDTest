package org.idpf.epubcheck.util.css;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.PrematureEOFException;
import org.idpf.epubcheck.util.css.CssGrammar.CssAtRule;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelector; // Importing the missing CssSelector class
import org.idpf.epubcheck.util.css.CssGrammar.CssGrammarException; // Importing the missing CssGrammarException class
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for CssParser.parse(Reader, String, CssErrorHandler, CssContentHandler)
 */
public class CssParser_parse_1_Test {

  /**
   * A content handler that records callback names in order.
   */
  private static class RecordingContentHandler implements CssContentHandler {
    List<String> calls = new ArrayList<>();

    @Override
    public void startDocument() {
      calls.add("startDocument");
    }

    @Override
    public void endDocument() {
      calls.add("endDocument");
    }

    @Override
    public void startAtRule(CssAtRule atRule) {
      // record startAtRule with the rule name
      calls.add("startAtRule(" + atRule.getName().get() + ")");
    }

    @Override
    public void endAtRule(String name) {
      calls.add("endAtRule(" + name + ")");
    }

    @Override
    public void selectors(List<CssSelector> selectors) {
      calls.add("selectors");
    }

    @Override
    public void endSelectors(List<CssSelector> selectors) {
      calls.add("endSelectors");
    }

    @Override
    public void declaration(org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration decl) {
      calls.add("declaration");
    }

    // other methods from interface we don't expect in these tests
    @Override public void startAtRule(CssAtRule atRule, boolean isNested) { } // Fixed method signature
    @Override public void endAtRule(CssAtRule atRule) { } // Fixed method signature
    @Override public void selectors(CssSelector selector) { } // Fixed method signature
  }

  /**
   * A simple error handler that only records errors.
   */
  private static class RecordingErrorHandler implements CssErrorHandler {
    List<String> errors = new ArrayList<>();

    @Override
    public void error(CssException e) {
      errors.add(e.getMessage());
    }
  }

  /**
   * An error handler that rethrows any error immediately.
   */
  private static class FailingErrorHandler implements CssErrorHandler {
    @Override
    public void error(CssException e) {
      // rethrow to simulate failure propagation
      throw e;
    }
  }

  @Test
  @DisplayName("TC07: parse(reader with ATKEYWORD that opens and closes a block → enter hasBlock=true, then handleDeclarationBlock empty)")
  public void test_TC07() throws IOException, CssException {
    // GIVEN a reader with an at-rule having an empty block
    Reader reader = new StringReader("@foo{}");
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();
    // WHEN parsing the input
    new CssParser(Locale.ENGLISH).parse(reader, "id", err, doc);
    // THEN the content handler should record exactly the document and at-rule callbacks, no declarations/selectors
    List<String> expected = List.of(
        "startDocument",
        "startAtRule(foo)",
        "endAtRule(foo)",
        "endDocument"
    );
    assertEquals(expected, doc.calls, "Expected only startDocument, startAtRule(foo), endAtRule(foo), endDocument");
  }

  @Test
  @DisplayName("TC08: parse(reader with nested rulesets in an at-rule block → hasRuleSet true path and recursive handleRuleSet)")
  public void test_TC08() throws IOException, CssException {
    // GIVEN a reader with an at-rule containing a nested ruleset "a{}"
    Reader reader = new StringReader("@bar{a{}};");
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();
    // WHEN parsing the input
    new CssParser(Locale.ENGLISH).parse(reader, "id", err, doc);
    // THEN the content handler should record startDocument, startAtRule, selectors/endSelectors for nested rule, endAtRule, endDocument
    List<String> expected = List.of(
        "startDocument",
        "startAtRule(bar)",
        "selectors",
        "endSelectors",
        "endAtRule(bar)",
        "endDocument"
    );
    assertEquals(expected, doc.calls, "Expected nested rule-set callbacks within at-rule bar");
  }

  @Test
  @DisplayName("TC09: parse(reader with rule-set missing closing brace → trigger PrematureEOFException in handleRuleSet catch")
  public void test_TC09() {
    // GIVEN a reader with an unterminated rule-set "a{"
    Reader reader = new StringReader("a{");
    FailingErrorHandler err = new FailingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();
    // WHEN/THEN parsing should throw PrematureEOFException due to missing closing brace
    assertThrows(PrematureEOFException.class, () -> {
      new CssParser(Locale.ENGLISH).parse(reader, "id", err, doc);
    });
  }

}