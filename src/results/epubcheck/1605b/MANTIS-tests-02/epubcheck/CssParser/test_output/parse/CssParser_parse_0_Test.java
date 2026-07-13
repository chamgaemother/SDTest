package org.idpf.epubcheck.util.css;

import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssException;
import org.idpf.epubcheck.util.css.CssGrammarException;
import org.idpf.epubcheck.util.css.CssSource;
import org.idpf.epubcheck.util.css.CssContentHandler;
import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssGrammar.CssAtRule;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelector;
import org.idpf.epubcheck.util.css.CssDeclaration;
import org.idpf.epubcheck.util.css.CssToken;
import org.idpf.epubcheck.util.css.CssTokenList;
import org.idpf.epubcheck.util.css.CssScanner;
import org.idpf.epubcheck.util.css.CssTokenConsumer;
import org.idpf.epubcheck.util.css.PrematureEOFException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CssParser.parse(Reader, String, CssErrorHandler, CssContentHandler).
 */
public class CssParser_parse_0_Test {

  /**
   * A simple error handler that records errors passed to it.
   */
  private static class TestErrorHandler implements CssErrorHandler {
    final List<CssException> errors = new ArrayList<>();
    @Override
    public void error(CssException e) {
      errors.add(e);
    }
  }

  /**
   * A simple content handler that records callbacks.
   */
  private static class TestContentHandler implements CssContentHandler {
    int startDocCount = 0;
    int endDocCount = 0;
    final List<List<String>> selectorsCalled = new ArrayList<>();
    final List<String> endSelectorsCalled = new ArrayList<>();
    final List<String> startAtRules = new ArrayList<>();
    final List<String> endAtRules = new ArrayList<>();

    @Override
    public void startDocument() {
      startDocCount++;
    }

    @Override
    public void endDocument() {
      endDocCount++;
    }

    @Override
    public void selectors(List<CssSelector> selectors) {
      List<String> names = new ArrayList<>();
      for (CssSelector sel : selectors) {
        names.add(sel.toCssString());
      }
      selectorsCalled.add(names);
    }

    @Override
    public void endSelectors(List<CssSelector> selectors) {
      // record endSelectors by concatenating names
      StringBuilder sb = new StringBuilder();
      for (CssSelector sel : selectors) {
        sb.append(sel.toCssString()).append(",");
      }
      endSelectorsCalled.add(sb.toString());
    }

    @Override
    public void startAtRule(CssAtRule atRule) {
      startAtRules.add(atRule.getName().or("")); 
    }

    @Override
    public void endAtRule(String name) {
      endAtRules.add(name);
    }

    // Unused in these tests:
    @Override public void declaration(CssDeclaration decl) {}
  }

  @Test
  @DisplayName("TC01: Empty stream results in no rules processed and immediate endDocument (loop-0)")
  public void test_TC01() throws IOException, CssException {
    // GIVEN an empty CSS input reader
    CssParser parser = new CssParser();
    Reader reader = new StringReader("");
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();
    // WHEN parsing
    parser.parse(reader, "id", err, doc);
    // THEN startDocument and endDocument each called exactly once, no selectors or at-rules or errors
    assertEquals(1, doc.startDocCount, "startDocument() should be called once");
    assertEquals(1, doc.endDocCount, "endDocument() should be called once");
    assertTrue(doc.selectorsCalled.isEmpty(), "No selectors should be recorded");
    assertTrue(doc.startAtRules.isEmpty(), "No at-rules should be started");
    assertTrue(err.errors.isEmpty(), "No errors should be recorded");
  }

