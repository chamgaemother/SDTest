package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.parser.Token.TokenType;
import com.ezylang.evalex.parser.ParseException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class Tokenizer_parse_2_Test {

  @Test
  @DisplayName("TC12: Valid variable structure access a.b produces VARIABLE, STRUCTURE_SEPARATOR, VARIABLE when structures allowed")
  public void test_TC12() throws Exception {
    // Structure separator path B0->B1->...->B11, structuresAllowed=true enables parseStructureSeparator branch
    ExpressionConfiguration config = new ExpressionConfiguration(true, false);
    Tokenizer tokenizer = new Tokenizer("a.b", config);

    List<Token> tokens = tokenizer.parse();

    assertEquals(3, tokens.size(), "Expect three tokens for a.b");
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(0).getType(), "First token should be variable");
    assertEquals(TokenType.STRUCTURE_SEPARATOR, tokens.get(1).getType(), "Second token should be structure separator");
    assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(2).getType(), "Third token should be variable");
  }

  @Test
  @DisplayName("TC13: Unexpected closing array ']' at start throws \"Unexpected closing array\" when arraysAllowed")
  public void test_TC13() {
    // Directly parse ']', parseArrayClose sees arrayBalance<0 triggers Unexpected closing array in B7->B9->B10
    ExpressionConfiguration config = new ExpressionConfiguration(false, true);
    Tokenizer tokenizer = new Tokenizer("]", config);

    ParseException ex = assertThrows(ParseException.class, tokenizer::parse,
        "Parsing ']' should throw Unexpected closing array");
    assertTrue(ex.getMessage().contains("Unexpected closing array"));
  }

  @Test
  @DisplayName("TC14: Hexadecimal literal 0x1A parsed as single NUMBER_LITERAL token")
  public void test_TC14() throws Exception {
    // Hex literal path: parseNumberLiteral->parseHexNumberLiteral, B0->B1->B6->...->B11
    ExpressionConfiguration config = new ExpressionConfiguration(false, false);
    Tokenizer tokenizer = new Tokenizer("0x1A", config);

    List<Token> tokens = tokenizer.parse();

    assertEquals(1, tokens.size(), "Expect one hex literal token");
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType(), "Token type should be NUMBER_LITERAL");
    assertEquals("0x1A", tokens.get(0).getTokenValue(), "Token value should be '0x1A'");
  }

  @Test
  @DisplayName("TC15: Undefined function foo(1) throws \"Undefined function 'foo'\" when function not in dictionary")
  public void test_TC15() {
    // parseIdentifier sees function call start 'foo(', functionDictionary.hasFunction false triggers exception
    ExpressionConfiguration config = new ExpressionConfiguration(false, false);
    Tokenizer tokenizer = new Tokenizer("foo(1)", config);

    ParseException ex = assertThrows(ParseException.class, tokenizer::parse,
        "Undefined function foo should throw exception");
    assertTrue(ex.getMessage().contains("Undefined function 'foo'"));
  }

  @Test
  @DisplayName("TC16: Unknown escape in single-quoted string '\\q' throws \"Unknown escape character\"")
  public void test_TC16() {
    // parseStringLiteral with singleQuote allowed, escapeCharacter sees '\\q' none of known cases, throws
    ExpressionConfiguration config = new ExpressionConfiguration(false, false);
    Tokenizer tokenizer = new Tokenizer("'\\q'", config);

    ParseException ex = assertThrows(ParseException.class, tokenizer::parse,
        "Unknown escape character should cause exception");
    assertTrue(ex.getMessage().contains("Unknown escape character"));
  }

  @Test
  @DisplayName("TC17: Balanced array [1] returns ARRAY_OPEN, NUMBER_LITERAL, ARRAY_CLOSE when arraysAllowed")
  public void test_TC17() throws Exception {
    // parseArrayOpen increments arrayBalance, parseArrayClose decrements, B0->B1->B11
    ExpressionConfiguration config = new ExpressionConfiguration(false, true);
    Tokenizer tokenizer = new Tokenizer("[1]", config);

    List<Token> tokens = tokenizer.parse();

    assertEquals(3, tokens.size(), "Expect three tokens for [1]");
    assertEquals(TokenType.ARRAY_OPEN, tokens.get(0).getType(), "First token should be ARRAY_OPEN");
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(1).getType(), "Second token should be NUMBER_LITERAL");
    assertEquals(TokenType.ARRAY_CLOSE, tokens.get(2).getType(), "Third token should be ARRAY_CLOSE");
  }

  @Test
  @DisplayName("TC18: Implicit multiplication between NUMBER_LITERAL and BRACE_OPEN in \"1(2)\" inserts '*' when allowed")
  public void test_TC18() throws Exception {
    // After parsing '1', implicitMultiplicationPossible returns true (NUMBER then BRACE_OPEN), B2->B3->B4
    ExpressionConfiguration config = new ExpressionConfiguration(true, false);
    Tokenizer tokenizer = new Tokenizer("1(2)", config);

    List<Token> tokens = tokenizer.parse();

    // Expect tokens: NUMBER_LITERAL, '*' infix operator, BRACE_OPEN, NUMBER_LITERAL, BRACE_CLOSE
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(1).getType(), "Second token should be auto-inserted '*'\");
    assertEquals("*", tokens.get(1).getTokenValue(), "Inserted operator should be '*'\");
  }

  @Test
  @DisplayName("TC19: Standalone closing brace ')' at start throws \"Unexpected closing brace\"")
  public void test_TC19() {
    // parseBraceClose sees braceBalance<0 triggers Unexpected closing brace at B7->B8
    ExpressionConfiguration config = new ExpressionConfiguration(false, false);
    Tokenizer tokenizer = new Tokenizer(")", config);

    ParseException ex = assertThrows(ParseException.class, tokenizer::parse,
        "Unexpected closing brace should cause exception");
    assertTrue(ex.getMessage().contains("Unexpected closing brace"));
  }

  @Test
  @DisplayName("TC20: Undefined single-character operator '@' throws \"Undefined operator '@'\"")
  public void test_TC20() {
    // parseOperator reads '@', no operator matches, throws undefined operator exception
    ExpressionConfiguration config = new ExpressionConfiguration(false, false);
    Tokenizer tokenizer = new Tokenizer("@", config);

    ParseException ex = assertThrows(ParseException.class, tokenizer::parse,
        "Undefined operator '@' should cause exception");
    assertTrue(ex.getMessage().contains("Undefined operator '@'"));
  }
}