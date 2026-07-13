package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.Token.TokenType;
import com.ezylang.evalex.parser.ParseException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class Tokenizer_parse_2_Test {

  @Test
  @DisplayName("Implicit multiplication between number and opening brace inserts '*' when allowed (NUMBER_LITERAL→BRACE_OPEN)")
  void test_TC23() throws Exception {
    // Input "2(3)": previous token is NUMBER_LITERAL before BRACE_OPEN, implicit multiplication allowed
    String expr = "2(3)";
    ExpressionConfiguration cfg = ExpressionConfiguration.builder()
        .setImplicitMultiplicationAllowed(true)
        .build();
    Tokenizer tokenizer = new Tokenizer(expr, cfg);

    List<Token> tokens = tokenizer.parse();

    // Expect sequence: 2, *, (, 3, )
    assertEquals(5, tokens.size(), "Should have 5 tokens including implicit '*'");
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("2", tokens.get(0).getTokenString());
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(1).getType());
    assertEquals("*", tokens.get(1).getTokenString());
    assertEquals(TokenType.BRACE_OPEN, tokens.get(2).getType());
    assertEquals("(", tokens.get(2).getTokenString());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(3).getType());
    assertEquals("3", tokens.get(3).getTokenString());
    assertEquals(TokenType.BRACE_CLOSE, tokens.get(4).getType());
    assertEquals(")", tokens.get(4).getTokenString());
  }

  @Test
  @DisplayName("Missing operator exception for disallowed implicit multiplication between number and brace (NUMBER_LITERAL→BRACE_OPEN)")
  void test_TC24() {
    // Input "2(3)": implicit multiplication disallowed leads to exception
    String expr = "2(3)";
    ExpressionConfiguration cfg = ExpressionConfiguration.builder()
        .setImplicitMultiplicationAllowed(false)
        .build();
    Tokenizer tokenizer = new Tokenizer(expr, cfg);

    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Missing operator", ex.getMessage());
  }

  @Test
  @DisplayName("Decimal number starting with dot is parsed as NUMBER_LITERAL ('.5')")
  void test_TC25() throws Exception {
    // Input ".5": dot-start triggers isAtNumberStart path, parseDecimalNumberLiteral
    String expr = ".5";
    ExpressionConfiguration cfg = ExpressionConfiguration.defaultConfiguration();
    Tokenizer tokenizer = new Tokenizer(expr, cfg);

    List<Token> tokens = tokenizer.parse();
    assertEquals(1, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals(".5", tokens.get(0).getTokenString());
  }

  @Test
  @DisplayName("Undefined operator throws exception when parsing unrecognized symbol '@'")
  void test_TC26() {
    // Input "@": not a number, identifier, or known operator -> undefined operator exception
    String expr = "@";
    ExpressionConfiguration cfg = ExpressionConfiguration.defaultConfiguration();
    Tokenizer tokenizer = new Tokenizer(expr, cfg);

    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertTrue(ex.getMessage().contains("Undefined operator '@'"));
  }

  @Test
  @DisplayName("Valid hexadecimal literal is parsed as NUMBER_LITERAL ('0xAB')")
  void test_TC27() throws Exception {
    // Input "0xAB": starts with 0x triggers parseHexNumberLiteral
    String expr = "0xAB";
    ExpressionConfiguration cfg = ExpressionConfiguration.defaultConfiguration();
    Tokenizer tokenizer = new Tokenizer(expr, cfg);

    List<Token> tokens = tokenizer.parse();
    assertEquals(1, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("0xAB", tokens.get(0).getTokenString());
  }

  @Test
  @DisplayName("Valid scientific notation number is parsed as NUMBER_LITERAL ('1.2e+3')")
  void test_TC28() throws Exception {
    // Input "1.2e+3": contains 'e' triggers scientific notation path
    String expr = "1.2e+3";
    ExpressionConfiguration cfg = ExpressionConfiguration.defaultConfiguration();
    Tokenizer tokenizer = new Tokenizer(expr, cfg);

    List<Token> tokens = tokenizer.parse();
    assertEquals(1, tokens.size());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    assertEquals("1.2e+3", tokens.get(0).getTokenString());
  }

  @Test
  @DisplayName("Unexpected token after infix operator throws exception on '1++2'")
  void test_TC29() {
    // Input "1++2": after parsing '1' and '+' infix, next '+' is invalid after infix
    String expr = "1++2";
    ExpressionConfiguration cfg = ExpressionConfiguration.defaultConfiguration();
    Tokenizer tokenizer = new Tokenizer(expr, cfg);

    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Unexpected token after infix operator", ex.getMessage());
  }

  @Test
  @DisplayName("Unexpected closing array after valid array throws 'Unexpected closing array' ('[1]]')")
  void test_TC30() {
    // Input "[1]]": arrays allowed, second ']' decrements arrayBalance below zero
    String expr = "[1]]";
    ExpressionConfiguration cfg = ExpressionConfiguration.builder()
        .setArraysAllowed(true)
        .build();
    Tokenizer tokenizer = new Tokenizer(expr, cfg);

    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Unexpected closing array", ex.getMessage());
  }

  @Test
  @DisplayName("Array open not allowed at start throws 'Array open not allowed here' ('[')")
  void test_TC31() {
    // Input "[": arrays allowed, first token '[' validation fails arrayOpenOrStructureSeparatorNotAllowed
    String expr = "[";
    ExpressionConfiguration cfg = ExpressionConfiguration.builder()
        .setArraysAllowed(true)
        .build();
    Tokenizer tokenizer = new Tokenizer(expr, cfg);

    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Array open not allowed here", ex.getMessage());
  }
}