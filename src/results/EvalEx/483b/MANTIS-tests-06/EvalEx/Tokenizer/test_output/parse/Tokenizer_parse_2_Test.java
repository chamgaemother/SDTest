package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
public class Tokenizer_parse_2_Test {

  @Test
  @DisplayName("TC18: Valid structure separator between two identifiers when structuresAllowed=true")
  void test_TC18() throws Exception {
    // Setup config: structuresAllowed=true, arraysAllowed=false, implicitMultiplicationAllowed=false
    ExpressionConfiguration cfg = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(cfg.isStructuresAllowed()).thenReturn(true);
    when(cfg.isArraysAllowed()).thenReturn(false);
    when(cfg.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(cfg.getOperatorDictionary()).thenReturn(opDict);
    when(cfg.getFunctionDictionary()).thenReturn(funcDict);
    // operator dict returns no operators for identifiers
    when(opDict.hasPrefixOperator(anyString())).thenReturn(false);
    when(opDict.hasPostfixOperator(anyString())).thenReturn(false);
    when(opDict.hasInfixOperator(anyString())).thenReturn(false);

    String expr = "x.y";
    Tokenizer tokenizer = new Tokenizer(expr, cfg);
    List<com.ezylang.evalex.parser.Token> tokens = tokenizer.parse(); // Fixed import for Token
    // Expect three tokens: x, ., y
    assertEquals(3, tokens.size());
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.VARIABLE_OR_CONSTANT, tokens.get(0).getType());
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.STRUCTURE_SEPARATOR, tokens.get(1).getType());
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.VARIABLE_OR_CONSTANT, tokens.get(2).getType());
  }

  @Test
  @DisplayName("TC19: Implicit multiplication inserted between consecutive parenthesis when allowed")
  void test_TC19() throws Exception {
    // Setup config: implicitMultiplicationAllowed=true, arraysAllowed=false, structuresAllowed=false
    ExpressionConfiguration cfg = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(cfg.isImplicitMultiplicationAllowed()).thenReturn(true);
    when(cfg.isArraysAllowed()).thenReturn(false);
    when(cfg.isStructuresAllowed()).thenReturn(false);
    when(cfg.getOperatorDictionary()).thenReturn(opDict);
    when(cfg.getFunctionDictionary()).thenReturn(funcDict);
    // stub operator lookup for "*"
    when(opDict.hasInfixOperator("*")).thenReturn(true);
    OperatorIfc mulOp = mock(OperatorIfc.class);
    when(opDict.getInfixOperator("*")).thenReturn(mulOp);
    // no other operators
    when(opDict.hasPrefixOperator(anyString())).thenReturn(false);
    when(opDict.hasPostfixOperator(anyString())).thenReturn(false);

    String expr = "(1)(2)";
    Tokenizer tokenizer = new Tokenizer(expr, cfg);
    List<com.ezylang.evalex.parser.Token> tokens = tokenizer.parse(); // Fixed import for Token
    // Tokens: '(', '1', ')', '*', '(', '2', ')'
    assertEquals(7, tokens.size());
    com.ezylang.evalex.parser.Token implicitMul = tokens.get(3); // Fixed import for Token
    assertEquals("*", implicitMul.getText());
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.INFIX_OPERATOR, implicitMul.getType());
  }

  @Test
  @DisplayName("TC20: Hex number literal '0xFa' recognized as NUMBER_LITERAL")
  void test_TC20() throws Exception {
    // Default config: all false
    ExpressionConfiguration cfg = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(cfg.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(cfg.isArraysAllowed()).thenReturn(false);
    when(cfg.isStructuresAllowed()).thenReturn(false);
    when(cfg.getOperatorDictionary()).thenReturn(opDict);
    when(cfg.getFunctionDictionary()).thenReturn(funcDict);
    when(opDict.hasPrefixOperator(anyString())).thenReturn(false);
    when(opDict.hasPostfixOperator(anyString())).thenReturn(false);
    when(opDict.hasInfixOperator(anyString())).thenReturn(false);

    String expr = "0xFa";
    Tokenizer tokenizer = new Tokenizer(expr, cfg);
    List<com.ezylang.evalex.parser.Token> tokens = tokenizer.parse(); // Fixed import for Token
    // Single hex literal should produce one NUMBER_LITERAL token
    assertEquals(1, tokens.size());
    com.ezylang.evalex.parser.Token t = tokens.get(0); // Fixed import for Token
    assertEquals("0xFa", t.getText());
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.NUMBER_LITERAL, t.getType());
  }

  @Test
  @DisplayName("TC21: Valid string literal with escaped characters parsed into STRING_LITERAL")
  void test_TC21() throws Exception {
    // Config with singleQuoteStringLiteralsAllowed=false => only double quotes start string
    ExpressionConfiguration cfg = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(cfg.isSingleQuoteStringLiteralsAllowed()).thenReturn(false);
    when(cfg.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(cfg.isArraysAllowed()).thenReturn(false);
    when(cfg.isStructuresAllowed()).thenReturn(false);
    when(cfg.getOperatorDictionary()).thenReturn(opDict);
    when(cfg.getFunctionDictionary()).thenReturn(funcDict);
    when(opDict.hasPrefixOperator(anyString())).thenReturn(false);
    when(opDict.hasPostfixOperator(anyString())).thenReturn(false);
    when(opDict.hasInfixOperator(anyString())).thenReturn(false);

    // expression: "\"hello\\world\"" -> content hello\world
    String expr = "\"hello\\world\"";
    Tokenizer tokenizer = new Tokenizer(expr, cfg);
    List<com.ezylang.evalex.parser.Token> tokens = tokenizer.parse(); // Fixed import for Token
    assertEquals(1, tokens.size());
    com.ezylang.evalex.parser.Token t = tokens.get(0); // Fixed import for Token
    assertEquals(com.ezylang.evalex.parser.Token.TokenType.STRING_LITERAL, t.getType());
    assertEquals("hello\\world", t.getText());
  }

  @Test
  @DisplayName("TC22: Unclosed string literal throws ParseException 'Closing quote not found'")
  void test_TC22() {
    ExpressionConfiguration cfg = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(cfg.isSingleQuoteStringLiteralsAllowed()).thenReturn(true);
    when(cfg.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(cfg.isArraysAllowed()).thenReturn(false);
    when(cfg.isStructuresAllowed()).thenReturn(false);
    when(cfg.getOperatorDictionary()).thenReturn(opDict);
    when(cfg.getFunctionDictionary()).thenReturn(funcDict);

    String expr = "\"abc"; // no closing quote
    Tokenizer tokenizer = new Tokenizer(expr, cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertTrue(ex.getMessage().contains("Closing quote not found"));
  }

  @Test
  @DisplayName("TC23: Unknown escape character throws ParseException in escapeCharacter")
  void test_TC23() {
    ExpressionConfiguration cfg = mock(ExpressionConfiguration.class);
    OperatorDictionaryIfc opDict = mock(OperatorDictionaryIfc.class);
    FunctionDictionaryIfc funcDict = mock(FunctionDictionaryIfc.class);
    when(cfg.isSingleQuoteStringLiteralsAllowed()).thenReturn(true);
    when(cfg.isImplicitMultiplicationAllowed()).thenReturn(false);
    when(cfg.isArraysAllowed()).thenReturn(false);
    when(cfg.isStructuresAllowed()).thenReturn(false);
    when(cfg.getOperatorDictionary()).thenReturn(opDict);
    when(cfg.getFunctionDictionary()).thenReturn(funcDict);

    String expr = "\"foo\\x\""; // \\x unknown escape
    Tokenizer tokenizer = new Tokenizer(expr, cfg);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertTrue(ex.getMessage().contains("Unknown escape character"));
  }
}