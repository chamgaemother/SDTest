package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.parser.Token.TokenType;
import com.ezylang.evalex.parser.ParseException;
public class Tokenizer_parse_1_Test {

  @Test
  @DisplayName("Valid structure separator between variable and variable when structures allowed")
  void test_TC12() throws Exception {
    // a.b should produce VARIABLE, STRUCTURE_SEPARATOR, VARIABLE
    ExpressionConfiguration config = new ExpressionConfiguration(true, true, true, true);
    Tokenizer tokenizer = new Tokenizer("a.b", config);
    List<Token> tokens = tokenizer.parse();
    assertEquals(3, tokens.size(), "Expected three tokens for 'a.b'");
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(0).getType());
    assertEquals(TokenType.STRUCTURE_SEPARATOR,    tokens.get(1).getType());
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(2).getType());
  }

  @Test
  @DisplayName("Unexpected closing array without opening throws \"Unexpected closing array\"")
  void test_TC13() {
    // Starting with ']' and arraysAllowed => immediate array close path and exception
    ExpressionConfiguration config = new ExpressionConfiguration(true, true, true, true);
    Tokenizer tokenizer = new Tokenizer("]", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertTrue(ex.getMessage().contains("Unexpected closing array"));
  }

  @Test
  @DisplayName("Hexadecimal number literal \"0x1A\" parsed as single NUMBER_LITERAL token")
  void test_TC14() throws Exception {
    // "0x1A" triggers hex literal parsing branch
    ExpressionConfiguration config = new ExpressionConfiguration(true, true, true, true);
    Tokenizer tokenizer = new Tokenizer("0x1A", config);
    List<Token> tokens = tokenizer.parse();
    assertEquals(1, tokens.size(), "Hex literal should be one token");
    Token t = tokens.get(0);
    assertEquals(TokenType.NUMBER_LITERAL, t.getType());
    assertEquals("0x1A", t.getTokenValue());
  }

  @Test
  @DisplayName("Undefined function name before '(' throws \"Undefined function 'foo'\"")
  void test_TC15() {
    // foo(1) with no foo in function dictionary => undefined function exception
    ExpressionConfiguration config = new ExpressionConfiguration(true, true, true, true);
    Tokenizer tokenizer = new Tokenizer("foo(1)", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertTrue(ex.getMessage().contains("Undefined function 'foo'"));
  }

  @Test
  @DisplayName("Unknown escape in single-quoted string throws \"Unknown escape character\"")
  void test_TC16() {
    // '\\q' with single-quote allowed, q is invalid escape => unknown escape path
    ExpressionConfiguration config = new ExpressionConfiguration(true, true, true, true);
    Tokenizer tokenizer = new Tokenizer("'\\q'", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertTrue(ex.getMessage().contains("Unknown escape character"));
  }

  @Test
  @DisplayName("Balanced array open and close returns ARRAY_OPEN and ARRAY_CLOSE tokens")
  void test_TC17() throws Exception {
    // [1] with arraysAllowed => open, number, close branches all valid
    ExpressionConfiguration config = new ExpressionConfiguration(true, true, true, true);
    Tokenizer tokenizer = new Tokenizer("[1]", config);
    List<Token> tokens = tokenizer.parse();
    assertEquals(3, tokens.size(), "Expected three tokens for '[1]'");
    assertEquals(TokenType.ARRAY_OPEN,  tokens.get(0).getType());
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(1).getType());
    assertEquals(TokenType.ARRAY_CLOSE, tokens.get(2).getType());
  }

  @Test
  @DisplayName("Implicit multiplication between NUMBER_LITERAL and BRACE_OPEN when allowed (1(2))")
  void test_TC18() throws Exception {
    // 1(2) with implicit multiplication allowed => insert '*' between 1 and '('
    ExpressionConfiguration config = new ExpressionConfiguration(true, true, true, true);
    Tokenizer tokenizer = new Tokenizer("1(2)", config);
    List<Token> tokens = tokenizer.parse();
    // tokens: 1, *, (, 2, )
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(1).getType(),
        "Expected '*' infix operator inserted for implicit multiplication");
    assertEquals("*", tokens.get(1).getTokenValue());
  }
}