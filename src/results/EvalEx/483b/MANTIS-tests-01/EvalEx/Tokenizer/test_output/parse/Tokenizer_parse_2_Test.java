package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.Token.TokenType;
import com.ezylang.evalex.parser.Tokenizer;
public class Tokenizer_parse_2_Test {

  @Test
  @DisplayName("parseBraceClose throws Unexpected closing brace when braceBalance < 0")
  void test_TC16() {
    // The input ")" causes parseBraceClose on empty stack (braceBalance=0 -> -1) → Unexpected closing brace
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated constructor
    Tokenizer tokenizer = new Tokenizer(")", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Unexpected closing brace", ex.getMessage());
  }

  @Test
  @DisplayName("parseArrayClose throws Unexpected closing array when arrayBalance < 0")
  void test_TC17() {
    // "x]" first yields VARIABLE_OR_CONSTANT 'x' then ']' with previous VARIABLE → allowed, arrayBalance 0->-1 → Unexpected closing array
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated constructor
    Tokenizer tokenizer = new Tokenizer("x]", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Unexpected closing array", ex.getMessage());
  }

  @Test
  @DisplayName("parseStructureSeparator produces valid token sequence when allowed between variables")
  void test_TC18() throws Exception {
    // "x.y": parseIdentifier 'x', then '.' with previous VARIABLE allowed, then 'y'
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated constructor
    Tokenizer tokenizer = new Tokenizer("x.y", config);
    List<Token> tokens = tokenizer.parse();
    assertEquals(3, tokens.size());
    assertAll("check types and values",
      () -> assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(0).getType()),
      () -> assertEquals("x", tokens.get(0).getValue()),
      () -> assertEquals(TokenType.STRUCTURE_SEPARATOR, tokens.get(1).getType()),
      () -> assertEquals(".", tokens.get(1).getValue()),
      () -> assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(2).getType()),
      () -> assertEquals("y", tokens.get(2).getValue())
    );
  }

  @Test
  @DisplayName("parseStructureSeparator throws when separator after BRACE_OPEN (not allowed)")
  void test_TC19() {
    // "(.x": '(' yields BRACE_OPEN then '.' with previous BRACE_OPEN → separator disallowed
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated constructor
    Tokenizer tokenizer = new Tokenizer("(.x", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Structure separator not allowed here", ex.getMessage());
  }

  @Test
  @DisplayName("parseArrayOpen throws Array open not allowed here at start (previousToken==null)")
  void test_TC20() {
    // "[1]": first char '[' with no previousToken → arrayOpenOrStructureSeparatorNotAllowed → Array open not allowed here
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated constructor
    Tokenizer tokenizer = new Tokenizer("[1]", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Array open not allowed here", ex.getMessage());
  }

  @Test
  @DisplayName("parseArrayClose throws Array close not allowed here when previous INFIX_OPERATOR")
  void test_TC21() {
    // "1+]": '1' NUMBER then '+' INFIX then ']' with previous INFIX_OPERATOR → arrayCloseAllowed false → Array close not allowed here
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated constructor
    Tokenizer tokenizer = new Tokenizer("1+]", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Array close not allowed here", ex.getMessage());
  }

  @Test
  @DisplayName("parseIdentifier throws on undefined function name before '('")
  void test_TC22() {
    // "foo()": identifier 'foo', next '(' detected, functionDictionary.hasFunction false → Undefined function 'foo'
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated constructor
    Tokenizer tokenizer = new Tokenizer("foo()", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Undefined function 'foo'", ex.getMessage());
  }

  @Test
  @DisplayName("parseDecimalNumberLiteral accepts valid scientific notation '3.14e2'")
  void test_TC23() throws Exception {
    // "3.14e2": valid decimal with scientific notation, no errors → one NUMBER_LITERAL token
    ExpressionConfiguration config = new ExpressionConfiguration(); // Updated constructor
    Tokenizer tokenizer = new Tokenizer("3.14e2", config);
    List<Token> tokens = tokenizer.parse();
    assertEquals(1, tokens.size());
    Token t = tokens.get(0);
    assertAll("scientific notation token",
      () -> assertEquals(TokenType.NUMBER_LITERAL, t.getType()),
      () -> assertEquals("3.14e2", t.getValue())
    );
  }
}