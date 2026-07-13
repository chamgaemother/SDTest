package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.Tokenizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class Tokenizer_parse_0_Test {

  /**
   * Scenario TC01:
   * Empty expression: loop never iterates (getNextToken() returns null immediately),
   * braceBalance == 0, arrayBalance == 0 → normal return with empty list.
   */
  @Test
  @DisplayName("Empty expression returns empty token list (loop-0, braceBalance>0 false, arrayBalance>0 false)")
  public void test_TC01() throws Exception {
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated to match correct constructor
    Tokenizer tokenizer = new Tokenizer("", config);
    List<Token> result = tokenizer.parse();
    assertTrue(result.isEmpty(), "Expected empty token list for empty expression");
  }

  /**
   * Scenario TC02:
   * Implicit multiplication disallowed: "2(x)" → first token NUMBER_LITERAL,
   * next token is BRACE_OPEN → implicitMultiplicationPossible true,
   * config.isImplicitMultiplicationAllowed() false → throws Missing operator.
   */
  @Test
  @DisplayName("Implicit multiplication disallowed causes Missing operator for NUMBER_LITERAL→BRACE_OPEN")
  public void test_TC02() {
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated to match correct constructor
    config.setImplicitMultiplicationAllowed(false);
    Tokenizer tokenizer = new Tokenizer("2(x)", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Missing operator", ex.getMessage());
  }

  /**
   * Scenario TC03:
   * Implicit multiplication allowed: "2(x)" → NUMBER_LITERAL then '*' inserted, then '(' as BRACE_OPEN,
   * then VARIABLE_OR_CONSTANT 'x', then BRACE_CLOSE ')'.
   */
  @Test
  @DisplayName("Implicit multiplication allowed inserts '*' token between NUMBER_LITERAL and BRACE_OPEN")
  public void test_TC03() throws Exception {
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated to match correct constructor
    config.setImplicitMultiplicationAllowed(true);
    Tokenizer tokenizer = new Tokenizer("2(x)", config);
    List<Token> tokens = tokenizer.parse();
    // Expect [ NUMBER_LITERAL("2"), INFIX_OPERATOR("*"), BRACE_OPEN("("), VARIABLE_OR_CONSTANT("x"), BRACE_CLOSE(")") ]
    assertAll("Token sequence and types",
        () -> assertEquals(5, tokens.size(), "Should produce 5 tokens"),
        () -> assertEquals(Token.TokenType.NUMBER_LITERAL, tokens.get(0).getType()),
        () -> assertEquals("2", tokens.get(0).getValue()),
        () -> assertEquals(Token.TokenType.INFIX_OPERATOR, tokens.get(1).getType()),
        () -> assertEquals("*", tokens.get(1).getValue()),
        () -> assertEquals(Token.TokenType.BRACE_OPEN, tokens.get(2).getType()),
        () -> assertEquals("(", tokens.get(2).getValue()),
        () -> assertEquals(Token.TokenType.VARIABLE_OR_CONSTANT, tokens.get(3).getType()),
        () -> assertEquals("x", tokens.get(3).getValue()),
        () -> assertEquals(Token.TokenType.BRACE_CLOSE, tokens.get(4).getType()),
        () -> assertEquals(")", tokens.get(4).getValue())
    );
  }

  /**
   * Scenario TC04:
   * Unclosed brace: "(1+2" → parse tokens until loop exit, braceBalance == 1 → throws Closing brace not found.
   */
  @Test
  @DisplayName("Unclosed brace throws Closing brace not found after end-of-input")
  public void test_TC04() {
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated to match correct constructor
    Tokenizer tokenizer = new Tokenizer("(1+2", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Closing brace not found", ex.getMessage());
  }

  /**
   * Scenario TC05:
   * Unclosed array: "[1,2" → parse tokens until loop exit, braceBalance == 0, arrayBalance == 1 → throws Closing array not found.
   */
  @Test
  @DisplayName("Unclosed array throws Closing array not found after end-of-input")
  public void test_TC05() {
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated to match correct constructor
    config.setArraysAllowed(true);
    Tokenizer tokenizer = new Tokenizer("[1,2", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Closing array not found", ex.getMessage());
  }

  /**
   * Scenario TC06:
   * Misplaced structure operator at start: ".x" → first token STRUCTURE_SEPARATOR, no previousToken → validateToken sees STRUCTURE_SEPARATOR at start → throws Misplaced structure operator.
   */
  @Test
  @DisplayName("Misplaced structure operator at start throws Misplaced structure operator")
  public void test_TC06() {
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated to match correct constructor
    config.setStructuresAllowed(true);
    Tokenizer tokenizer = new Tokenizer(".x", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Misplaced structure operator", ex.getMessage());
  }
}