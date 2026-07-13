package org.idpf.epubcheck.util.css;

import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssContentHandler;
import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssGrammarException;
import org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode;
import org.idpf.epubcheck.util.css.CssGrammar.CssAtRule;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelector;
import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssToken;
import org.idpf.epubcheck.util.css.CssTokenList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CssParser_parse_0_Test {

  // A simple error handler that records received error codes
  static class TestErrorHandler implements CssErrorHandler {
    final List<CssErrorCode> errors = new ArrayList<>();
    @Override
    public void error(CssException e) {
      if (e instanceof CssGrammarException) {
        errors.add(((CssGrammarException) e).getErrorCode());
      } else {
        errors.add(null);
      }
    }
  }

  // A simple content handler that records document events
  static class TestContentHandler implements CssContentHandler {
    int startDocumentCount = 0;
    int endDocumentCount = 0;
    final List<String> startedAtRuleNames = new ArrayList<>();
    final List<String> endedAtRuleNames = new ArrayList<>();
    final List<List<CssSelector>> selectorsList = new ArrayList<>();
    int endSelectorsCount = 0;

    @Override
    public void startDocument() {
      startDocumentCount++;
    }

    @Override
    public void endDocument() {
      endDocumentCount++;
    }

    @Override
    public void startAtRule(CssAtRule atRule) {
      // record the at-rule name for verification
      startedAtRuleNames.add(atRule.getName().orElse(null));
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
      endSelectorsCount++;
    }

    @Override
    public void declaration(org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration declaration) {
      // not used in these tests
    }
  }

  // A Reader that throws IOException on any read
  static class IoErrorReader extends Reader {
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      throw new IOException("read error");
    }
    @Override
    public void close() throws IOException {
      throw new IOException("close error");
    }
  }

  @Test
  @DisplayName("TC01: Empty CSS input results in no iterations")
  public void test_TC01() throws Exception {
    // INPUT: empty string -> iter.hasNext() false immediately (B1 false -> B8)
    CssParser parser = new CssParser();
    StringReader reader = new StringReader("");
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();

    parser.parse(reader, "id", err, doc);

    // VERIFY: startDocument and endDocument each called exactly once
    assertEquals(1, doc.startDocumentCount, "startDocument should be called once");
    assertEquals(1, doc.endDocumentCount, "endDocument should be called once");
    // no errors or other events
    assertTrue(err.errors.isEmpty(), "No errors should be reported");
  }

  @Test
  @DisplayName("TC02: Single at-rule token triggers handleAtRule branch")
  public void test_TC02() throws Exception {
    // INPUT: "@import 'a.css';" triggers one ATKEYWORD -> handleAtRule then exit loop
    CssParser parser = new CssParser();
    StringReader reader = new StringReader("@import 'a.css';");
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();

    parser.parse(reader, "id", err, doc);

    // VERIFY: at-rule start and end captured
    assertEquals(1, doc.startDocumentCount, "Document start once");
    assertEquals(1, doc.endDocumentCount, "Document end once");
    assertEquals(List.of("import"), doc.startedAtRuleNames, "Should start 'import' rule");
    assertEquals(List.of("import"), doc.endedAtRuleNames, "Should end 'import' rule");
  }

  @Test
  @DisplayName("TC03: Single rule-set token triggers handleRuleSet branch")
  public void test_TC03() throws Exception {
    // INPUT: "h1{}" triggers non-ATKEYWORD -> handleRuleSet once then exit
    CssParser parser = new CssParser();
    StringReader reader = new StringReader("h1{}");
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();

    parser.parse(reader, "id", err, doc);

    // VERIFY: selectorsList should have one non-empty selector list
    assertEquals(1, doc.startDocumentCount);
    assertEquals(1, doc.endDocumentCount);
    assertEquals(1, doc.selectorsList.size(), "One selector group should be parsed");
    assertFalse(doc.selectorsList.get(0).isEmpty(), "Selector list should not be empty");
    assertEquals(1, doc.endSelectorsCount, "endSelectors should be called once");
  }

  @Test
  @DisplayName("TC04: Multiple mixed tokens iterate twice: at-rule then rule-set")
  public void test_TC04() throws Exception {
    // INPUT: "@x{}h2{}" will first ATKEYWORD then IDENT path -> two iterations
    CssParser parser = new CssParser();
    StringReader reader = new StringReader("@x{}h2{}");
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();

    parser.parse(reader, "id", err, doc);

    // VERIFY: first at-rule "x", then selectors for "h2"
    assertTrue(doc.startedAtRuleNames.contains("x"), "Should start 'x' at-rule");
    assertTrue(doc.endedAtRuleNames.contains("x"), "Should end 'x' at-rule");
    // selectors for h2 present
    assertEquals(1, doc.selectorsList.size(), "One rule-set selectors list expected");
    String firstSel = doc.selectorsList.get(0).get(0).toString();
    assertTrue(firstSel.contains("h2"), "Selector should contain 'h2'");
  }

  @Test
  @DisplayName("TC05: IO error thrown by scan(reader) propagates IOException")
  public void test_TC05() {
    // INPUT: Reader that throws IOException on read -> scan throws immediately at B0
    CssParser parser = new CssParser();
    IoErrorReader reader = new IoErrorReader();
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();

    assertThrows(IOException.class, () -> parser.parse(reader, "id", err, doc),
        "IOException should propagate");
    // VERIFY: no document calls
    assertEquals(0, doc.startDocumentCount, "startDocument must not be called");
    assertEquals(0, doc.endDocumentCount, "endDocument must not be called");
  }

  @Test
  @DisplayName("TC06: Premature EOF in handleRuleSet breaks loop and ends document")
  public void test_TC06() {
    // INPUT: "p{color:" missing closing -> handleRuleSet catches PrematureEOF (B3->B6->B8)
    CssParser parser = new CssParser();
    StringReader reader = new StringReader("p{color:");
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();

    // parse should not throw, but report grammar premature EOF and endDocument
    assertDoesNotThrow(() -> parser.parse(reader, "id", err, doc), "Should handle premature EOF");
    assertEquals(1, doc.startDocumentCount);
    assertEquals(1, doc.endDocumentCount, "Document should be ended even on premature EOF");
    assertTrue(err.errors.contains(CssErrorCode.GRAMMAR_PREMATURE_EOF),
        "Error handler should receive GRAMMAR_PREMATURE_EOF");
  }

  @Test
  @DisplayName("TC07: Null Reader argument throws NullPointerException")
  public void test_TC07() {
    // INPUT: null reader -> NPE at method entry
    CssParser parser = new CssParser();
    TestErrorHandler err = new TestErrorHandler();
    TestContentHandler doc = new TestContentHandler();

    assertThrows(NullPointerException.class,
        () -> parser.parse((java.io.Reader) null, "id", err, doc),
        "Null reader should cause NullPointerException");
  }
}