  @Test
  @DisplayName("TC02: Single at-rule without block parsed (one iteration, branch-true ATKEYWORD)")
  public void test_TC02() throws IOException, CssException {
    // GIVEN an @charset rule; MATCH_SEMI_OPENBRACE true on ';'
    CssParser parser = new CssParser();
    Reader reader = new StringReader("@charset \"UTF-8\";");
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();
    // WHEN parsing
    parser.parse(reader, "sys", err, doc);
    // THEN no errors; startAtRule and endAtRule called for 'charset'; no selectors
    assertTrue(err.errors.isEmpty(), "No errors for valid at-rule");
    assertEquals(1, doc.startDocCount);
    assertEquals(1, doc.endDocCount);
    assertEquals(1, doc.startAtRules.size(), "One at-rule should start");
    assertEquals("charset", doc.startAtRules.get(0), "At-rule name should be 'charset'");
    assertEquals(1, doc.endAtRules.size(), "One at-rule should end");
    assertEquals("charset", doc.endAtRules.get(0), "endAtRule name should be 'charset'");
    assertTrue(doc.selectorsCalled.isEmpty(), "No selectors for at-rule");
  }

  @Test
  @DisplayName("TC03: Single rule-set without declarations parsed (one iteration, branch-false ATKEYWORD)")
  public void test_TC03() throws IOException, CssException {
    // GIVEN a simple ruleset 'body{}'; branch on ATKEYWORD false
    CssParser parser = new CssParser();
    Reader reader = new StringReader("body{}");
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();
    // WHEN parsing
    parser.parse(reader, "url", err, doc);
    // THEN selectors called once with "body"; endSelectors too; no errors
    assertTrue(err.errors.isEmpty(), "No errors for valid ruleset");
    assertEquals(1, doc.startDocCount);
    assertEquals(1, doc.endDocCount);
    assertEquals(1, doc.selectorsCalled.size(), "One selectors() call expected");
    assertEquals("body", doc.selectorsCalled.get(0).get(0), "Selector should be 'body'");
    assertEquals(1, doc.endSelectorsCalled.size(), "One endSelectors() call expected");
    assertTrue(doc.startAtRules.isEmpty(), "No at-rules for ruleset");
  }

  @Test
  @DisplayName("TC04: Multiple rule-sets parsed sequentially (loop-N for N=2)")
  public void test_TC04() throws IOException, CssException {
    // GIVEN two rulesets 'h1{}h2{}'; loop runs twice
    CssParser parser = new CssParser();
    Reader reader = new StringReader("h1{}h2{}");
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();
    // WHEN parsing
    parser.parse(reader, "id", err, doc);
    // THEN selectors called twice: "h1" then "h2"
    assertTrue(err.errors.isEmpty(), "No errors for two valid rulesets");
    assertEquals(1, doc.startDocCount);
    assertEquals(1, doc.endDocCount);
    assertEquals(2, doc.selectorsCalled.size(), "Two selectors() calls expected");
    assertEquals("h1", doc.selectorsCalled.get(0).get(0), "First selector 'h1'");
    assertEquals("h2", doc.selectorsCalled.get(1).get(0), "Second selector 'h2'");
  }

  @Test
  @DisplayName("TC05: Premature EOF inside a rule-set triggers error and breaks loop (exception path)")
  public void test_TC05() throws IOException, CssException {
    // GIVEN incomplete ruleset 'p{' leads to NoSuchElementException in handleRuleSet
    CssParser parser = new CssParser();
    Reader reader = new StringReader("p{");
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();
    // WHEN parsing
    parser.parse(reader, "sys", err, doc);
    // THEN errorHandler.error called once with GRAMMAR_PREMATURE_EOF; loop breaks and endDocument still called
    assertEquals(1, err.errors.size(), "One premature-EOF error expected");
    assertTrue(err.errors.get(0) instanceof CssGrammarException, "Error should be CssGrammarException");
    String msg = err.errors.get(0).getMessage();
    assertTrue(msg.contains("PREMATURE_EOF"), "Error code PREMATURE_EOF expected in message");
    assertEquals(1, doc.startDocCount, "startDocument still called");
    assertEquals(1, doc.endDocCount, "endDocument still called despite error");
  }
}