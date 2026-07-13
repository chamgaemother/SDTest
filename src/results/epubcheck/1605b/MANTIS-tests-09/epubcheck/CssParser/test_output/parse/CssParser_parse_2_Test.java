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
import org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode;
import org.idpf.epubcheck.util.css.CssGrammar.CssAtRule;
import org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelector;
import org.idpf.epubcheck.util.css.CssParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CssParser_parse_2_Test {

  /**
   * Simple error handler to record the last exception reported.
   */
  static class RecordingErrorHandler implements CssErrorHandler {
    private CssGrammarException lastException;

    @Override
    public void error(CssException e) {
      this.lastException = (CssGrammarException) e;
    }

    public CssGrammarException getLastException() {
      return lastException;
    }
  }

  /**
   * Simple content handler to record parsing events.
   */
  static class RecordingContentHandler implements CssContentHandler {
    List<String> events = new ArrayList<>();

    @Override
    public void startDocument() {
      events.add("startDocument");
    }

    @Override
    public void endDocument() {
      events.add("endDocument");
    }

    @Override
    public void startAtRule(CssAtRule rule) {
      events.add("startAtRule(" + rule.getName().orElse("") + ")");
    }

    @Override
    public void endAtRule(String atRuleName) {
      events.add("endAtRule(" + atRuleName + ")");
    }

    @Override
    public void selectors(List<CssSelector> selectors) {
      events.add("selectors");
    }

    @Override
    public void endSelectors(List<CssSelector> selectors) {
      events.add("endSelectors");
    }

    @Override
    public void declaration(CssDeclaration decl) {
      events.add("declaration");
    }
  }

  @Test
  @DisplayName("ATKEYWORD with block and only declarations exercises hasRuleSet==false path in handleAtRule")
  public void test_TC15() throws IOException, CssException {
    // Input has an @font-face at-rule with a single declaration, no nested ruleset
    Reader reader = new StringReader("@font-face{font-family:Arial;}\n");
    CssParser parser = new CssParser(Locale.ENGLISH);
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();

    parser.parse(reader, "id", (CssErrorHandler) err, (CssContentHandler) doc);

    // No grammar errors expected
    assertNull(err.getLastException(), "No error should be reported for well-formed at-rule with declarations");
    // Verify the precise sequence of events for hasRuleSet==false path
    List<String> ev = doc.events;
    assertEquals(5, ev.size(), "Expected exactly 5 events");
    assertEquals("startDocument", ev.get(0));
    assertEquals("startAtRule(font-face)", ev.get(1));
    assertEquals("declaration", ev.get(2));
    assertEquals("endAtRule(font-face)", ev.get(3));
    assertEquals("endDocument", ev.get(4));
  }

  @Test
  @DisplayName("Premature EOF inside at-rule block after nested ruleset triggers EOF catch in parse loop")
  public void test_TC16() throws IOException {
    // Input has an @media at-rule with an inner ruleset but missing closing '}' for the outer block:
    Reader reader = new StringReader("@media { h1{color:blue;} ");
    CssParser parser = new CssParser(Locale.ENGLISH);
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();

    // Expect no uncaught exception; parse should catch EOF and finish
    assertDoesNotThrow(() -> parser.parse(reader, "id", (CssErrorHandler) err, (CssContentHandler) doc),
        "Parsing should handle premature EOF internally and not throw");

    // The error handler must have received a GRAMMAR_PREMATURE_EOF error
    CssGrammarException le = err.getLastException();
    assertNotNull(le, "Expected a premature EOF grammar exception");
    assertEquals(CssErrorCode.GRAMMAR_PREMATURE_EOF, le.getErrorCode(),
        "Error code must be GRAMMAR_PREMATURE_EOF");

    // Verify that nested ruleset events were produced before EOF
    List<String> ev = doc.events;
    // Expect at least: startDocument, startAtRule(media), selectors, declaration, endSelectors, endDocument
    assertFalse(ev.isEmpty(), "Event list should not be empty");
    assertEquals("startDocument", ev.get(0));
    assertTrue(ev.contains("startAtRule(media)"), "Should have started @media at-rule");
    assertTrue(ev.contains("selectors"), "Inner ruleset selectors should be reported");
    assertTrue(ev.contains("declaration"), "Inner declaration should be reported");
    assertTrue(ev.contains("endSelectors"), "Inner ruleset endSelectors should be reported");
    assertEquals("endDocument", ev.get(ev.size() - 1), "Last event must be endDocument");
  }
}