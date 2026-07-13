package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.Tokenizer;
import com.ezylang.evalex.parser.Token.TokenType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class Tokenizer_parse_2_Test {

  // A minimal stub configuration to control allowed features and operator/function dictionaries.
  static class StubConfig implements ExpressionConfiguration {
    private final boolean implicitMult, arraysAllowed, structuresAllowed, singleQuoteAllowed;
    private final OperatorDictionaryIfc opDict = new OperatorDictionaryIfc() {
      @Override public boolean hasInfixOperator(String s) { return "*".equals(s); }
      @Override public OperatorIfc getInfixOperator(String s) { return (args -> null); }
      @Override public boolean hasPrefixOperator(String s) { return false; }
      @Override public OperatorIfc getPrefixOperator(String s) { return null; }
      @Override public boolean hasPostfixOperator(String s) { return false; }
      @Override public OperatorIfc getPostfixOperator(String s) { return null; }
    };
    private final FunctionDictionaryIfc funcDict = new FunctionDictionaryIfc() {
      @Override public boolean hasFunction(String name) { return false; }
      @Override public FunctionIfc getFunction(String name) { return null; }
    };

    StubConfig(boolean implicitMult, boolean arraysAllowed,
               boolean structuresAllowed, boolean singleQuoteAllowed) {
      this.implicitMult = implicitMult;
      this.arraysAllowed = arraysAllowed;
      this.structuresAllowed = structuresAllowed;
      this.singleQuoteAllowed = singleQuoteAllowed;
    }

    @Override public OperatorDictionaryIfc getOperatorDictionary() { return opDict; }
    @Override public FunctionDictionaryIfc getFunctionDictionary() { return funcDict; }
    @Override public boolean isImplicitMultiplicationAllowed() { return implicitMult; }
    @Override public boolean isArraysAllowed() { return arraysAllowed; }
    @Override public boolean isStructuresAllowed() { return structuresAllowed; }
    @Override public boolean isSingleQuoteStringLiteralsAllowed() { return singleQuoteAllowed; }
  }

  @Test
  @DisplayName("Implicit multiplication between ')' and '(' disallowed throws Missing operator")
  void test_TC14() {
    // ')' then '(' would trigger implicitMultiplicationPossible true, but implicit mult disabled
    String expr = ")(";
    Tokenizer tokenizer = new Tokenizer(expr, new StubConfig(false, true, true, true));
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertTrue(ex.getMessage().contains("Missing operator"));
  }

  @Test
  @DisplayName("Implicit multiplication between number and variable inserts '*'")
  void test_TC15() throws ParseException {
    // digits then identifier triggers implicitMult and insertion of '*' when allowed
    String expr = "2x";
    Tokenizer tokenizer = new Tokenizer(expr, new StubConfig(true, true, true, true));
    List<Token> tokens = tokenizer.parse();
    assertEquals(3, tokens.size());
    assertEquals("2", tokens.get(0).getToken());
    assertEquals("*", tokens.get(1).getToken());
    assertEquals("x", tokens.get(2).getToken());
  }

  @Test
  @DisplayName("Implicit multiplication between number and variable disallowed throws Missing operator")
  void test_TC16() {
    // same as TC15 but implicit mult disabled should error on missing operator
    String expr = "2x";
    Tokenizer tokenizer = new Tokenizer(expr, new StubConfig(false, true, true, true));
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertTrue(ex.getMessage().contains("Missing operator"));
  }

  @Test
  @DisplayName("Unexpected token after infix operator throws Unexpected token after infix operator")
  void test_TC17() {
    // expression "2+)" has infix '+' then immediate BRACE_CLOSE which is invalid after INFIX_OPERATOR
    String expr = "2+)";
    Tokenizer tokenizer = new Tokenizer(expr, new StubConfig(true, true, true, true));
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertTrue(ex.getMessage().contains("Unexpected token after infix operator"));
  }

  @Test
  @DisplayName("Balanced array '[1]' returns tokens ARRAY_OPEN, NUMBER_LITERAL, ARRAY_CLOSE")
  void test_TC18() throws ParseException {
    // '[' then number then ']' with arraysAllowed true should produce ARRAY_OPEN, NUMBER_LITERAL, ARRAY_CLOSE
    String expr = "[1]";
    Tokenizer tokenizer = new Tokenizer(expr, new StubConfig(true, true, true, true));
    List<Token> tokens = tokenizer.parse();
    assertEquals(3, tokens.size());
    assertEquals(TokenType.ARRAY_OPEN, tokens.get(0).getType());
    assertEquals("1", tokens.get(1).getToken());
    assertEquals(TokenType.ARRAY_CLOSE, tokens.get(2).getType());
  }

  @Test
  @DisplayName("Structure separator between variables when allowed produces VARIABLE, '.', VARIABLE")
  void test_TC19() throws ParseException {
    // 'a.b' with structuresAllowed true and no number conflict: '.' recognized as structure separator
    String expr = "a.b";
    Tokenizer tokenizer = new Tokenizer(expr, new StubConfig(true, true, true, true));
    List<Token> tokens = tokenizer.parse();
    assertEquals(3, tokens.size());
    assertEquals("a", tokens.get(0).getToken());
    assertEquals(".", tokens.get(1).getToken());
    assertEquals("b", tokens.get(2).getToken());
  }

  @Test
  @DisplayName("Double-quoted string literal is parsed as STRING_LITERAL")
  void test_TC20() throws ParseException {
    // a quoted literal "hello"
    String expr = "\"hello\"";
    Tokenizer tokenizer = new Tokenizer(expr, new StubConfig(true, true, true, false));
    List<Token> tokens = tokenizer.parse();
    assertEquals(1, tokens.size());
    assertEquals(TokenType.STRING_LITERAL, tokens.get(0).getType());
    assertEquals("hello", tokens.get(0).getToken());
  }
}