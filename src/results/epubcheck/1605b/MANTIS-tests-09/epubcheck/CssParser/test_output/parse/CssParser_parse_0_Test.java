package org.idpf.epubcheck.util.css;

import org.idpf.epubcheck.util.css.CssParser;
import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssGrammarException;
import org.idpf.epubcheck.util.css.CssErrorCode;
import org.idpf.epubcheck.util.css.CssErrorHandler;
import org.idpf.epubcheck.util.css.CssContentHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit5 tests for CssParser.parse(Reader, String, CssErrorHandler, CssContentHandler)
 * Covers scenarios TC01 through TC10.
 */
public class CssParser_parse_0_Test {

  /**
   * A no-op error handler for tests that should not record errors.
   */
  static class NoOpErrorHandler implements CssErrorHandler {
    @Override public void error(CssGrammarException e) { /* ignore */ }
    @Override public void error(CssException e) { /* ignore */ }
  }

  /**
   * A recording error handler to capture the last error code.
   */
  static class RecordingErrorHandler implements CssErrorHandler {
    CssGrammarException last;
    @Override
    public void error(CssGrammarException e) {
      last = e;
    }
    @Override public void error(CssException e) { /* ignore */ }
  }

  /**
   * A recording content handler to record the sequence of calls.
   */
  static class RecordingContentHandler implements CssContentHandler {
    List<String> events = new ArrayList<>();

    @Override public void startDocument() { events.add("startDocument"); }
    @Override public void endDocument() { events.add("endDocument"); }
    @Override public void startAtRule(org.idpf.epubcheck.util.css.CssGrammar.CssAtRule rule) {
      events.add("startAtRule(" + rule.getName().get() + ")");
    }
    @Override public void endAtRule(String name) {
      events.add("endAtRule(" + name + ")");
    }
    @Override public void selectors(List<org.idpf.epubcheck.util.css.CssGrammar.CssSelector> selectors) {
      events.add("selectors");
    }
    @Override public void endSelectors(List<org.idpf.epubcheck.util.css.CssGrammar.CssSelector> selectors) {
      events.add("endSelectors");
    }
    @Override public void declaration(org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration decl) {
      events.add("declaration");
    }
  }

  @Test @DisplayName("TC01: Empty CSS input results in no rule processing, covering loop-0 at B1")
  void test_TC01() throws IOException, CssException {
    StringReader reader = new StringReader("");
    NoOpErrorHandler err = new NoOpErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();
    // empty input: iterator.hasNext() false immediately
    new CssParser().parse(reader, "id", err, doc);
    assertEquals(List.of("startDocument", "endDocument"), doc.events);
  }

  @Test @DisplayName("TC02: Single ATKEYWORD token invokes handleAtRule once, covering branch-true at B2 and loop-1")
  void test_TC02() throws IOException, CssException {
    // "@media{}": one ATKEYWORD then a block with no selectors => startAtRule and endAtRule for "media"
    StringReader reader = new StringReader("@media{}");
    NoOpErrorHandler err = new NoOpErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();
    new CssParser().parse(reader, "id", err, doc);
    assertEquals(List.of("startDocument","startAtRule(media)","endAtRule(media)","endDocument"), doc.events);
  }

  @Test @DisplayName("TC03: Single non-ATKEYWORD token invokes handleRuleSet once, covering branch-false at B2 and loop-1")
  void test_TC03() throws IOException, CssException {
    // "h1{color:red;}": one ruleset with one declaration
    StringReader reader = new StringReader("h1{color:red;}");
    NoOpErrorHandler err = new NoOpErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();
    new CssParser().parse(reader, "id", err, doc);
    assertEquals(List.of("startDocument","selectors","declaration","endSelectors","endDocument"), doc.events);
  }

  @Test @DisplayName("TC04: Parser propagates IOException from scan, covering exceptional path during scan")
  void test_TC04() {
    // Reader that always throws IOException on read()
    Reader throwing = new Reader(){
      @Override public int read(char[] cbuf, int off, int len) throws IOException {
        throw new IOException("scan failure");
      }
      @Override public void close() { }
    };
    NoOpErrorHandler err = new NoOpErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();
    assertThrows(IOException.class, () ->
      new CssParser().parse(throwing, "id", err, doc)
    );
    assertTrue(doc.events.isEmpty());
  }

