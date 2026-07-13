package org.idpf.epubcheck.util.css;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelector;
import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssContentHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CssParser_parse_2_Test {

  /**
   * A simple error handler that collects errors.
   */
  static class TestErrorHandler implements CssErrorHandler {
    final List<String> errors = new ArrayList<>();

    @Override
    public void error(CssException e) {
      errors.add(e.getMessage());
    }
  }

  /**
   * A simple content handler that records events.
   */
  static class TestContentHandler implements CssContentHandler {
    final List<String> startedAtRuleNames = new ArrayList<>();
    final List<String> endedAtRuleNames = new ArrayList<>();
    final List<List<CssSelector>> selectorsList = new ArrayList<>();

    @Override
    public void startDocument() {
      // no-op
    }

    @Override
    public void endDocument() {
      // no-op
    }

    @Override
    public void startAtRule(org.idpf.epubcheck.util.css.CssGrammar.CssAtRule atRule) {
      startedAtRuleNames.add(atRule.getName().get());
    }

    @Override
    public void endAtRule(String name) {
      endedAtRuleNames.add(name);
    }

    @Override
    public void selectors(List<CssSelector> selectors) {
      selectorsList.add(selectors);
    }

    @Override
    public void endSelectors(List<CssSelector> selectors) {
      // no-op
    }

    @Override
    public void declaration(org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration decl) {
      // no-op for these tests
    }
  }

  @Test
  @DisplayName("parse handles an at-rule with block containing a nested ruleset (hasRuleSet=true)")
  public void test_TC11() throws IOException, CssException {
    // Input contains @media block with nested h1 ruleset -> triggers hasRuleSet=true branch
    Reader reader = new StringReader("@media screen { h1 {} }");
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();

    new CssParser(Locale.ENGLISH).parse(reader, "id", err, doc);

    // The media at-rule should have been started and ended
    assertTrue(doc.startedAtRuleNames.contains("media"),
        "Expected startAtRule for 'media'");
    // The nested ruleset should produce a selectors event for h1
    assertFalse(doc.selectorsList.isEmpty(),
        "Expected selectors for nested ruleset inside @media");
    boolean foundH1 = doc.selectorsList.stream()
        .flatMap(List::stream)
        .anyMatch(sel -> sel.toString().contains("h1"));
    assertTrue(foundH1, "Expected a selector 'h1' in the nested ruleset");
    assertTrue(doc.endedAtRuleNames.contains("media"),
        "Expected endAtRule for 'media'");
  }

  @Test
  @DisplayName("parse handles an at-rule with block containing declarations only (hasRuleSet=false)")
  public void test_TC12() throws IOException, CssException {
    // Input @font-face with only declarations -> triggers hasRuleSet=false branch
    String css = "@font-face { font-family: 'A'; }";
    Reader reader = new StringReader(css);
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();

    new CssParser(Locale.ENGLISH).parse(reader, "id", err, doc);

    // font-face at-rule events
    assertTrue(doc.startedAtRuleNames.contains("font-face"),
        "Expected startAtRule for 'font-face'");
    // No parse errors should have been recorded
    assertTrue(err.errors.isEmpty(), "Expected no errors");
    assertTrue(doc.endedAtRuleNames.contains("font-face"),
        "Expected endAtRule for 'font-face'");
  }

  @Test
  @DisplayName("parse handles nested at-rule inside an at-rule block (inner ATKEYWORD branch)")
  public void test_TC13() throws IOException, CssException {
    // Input contains outer at-rule with an inner at-rule -> triggers nested ATKEYWORD branch
    Reader reader = new StringReader("@outer { @inner {} }");
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();

    new CssParser(Locale.ENGLISH).parse(reader, "id", err, doc);

    // Both outer and inner should have start and end events in correct order
    assertTrue(doc.startedAtRuleNames.contains("outer"),
        "Expected startAtRule for 'outer'");
    assertTrue(doc.startedAtRuleNames.contains("inner"),
        "Expected nested startAtRule for 'inner'");
    // endAtRule for inner should occur before outer
    int idxOuterEnd = doc.endedAtRuleNames.indexOf("outer");
    int idxInnerEnd = doc.endedAtRuleNames.indexOf("inner");
    assertTrue(idxInnerEnd >= 0 && idxOuterEnd > idxInnerEnd,
        "Expected endAtRule 'inner' before 'outer'");
  }
}