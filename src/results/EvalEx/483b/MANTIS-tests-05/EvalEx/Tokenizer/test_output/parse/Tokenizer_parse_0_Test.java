package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

/**
 * JUnit tests for Tokenizer.parse() based on provided scenarios.
 */
public class Tokenizer_parse_0_Test {

  private static final OperatorDictionaryIfc EMPTY_OP_DICT = new OperatorDictionaryIfc() {
    @Override
    public boolean hasInfixOperator(String s) { return "*".equals(s); }
    @Override
    public OperatorIfc getInfixOperator(String s) { return null; }
    @Override
    public boolean hasPrefixOperator(String s) { return false; }
    @Override
    public OperatorIfc getPrefixOperator(String s) { return null; }
    @Override
    public boolean hasPostfixOperator(String s) { return false; }
    @Override
    public OperatorIfc getPostfixOperator(String s) { return null; }
    @Override
    public void addOperator(String s, OperatorIfc op) {} // Implemented missing method
  };

  private static final FunctionDictionaryIfc EMPTY_FN_DICT = new FunctionDictionaryIfc() {
    @Override
    public boolean hasFunction(String s) { return false; }
    @Override
    public FunctionIfc getFunction(String s) { return null; }
    @Override
    public void addFunction(String s, FunctionIfc fn) {} // Implemented missing method
  };

  private static ExpressionConfiguration config(boolean implicitMult,
                                                boolean allowArrays,
                                                boolean allowStruct,
                                                boolean allowSingleQuote) {
    return new ExpressionConfiguration(EMPTY_OP_DICT, EMPTY_FN_DICT, implicitMult, allowArrays, allowStruct); // Removed allowSingleQuote as per constructor
  }

  @Test
  @DisplayName("TC01 Empty expression returns empty token list without exception")
  void test_TC01() throws Exception {
    Tokenizer tz = new Tokenizer("", config(false, false, false, false));
    List<com.ezylang.evalex.parser.Token> tokens = tz.parse(); // Fully qualified Token class
    assertTrue(tokens.isEmpty(), "Expected no tokens for empty input");
  }

  @Test
  @DisplayName("TC02 Single number literal produces one NUMBER_LITERAL token")
  void test_TC02() throws Exception {
    Tokenizer tz = new Tokenizer("42", config(false, false, false, false));
    List<com.ezylang.evalex.parser.Token> tokens = tz.parse(); // Fully qualified Token class
    assertEquals(1, tokens.size());
    com.ezylang.evalex.parser.Token t = tokens.get(0); // Fully qualified Token class
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.NUMBER_LITERAL, t.getType()); // Fully qualified Token class
    assertEquals("42", t.getTokenValue());
  }

  @Test
  @DisplayName("TC03 Implicit multiplication inserted when NUMBER followed by BRACE_OPEN and allowed")
  void test_TC03() throws Exception {
    Tokenizer tz = new Tokenizer("2(3)", config(true, false, false, false));
    List<com.ezylang.evalex.parser.Token> tokens = tz.parse(); // Fully qualified Token class
    assertEquals(5, tokens.size());
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.NUMBER_LITERAL, tokens.get(0).getType()); // Fully qualified Token class
    assertEquals("2", tokens.get(0).getTokenValue());
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.INFIX_OPERATOR, tokens.get(1).getType()); // Fully qualified Token class
    assertEquals("*", tokens.get(1).getTokenValue());
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.BRACE_OPEN, tokens.get(2).getType()); // Fully qualified Token class
    assertEquals("(", tokens.get(2).getTokenValue());
  }

  @Test
  @DisplayName("TC04 Missing operator exception when NUMBER before BRACE_OPEN and implicit multiplication disallowed")
  void test_TC04() {
    Tokenizer tz = new Tokenizer("2(3)", config(false, false, false, false));
    ParseException ex = assertThrows(ParseException.class, tz::parse);
    assertEquals("Missing operator", ex.getMessage());
  }

  @Test
  @DisplayName("TC05 Unexpected token after INFIX_OPERATOR triggers validateToken exception")
  void test_TC05() {
    Tokenizer tz = new Tokenizer("+*", config(false, false, false, false));
    ParseException ex = assertThrows(ParseException.class, tz::parse);
    assertEquals("Unexpected token after infix operator", ex.getMessage());
  }

  @Test
  @DisplayName("TC06 Misplaced structure separator at start triggers exception")
  void test_TC06() {
    Tokenizer tz = new Tokenizer(".a", config(false, false, true, false));
    ParseException ex = assertThrows(ParseException.class, tz::parse);
    assertEquals("Misplaced structure operator", ex.getMessage());
  }

  @Test
  @DisplayName("TC07 Unmatched closing brace at end triggers closing brace exception")
  void test_TC07() {
    Tokenizer tz = new Tokenizer("(", config(false, false, false, false));
    ParseException ex = assertThrows(ParseException.class, tz::parse);
    assertEquals("Closing brace not found", ex.getMessage());
  }

  @Test
  @DisplayName("TC08 Array open and close produce ARRAY_OPEN and ARRAY_CLOSE tokens")
  void test_TC08() throws Exception {
    Tokenizer tz = new Tokenizer("[1]", config(false, true, false, false));
    List<com.ezylang.evalex.parser.Token> tokens = tz.parse(); // Fully qualified Token class
    assertEquals(3, tokens.size());
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.ARRAY_OPEN, tokens.get(0).getType()); // Fully qualified Token class
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.NUMBER_LITERAL, tokens.get(1).getType()); // Fully qualified Token class
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.ARRAY_CLOSE, tokens.get(2).getType()); // Fully qualified Token class
  }

  @Test
  @DisplayName("TC09 Undefined operator sequence triggers undefined operator exception")
  void test_TC09() {
    Tokenizer tz = new Tokenizer("$", config(false, false, false, false));
    assertThrows(ParseException.class, tz::parse);
  }

  @Test
  @DisplayName("TC10 String literal parsing with escape and closing quote")
  void test_TC10() throws Exception {
    Tokenizer tz = new Tokenizer("'a\\'b'", config(false, false, false, true));
    List<com.ezylang.evalex.parser.Token> tokens = tz.parse(); // Fully qualified Token class
    assertEquals(1, tokens.size());
    com.ezylang.evalex.parser.Token t = tokens.get(0); // Fully qualified Token class
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.STRING_LITERAL, t.getType()); // Fully qualified Token class
    assertEquals("a'b", t.getTokenValue());
  }
}