  @Test @DisplayName("TC05: Parser propagates CssException from scan, covering exceptional path during scan-cssexception")
  void test_TC05() {
    // Fake scan failure by subclassing CssParser and overriding private scan via reflection
    CssParser parser = new CssParser();
    // reflectively replace scan to throw CssException
    assertThrows(CssException.class, () -> {
      java.lang.reflect.Method m = CssParser.class.getDeclaredMethod("scan", Reader.class, String.class, CssErrorHandler.class);
      m.setAccessible(true);
      CssErrorHandler err = new NoOpErrorHandler();
      RecordingContentHandler doc = new RecordingContentHandler();
      // invoke scan to trigger CssException
      m.invoke(parser, new StringReader(""), "id", err);
    });
  }

  @Test @DisplayName("TC06: PrematureEOF in handleRuleSet breaks loop, covering exception path from handleRuleSet to B6")
  void test_TC06() throws IOException {
    // "h1{" leaves unterminated ruleset => grammar premature EOF in handleRuleSet
    StringReader reader = new StringReader("h1{");
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();
    new CssParser().parse(reader, "id", err, doc);
    assertNotNull(err.last);
    assertEquals(CssErrorCode.GRAMMAR_PREMATURE_EOF, err.last.getErrorCode());
    assertEquals(List.of("startDocument","endDocument"), doc.events);
  }

  @Test @DisplayName("TC07: Multiple tokens mix ATKEYWORD and ruleset, covering loop-N with N>1, both branches")
  void test_TC07() throws IOException, CssException {
    // mix: @foo{} h1{color:red;} @bar{}
    StringReader reader = new StringReader("@foo{} h1{color:red;} @bar{}");
    NoOpErrorHandler err = new NoOpErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();
    new CssParser().parse(reader, "id", err, doc);
    List<String> expected = List.of(
      "startDocument",
      "startAtRule(foo)","endAtRule(foo)",
      "selectors","declaration","endSelectors",
      "startAtRule(bar)","endAtRule(bar)",
      "endDocument");
    assertEquals(expected, doc.events);
  }

  @Test @DisplayName("TC08: ATKEYWORD without block but ending with ';' triggers no nested content, covering semi-open branch in handleAtRule then loop-1")
  void test_TC08() throws IOException, CssException {
    // "@charset \"UTF-8\";" is ATKEYWORD ending with semicolon, no block
    StringReader reader = new StringReader("@charset \"UTF-8\";");
    NoOpErrorHandler err = new NoOpErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();
    new CssParser().parse(reader, "id", err, doc);
    assertEquals(List.of("startDocument","startAtRule(charset)","endAtRule(charset)","endDocument"), doc.events);
  }

  @Test @DisplayName("TC09: RuleSet with comment tokens filtered tests FILTER_S_CMNT_CDO_CDC affecting hasNext and next")
  void test_TC09() throws IOException, CssException {
    // comments "/*c*/" skipped by FILTER_S_CMNT_CDO_CDC before ruleset
    StringReader reader = new StringReader("/*c*/h1{color:red;}");
    NoOpErrorHandler err = new NoOpErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();
    new CssParser().parse(reader, "id", err, doc);
    assertEquals(List.of("startDocument","selectors","declaration","endSelectors","endDocument"), doc.events);
  }

  @Test @DisplayName("TC10: PrematureEOF in handleAtRule param loop triggers EOF catch in handleAtRule and break, covering inner exception path")
  void test_TC10() throws IOException {
    // "@foo" without any terminator: EOF in handleAtRule
    StringReader reader = new StringReader("@foo");
    RecordingErrorHandler err = new RecordingErrorHandler();
    RecordingContentHandler doc = new RecordingContentHandler();
    new CssParser().parse(reader, "id", err, doc);
    assertNotNull(err.last);
    assertEquals(CssErrorCode.GRAMMAR_PREMATURE_EOF, err.last.getErrorCode());
    // ensure start and end at-rule events recorded before loop ends
    assertTrue(doc.events.contains("startAtRule(foo)"));
    assertTrue(doc.events.contains("endAtRule(foo)"));
    assertEquals("endDocument", doc.events.get(doc.events.size()-1));
  }
}