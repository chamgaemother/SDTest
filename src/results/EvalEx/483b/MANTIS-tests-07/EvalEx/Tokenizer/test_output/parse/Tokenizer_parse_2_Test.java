package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.parser.Token.TokenType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class Tokenizer_parse_2_Test {

  @Test
  @DisplayName("TC24: Prefix operator '-' before number yields PREFIX_OPERATOR then NUMBER_LITERAL")
  void test_TC24() throws Exception {
    String expr = "-1";
    ExpressionConfiguration config = new ExpressionConfiguration(false, false, false); // Adjust constructor parameters as required
    Tokenizer tokenizer = new Tokenizer(expr, config);

    List<Token> tokens = tokenizer.parse();

    assertEquals(2, tokens.size(), "Should produce exactly two tokens");
    assertEquals(TokenType.PREFIX_OPERATOR, tokens.get(0).getType(), "First token must be PREFIX_OPERATOR");
    assertEquals("-", tokens.get(0).getValue(), "First token value must be '-'\");
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(1).getType(), "Second token must be NUMBER_LITERAL");
    assertEquals("1", tokens.get(1).getValue(), "Second token value must be '1'");
  }

  @Test
  @DisplayName("TC25: Valid scientific notation '1e3' parsed as single NUMBER_LITERAL")
  void test_TC25() throws Exception {
    String expr = "1e3";
    ExpressionConfiguration config = new ExpressionConfiguration(false, false, false); // Adjust constructor parameters as required
    Tokenizer tokenizer = new Tokenizer(expr, config);

    List<Token> tokens = tokenizer.parse();

    assertEquals(1, tokens.size(), "Should produce exactly one token");
    Token t = tokens.get(0);
    assertEquals(TokenType.NUMBER_LITERAL, t.getType(), "Token must be NUMBER_LITERAL");
    assertEquals("1e3", t.getValue(), "Token value must be '1e3'");
  }

  @Test
  @DisplayName("TC26: Unmatched opening array throws 'Closing array not found' when arraysAllowed=true")
  void test_TC26() {
    String expr = "[";
    ExpressionConfiguration config = new ExpressionConfiguration(true, false, false); // Adjust constructor parameters as required
    Tokenizer tokenizer = new Tokenizer(expr, config);

    ParseException ex = assertThrows(ParseException.class, tokenizer::parse,
        "Expected ParseException for unmatched opening array");
    assertEquals("Closing array not found", ex.getMessage(), "Exception message must indicate missing closing array");
  }

  @Test
  @DisplayName("TC27: Number literal starting with dot '.5' parsed correctly")
  void test_TC27() throws Exception {
    String expr = ".5";
    ExpressionConfiguration config = new ExpressionConfiguration(false, false, false); // Adjust constructor parameters as required
    Tokenizer tokenizer = new Tokenizer(expr, config);

    List<Token> tokens = tokenizer.parse();

    assertEquals(1, tokens.size(), "Should produce exactly one token");
    Token t = tokens.get(0);
    assertEquals(TokenType.NUMBER_LITERAL, t.getType(), "Token must be NUMBER_LITERAL");
    assertEquals(".5", t.getValue(), "Token value must be '.5'");
  }

  @Test
  @DisplayName("TC28: Implicit multiplication between number and '(' when allowed inserts '*'\")
  void test_TC28() throws Exception {
    String expr = "2(3)";
    ExpressionConfiguration config = new ExpressionConfiguration(false, true, false); // Adjust constructor parameters as required
    Tokenizer tokenizer = new Tokenizer(expr, config);

    List<Token> tokens = tokenizer.parse();

    assertEquals(3, tokens.size(), "Should produce three tokens: '2', '*', '('");
    assertEquals("2", tokens.get(0).getValue(), "First token value must be '2'");
    assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType(), "First token must be NUMBER_LITERAL");
    assertEquals("*", tokens.get(1).getValue(), "Second token value must be '*'");
    assertEquals(TokenType.INFIX_OPERATOR, tokens.get(1).getType(), "Second token must be INFIX_OPERATOR");
    assertEquals("(", tokens.get(2).getValue(), "Third token value must be '('");
    assertEquals(TokenType.BRACE_OPEN, tokens.get(2).getType(), "Third token must be BRACE_OPEN");
  }
}