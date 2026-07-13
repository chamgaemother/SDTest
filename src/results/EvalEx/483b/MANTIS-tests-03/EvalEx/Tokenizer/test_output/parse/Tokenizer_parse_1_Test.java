package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.Token.TokenType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class Tokenizer_parse_1_Test {

  @Test
  @DisplayName("TC11: Implicit multiplication between closing and opening brace inserts '*' when allowed")
  void test_TC11() throws Exception {
    // "(1)(2)" triggers implicitMultiplicationPossible between ')' and '('
    ExpressionConfiguration cfg = ExpressionConfiguration.builder()
        .setImplicitMultiplicationAllowed(true)
        .setArraysAllowed(false)
        .setStructuresAllowed(false)
        .build();
    com.ezylang.evalex.parser.Tokenizer tokenizer = new com.ezylang.evalex.parser.Tokenizer("(1)(2)", cfg);
    List<Token> tokens = tokenizer.parse();
    assertAll(
      () -> assertEquals(com.ezylang.evalex.parser.Token.TokenType.BRACE_OPEN, tokens.get(0).getType()),
      () -> assertEquals("1", tokens.get(1).getTokenString()),
      () -> assertEquals(com.ezylang.evalex.parser.Token.TokenType.BRACE_CLOSE, tokens.get(2).getType()),
      () -> assertEquals("*", tokens.get(3).getTokenString()),
      () -> assertEquals(com.ezylang.evalex.parser.Token.TokenType.BRACE_OPEN, tokens.get(4).getType()),
      () -> assertEquals("2", tokens.get(5).getTokenString()),
      () -> assertEquals(com.ezylang.evalex.parser.Token.TokenType.BRACE_CLOSE, tokens.get(6).getType())
    );
  }

  @Test
  @DisplayName("TC12: Missing operator exception for disallowed implicit multiplication between braces")
  void test_TC12() {
    // implicit multiplication disabled should throw on '(1)(2)'
    ExpressionConfiguration cfg = ExpressionConfiguration.builder()
        .setImplicitMultiplicationAllowed(false)
        .setArraysAllowed(false)
        .setStructuresAllowed(false)
        .build();
    com.ezylang.evalex.parser.Tokenizer tokenizer = new com.ezylang.evalex.parser.Tokenizer("(1)(2)", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Missing operator", ex.getMessage());
  }

  @Test
  @DisplayName("TC13: Unclosed array throws 'Closing array not found' after parse loop")
  void test_TC13() {
    // '[' without closing ']' should leave arrayBalance>0
    ExpressionConfiguration cfg = ExpressionConfiguration.builder()
        .setArraysAllowed(true)
        .setImplicitMultiplicationAllowed(false)
        .setStructuresAllowed(false)
        .build();
    com.ezylang.evalex.parser.Tokenizer tokenizer = new com.ezylang.evalex.parser.Tokenizer("[1", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Closing array not found", ex.getMessage());
  }

  @Test
  @DisplayName("TC14: Unexpected closing array at start throws 'Array close not allowed here'")
  void test_TC14() {
    // ']' at start, no previous token, arrayCloseAllowed() false
    ExpressionConfiguration cfg = ExpressionConfiguration.builder()
        .setArraysAllowed(true)
        .setImplicitMultiplicationAllowed(false)
        .setStructuresAllowed(false)
        .build();
    com.ezylang.evalex.parser.Tokenizer tokenizer = new com.ezylang.evalex.parser.Tokenizer("]", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Array close not allowed here", ex.getMessage());
  }

  @Test
  @DisplayName("TC15: Parse valid bracketed array '[1]' when arrays allowed")
  void test_TC15() throws Exception {
    // '[' then '1' then ']' with arraysAllowed true
    ExpressionConfiguration cfg = ExpressionConfiguration.builder()
        .setArraysAllowed(true)
        .setImplicitMultiplicationAllowed(false)
        .setStructuresAllowed(false)
        .build();
    com.ezylang.evalex.parser.Tokenizer tokenizer = new com.ezylang.evalex.parser.Tokenizer("[1]", cfg);
    List<Token> tokens = tokenizer.parse();
    assertAll(
      () -> assertEquals(com.ezylang.evalex.parser.Token.TokenType.ARRAY_OPEN, tokens.get(0).getType()),
      () -> assertEquals("1", tokens.get(1).getTokenString()),
      () -> assertEquals(com.ezylang.evalex.parser.Token.TokenType.ARRAY_CLOSE, tokens.get(2).getType())
    );
  }

  @Test
  @DisplayName("TC16: Valid structure separator between variable and field when allowed")
  void test_TC16() throws Exception {
    // 'x.y' structure separator '.' allowed and not number-char
    ExpressionConfiguration cfg = ExpressionConfiguration.builder()
        .setStructuresAllowed(true)
        .setArraysAllowed(false)
        .setImplicitMultiplicationAllowed(false)
        .build();
    com.ezylang.evalex.parser.Tokenizer tokenizer = new com.ezylang.evalex.parser.Tokenizer("x.y", cfg);
    List<Token> tokens = tokenizer.parse();
    assertAll(
      () -> assertEquals(com.ezylang.evalex.parser.Token.TokenType.VARIABLE_OR_CONSTANT, tokens.get(0).getType()),
      () -> assertEquals(com.ezylang.evalex.parser.Token.TokenType.STRUCTURE_SEPARATOR, tokens.get(1).getType()),
      () -> assertEquals(com.ezylang.evalex.parser.Token.TokenType.VARIABLE_OR_CONSTANT, tokens.get(2).getType())
    );
  }

  @Test
  @DisplayName("TC17: Decimal literal with two dots throws 'Number contains more than one decimal point'")
  void test_TC17() {
    // '1.2.3' has two dots, dotEncountered triggers exception
    ExpressionConfiguration cfg = ExpressionConfiguration.defaultConfiguration();
    com.ezylang.evalex.parser.Tokenizer tokenizer = new com.ezylang.evalex.parser.Tokenizer("1.2.3", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Number contains more than one decimal point", ex.getMessage());
  }

  @Test
  @DisplayName("TC18: Illegal scientific notation '1e+' throws 'Illegal scientific format'")
  void test_TC18() {
    // '1e+' ends in '+' after 'e', illegal scientificNotation
    ExpressionConfiguration cfg = ExpressionConfiguration.defaultConfiguration();
    com.ezylang.evalex.parser.Tokenizer tokenizer = new com.ezylang.evalex.parser.Tokenizer("1e+", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Illegal scientific format", ex.getMessage());
  }

  @Test
  @DisplayName("TC19: Hexadecimal literal '0x1A3f' is parsed as one NUMBER_LITERAL")
  void test_TC19() throws Exception {
    // '0x1A3f' triggers parseHexNumberLiteral path
    ExpressionConfiguration cfg = ExpressionConfiguration.defaultConfiguration();
    com.ezylang.evalex.parser.Tokenizer tokenizer = new com.ezylang.evalex.parser.Tokenizer("0x1A3f", cfg);
    List<Token> tokens = tokenizer.parse();
    assertEquals(1, tokens.size());
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("0x1A3f", tokens.get(0).getTokenString());
  }

  @Test
  @DisplayName("TC20: String literal with missing closing quote throws 'Closing quote not found'")
  void test_TC20() {
    // '"abc' missing closing double quote
    ExpressionConfiguration cfg = ExpressionConfiguration.defaultConfiguration();
    com.ezylang.evalex.parser.Tokenizer tokenizer = new com.ezylang.evalex.parser.Tokenizer("\"abc", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Closing quote not found", ex.getMessage());
  }

  @Test
  @DisplayName("TC21: Unknown escape sequence in string throws 'Unknown escape character'")
  void test_TC21() {
    // '\\q' inside single-quoted string, unknown escape char
    ExpressionConfiguration cfg = ExpressionConfiguration.builder()
        .setSingleQuoteStringLiteralsAllowed(true)
        .build();
    com.ezylang.evalex.parser.Tokenizer tokenizer = new com.ezylang.evalex.parser.Tokenizer("'\\q'", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Unknown escape character", ex.getMessage());
  }

  @Test
  @DisplayName("TC22: Undefined function name before '(' throws 'Undefined function'")
  void test_TC22() {
    // 'foo(1)' where foo not in functionDictionary
    ExpressionConfiguration cfg = ExpressionConfiguration.defaultConfiguration();
    com.ezylang.evalex.parser.Tokenizer tokenizer = new com.ezylang.evalex.parser.Tokenizer("foo(1)", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertTrue(ex.getMessage().contains("Undefined function 'foo'"));
  }
}