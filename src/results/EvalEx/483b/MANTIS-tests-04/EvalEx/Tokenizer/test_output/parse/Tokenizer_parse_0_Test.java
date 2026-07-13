package com.ezylang.evalex.parser;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import com.ezylang.evalex.functions.FunctionIfc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ezylang.evalex.parser.Token.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class Tokenizer_parse_0_Test {

  // Helper to create a mocked default configuration: implicit, arrays, structures allowed.
  private ExpressionConfiguration defaultConfig() {
    ExpressionConfiguration cfg = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc fnDict = mock(FunctionDictionaryIfc.class);
    when(cfg.getOperatorDictionary()).thenReturn(opDict);
    when(cfg.getFunctionDictionary()).thenReturn(fnDict);
    when(cfg.isImplicitMultiplicationAllowed()).thenReturn(true);
    when(cfg.isArraysAllowed()).thenReturn(true);
    when(cfg.isStructuresAllowed()).thenReturn(true);
    // define that "*" is a known infix operator for implicit multiplication tests
    OperatorIfc dummyOp = mock(OperatorIfc.class);
    when(opDict.hasInfixOperator("*")).thenReturn(true);
    when(opDict.getInfixOperator("*")).thenReturn(dummyOp);
    // undefined operators by default
    when(opDict.hasPrefixOperator(anyString())).thenReturn(false);
    when(opDict.hasPostfixOperator(anyString())).thenReturn(false);
    when(opDict.hasInfixOperator(anyString())).thenAnswer(invocation -> {
      String op = invocation.getArgument(0);
      return "*".equals(op);
    });
    return cfg;
  }

  @Test
  @DisplayName("TC01: Empty expression returns empty token list (no loop iterations, no braces or arrays)")
  void test_TC01() throws Exception {
    // empty input => getNextToken returns null immediately, no braces/arrays => direct return empty list
    ExpressionConfiguration cfg = defaultConfig();
    Tokenizer tokenizer = new Tokenizer("", cfg);
    List<Token> result = tokenizer.parse();
    assertTrue(result.isEmpty(), "Expected no tokens for empty expression");
  }

  @Test
  @DisplayName("TC02: Single decimal number produces one NUMBER_LITERAL token and then exits loop")
  void test_TC02() throws Exception {
    // one number literal => loop once, then getNextToken returns null
    ExpressionConfiguration cfg = defaultConfig();
    Tokenizer tokenizer = new Tokenizer("123", cfg);
    List<Token> result = tokenizer.parse();
    assertEquals(1, result.size(), "One token expected");
    Token tok = result.get(0);
    assertEquals(NUMBER_LITERAL, tok.getType(), "Token type should be NUMBER_LITERAL");
    assertEquals("123", tok.getValue(), "Token value should equal input number");
  }

  @Test
  @DisplayName("TC03: Implicit multiplication inserts '*' between NUMBER_LITERAL and BRACE_OPEN when allowed")
  void test_TC03() throws Exception {
    // "2(3)" => number then brace open triggers implicitMultiplicationPossible true and allowed
    ExpressionConfiguration cfg = defaultConfig();
    Tokenizer tokenizer = new Tokenizer("2(3)", cfg);
    List<Token> result = tokenizer.parse();
    // expected tokens: [ "2" NUMBER_LITERAL, "*" INFIX_OPERATOR, "(" BRACE_OPEN, "3" NUMBER_LITERAL, ")" BRACE_CLOSE ]
    assertAll("Implicit multiplication sequence",
      () -> assertEquals(NUMBER_LITERAL, result.get(0).getType(), "First token is number"),
      () -> assertEquals(INFIX_OPERATOR, result.get(1).getType(), "Second token is '*' operator"),
      () -> assertEquals("*", result.get(1).getValue(), "Operator token should be '*'"),
      () -> assertEquals(BRACE_OPEN, result.get(2).getType(), "Third token is '('"),
      () -> assertEquals(NUMBER_LITERAL, result.get(3).getType(), "Fourth token is number inside braces"),
      () -> assertEquals(BRACE_CLOSE, result.get(4).getType(), "Fifth token is ')'")
    );
  }

  @Test
  @DisplayName("TC04: Implicit multiplication not allowed throws Missing operator after NUMBER_LITERAL then BRACE_OPEN")
  void test_TC04() {
    // "2(3)" => implicitMultiplicationPossible true but config disallows => exception at first loop iteration
    ExpressionConfiguration cfg = defaultConfig();
    when(cfg.isImplicitMultiplicationAllowed()).thenReturn(false);
    Tokenizer tokenizer = new Tokenizer("2(3)", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Missing operator", ex.getMessage(), "Expected Missing operator message");
  }

  @Test
  @DisplayName("TC05: Unclosed opening brace throws Closing brace not found")
  void test_TC05() {
    // "(" => one brace open, loop exits, braceBalance > 0 => error
    ExpressionConfiguration cfg = defaultConfig();
    Tokenizer tokenizer = new Tokenizer("(", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Closing brace not found", ex.getMessage(), "Expected unclosed brace message");
  }

  @Test
  @DisplayName("TC06: Unclosed opening array throws Closing array not found")
  void test_TC06() {
    // "[" => one array open, then error at end
    ExpressionConfiguration cfg = defaultConfig();
    when(cfg.isArraysAllowed()).thenReturn(true);
    Tokenizer tokenizer = new Tokenizer("[", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Closing array not found", ex.getMessage(), "Expected unclosed array message");
  }

  @Test
  @DisplayName("TC07: Structure separator at start throws Misplaced structure operator")
  void test_TC07() {
    // ".a" => first token STRUCTURE_SEPARATOR with no previous => error
    ExpressionConfiguration cfg = defaultConfig();
    when(cfg.isStructuresAllowed()).thenReturn(true);
    Tokenizer tokenizer = new Tokenizer(".a", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Misplaced structure operator", ex.getMessage(), "Expected misplaced structure operator");
  }

  @Test
  @DisplayName("TC08: Unexpected token after infix operator throws Unexpected token after infix operator")
  void test_TC08() {
    // "1,+2" => "1" NUMBER_LITERAL, "," COMMA or "+" treated as operator => infix then COMMA triggers invalidTokenAfterInfixOperator
    ExpressionConfiguration cfg = defaultConfig();
    Tokenizer tokenizer = new Tokenizer("1,+2", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Unexpected token after infix operator", ex.getMessage(), "Expected unexpected token after infix operator");
  }

  @Test
  @DisplayName("TC09: Undefined operator throws ParseException for unknown operator")
  void test_TC09() {
    // "??" => parseOperator sees unknown operator, throws ParseException with undefined operator message
    ExpressionConfiguration cfg = defaultConfig();
    Tokenizer tokenizer = new Tokenizer("??", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertTrue(ex.getMessage().contains("Undefined operator '??'"), "Expected undefined operator message");
  }

  @Test
  @DisplayName("TC10: Illegal scientific format throws ParseException for '1e' literal")
  void test_TC10() {
    // "1e" => parseDecimalNumberLiteral detects scientificNotation && lastChar 'e' => Illegal scientific format
    ExpressionConfiguration cfg = defaultConfig();
    Tokenizer tokenizer = new Tokenizer("1e", cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Illegal scientific format", ex.getMessage(), "Expected illegal scientific format message");
  }
}