package org.idpf.epubcheck.util.css;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssGrammarException;
import org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test suite for CssParser.parse(Reader, String, CssErrorHandler, CssContentHandler)
 * covering scenarios TC11–TC14.
 */
public class CssParser_parse_1_Test
{
  // A simple error handler that records the last grammar exception.
  private static class RecordingErrorHandler implements CssErrorHandler
  {
    private CssGrammarException lastException;

    @Override
    public void error(CssGrammarException exception)
    {
      this.lastException = exception;
    }

    @Override
    public void error(CssException exception) {
      // Handle CssException if needed
    }

    public CssGrammarException getLastException()
    {
      return lastException;
    }
  }

  // A simple content handler that records event names in sequence.
  private static class RecordingContentHandler implements CssContentHandler
  {
    private final List<String> events = new ArrayList<>();

    public List<String> getEvents()
    {
      return events;
    }

    @Override
    public void startDocument() { events.add("startDocument"); }

    @Override
    public void endDocument() { events.add("endDocument"); }

    @Override
    public void startAtRule(org.idpf.epubcheck.util.css.CssGrammar.CssAtRule atRule)
    {
      // record the at-rule name
      events.add("startAtRule(" + atRule.getName().orElse("<no-name>") + ")");
    }

    @Override
    public void endAtRule(String atRuleName)
    {
      events.add("endAtRule(" + atRuleName + ")");
    }

    @Override
    public void selectors(List<org.idpf.epubcheck.util.css.CssGrammar.CssSelector> selectors)
    {
      events.add("selectors");
    }

    @Override
    public void endSelectors(List<org.idpf.epubcheck.util.css.CssGrammar.CssSelector> selectors)
    {
      events.add("endSelectors");
    }

    @Override
    public void declaration(org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration decl)
    {
      events.add("declaration");
    }
  }

  // Test cases follow...

  /**
   * TC11: Nested ATKEYWORD with inner ruleset exercises hasRuleSet==true path.
   * The input "@media { h1{color:blue;} }" forces the parser to:
   * - see an ATKEYWORD → handleAtRule
   * - inside, detect an inner '{' before any ';' → hasRuleSet()==true
   * - invoke handleRuleSet for h1 { ... }
   * - produce selectors and declaration events
   */
  @Test
  @DisplayName("Nested ATKEYWORD with inner ruleset exercises hasRuleSet==true path")
  public void test_TC11() throws IOException, CssException
  {
    Reader reader = new StringReader("@media { h1{color:blue;} }");
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();

    new CssParser().parse(reader, "id", err, doc);

    // Expect no grammar errors
    assertNull(err.getLastException(), "No errors expected for a valid nested at-rule.");
    // Expected sequence of content handler events:
    List<String> expected = List.of(
        "startDocument",
        "startAtRule(media)",
        "selectors",
        "declaration",
        "endSelectors",
        "endAtRule(media)",
        "endDocument");
    assertEquals(expected, doc.getEvents(),
        "Events should match startDocument, startAtRule(media), selectors, declaration, endSelectors, endAtRule(media), endDocument");
  }

  /**
   * TC12: Ruleset with unexpected ')' after selector exercises MATCH_CLOSEPAREN branch in handleRuleSet.
   * The input "h1){color:red;}" causes:
   * - first token h1 (IDENT), then next token ')' → triggers MATCH_CLOSEPAREN in handleRuleSet
   * - error handler records GRAMMAR_UNEXPECTED_TOKEN
   * - parser returns immediately (no selectors/declarations)
   */
  @Test
  @DisplayName("Ruleset with unexpected ')' after selector exercises MATCH_CLOSEPAREN branch")
  public void test_TC12() throws IOException
  {
    Reader reader = new StringReader("h1){color:red;}");
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();

    // We expect no CssException thrown; the grammar error is reported via handler.
    assertDoesNotThrow(() -> new CssParser().parse(reader, "id", err, doc));

    assertNotNull(err.getLastException(), "A grammar exception should have been reported.");
    assertEquals(CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN,
        err.getLastException().getErrorCode(),
        "Error code should be GRAMMAR_UNEXPECTED_TOKEN for unexpected ')'.");
    // Only startDocument and endDocument should be emitted
    List<String> expectedEvents = List.of("startDocument", "endDocument");
    assertEquals(expectedEvents, doc.getEvents(),
        "Only startDocument and endDocument expected when selectors parse fails early.");
  }

  /**
   * TC13: Ruleset with selector-list error causing selectors==null path in handleRuleSet.
   * The input "{color:red;}" has no selector before '{', so createSelectorList returns null:
   * - error handler records GRAMMAR_EXPECTING_TOKEN
   * - parser skips to matching '}' and returns
   */
  @Test
  @DisplayName("Ruleset with selector-list error causing selectors==null")
  public void test_TC13() throws IOException
  {
    Reader reader = new StringReader("{color:red;}");
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();

    assertDoesNotThrow(() -> new CssParser().parse(reader, "id", err, doc));

    assertNotNull(err.getLastException(), "A grammar exception should have been reported for missing selector.");
    assertEquals(CssErrorCode.GRAMMAR_EXPECTING_TOKEN,
        err.getLastException().getErrorCode(),
        "Error code should be GRAMMAR_EXPECTING_TOKEN for empty selector list.");
    // Only startDocument and endDocument should be emitted
    List<String> expectedEvents = List.of("startDocument", "endDocument");
    assertEquals(expectedEvents, doc.getEvents(),
        "Only startDocument and endDocument expected when selector list is null.");
  }

  /**
   * TC14: ATKEYWORD param error exercises param==null branch and skip-to-close logic in handleAtRule.
   * The input "@foo %invalid;{}" causes:
   * - handleAtRuleParam returns null on '%'
   * - error handler records GRAMMAR_UNEXPECTED_TOKEN
   * - content handler sees startAtRule(foo) then immediate endAtRule(foo)
   */
  @Test
  @DisplayName("ATKEYWORD param error exercises param==null branch and skip-to-close logic")
  public void test_TC14() throws IOException, CssException
  {
    Reader reader = new StringReader("@foo %invalid;{}");
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();

    new CssParser().parse(reader, "id", err, doc);

    assertNotNull(err.getLastException(), "A grammar exception should be reported for invalid atrule param.");
    assertEquals(CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN,
        err.getLastException().getErrorCode(),
        "Error code should be GRAMMAR_UNEXPECTED_TOKEN for invalid at-rule parameter.");
    // Expected events: startDocument, startAtRule(foo), endAtRule(foo), endDocument
    List<String> expected = List.of(
        "startDocument",
        "startAtRule(foo)",
        "endAtRule(foo)",
        "endDocument");
    assertEquals(expected, doc.getEvents(),
        "Events should match startDocument, startAtRule(foo), endAtRule(foo), endDocument for invalid param.");
  }
}