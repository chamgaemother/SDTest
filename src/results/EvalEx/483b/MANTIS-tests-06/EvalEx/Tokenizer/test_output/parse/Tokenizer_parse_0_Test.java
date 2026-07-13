package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.TokenType; // Corrected import
import com.ezylang.evalex.parser.Tokenizer;

/**
 * JUnit 5 tests for Tokenizer.parse() covering scenarios TC01–TC10.
 */
public class Tokenizer_parse_0_Test {

    /**
     * Helper to produce a default configuration:
     * no implicit multiplication, no arrays, no structures.
     */
    private ExpressionConfiguration defaultConfig() {
        return ExpressionConfiguration
                .builder()
                .implicitMultiplicationAllowed(false)
                .arraysAllowed(false)
                .structuresAllowed(false)
                .build();
    }

    @Test
    @DisplayName("TC01: Empty expression returns empty token list (r14==null exit)")
    public void test_TC01() throws Exception {
        // GIVEN an empty expression string -> getNextToken immediately returns null => B1→B7→B9→B11
        Tokenizer tokenizer = new Tokenizer("", defaultConfig());
        // WHEN
        List<Token> tokens = tokenizer.parse();
        // THEN
        assertTrue(tokens.isEmpty(), "Expected no tokens for empty expression");
    }

    @Test
    @DisplayName("TC02: Single number literal parses one token (implicitMultiplicationPossible=false)")
    public void test_TC02() throws Exception {
        // GIVEN a single number literal -> parseNumberLiteral used, no implicit multiplication branch => B2 false
        Tokenizer tokenizer = new Tokenizer("42", defaultConfig());
        // WHEN
        List<Token> tokens = tokenizer.parse();
        // THEN
        assertEquals(1, tokens.size(), "Should have exactly one token");
        Token t = tokens.get(0);
        assertEquals(TokenType.NUMBER_LITERAL, t.getType(), "Token type should be NUMBER_LITERAL");
        assertEquals("42", t.getText(), "Token text should be '42'");
    }

    @Test
    @DisplayName("TC03: Paired parentheses produce BRACE_OPEN and BRACE_CLOSE tokens")
    public void test_TC03() throws Exception {
        // GIVEN an empty parentheses "()" -> parseBraceOpen then parseBraceClose => no implicit multiplication
        Tokenizer tokenizer = new Tokenizer("()", defaultConfig());
        // WHEN
        List<Token> tokens = tokenizer.parse();
        // THEN
        assertEquals(2, tokens.size(), "Should parse two tokens for parentheses");
        assertEquals(TokenType.BRACE_OPEN, tokens.get(0).getType(), "First token should be BRACE_OPEN");
        assertEquals(TokenType.BRACE_CLOSE, tokens.get(1).getType(), "Second token should be BRACE_CLOSE");
    }

    @Test
    @DisplayName("TC04: Implicit multiplication inserts '*' before '(' and throws on missing closing brace")
    public void test_TC04() {
        // GIVEN "2(" with implicitMultiplicationAllowed=true triggers implicitMultiplicationPossible true => B2->B4, then missing brace error at end
        ExpressionConfiguration cfg = ExpressionConfiguration
                .builder()
                .implicitMultiplicationAllowed(true)
                .arraysAllowed(false)
                .structuresAllowed(false)
                .build();
        Tokenizer tokenizer = new Tokenizer("2(", cfg);
        // WHEN / THEN
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse,
                "Expected ParseException due to missing closing brace");
        assertTrue(ex.getMessage().contains("Closing brace not found"),
                "Exception message should mention 'Closing brace not found'");
    }

    @Test
    @DisplayName("TC05: Implicit multiplication disallowed throws Missing operator")
    public void test_TC05() {
        // GIVEN "2(" with implicitMultiplicationAllowed=false triggers implicitMultiplicationPossible true => B2->B3->B5
        ExpressionConfiguration cfg = ExpressionConfiguration
                .builder()
                .implicitMultiplicationAllowed(false)
                .arraysAllowed(false)
                .structuresAllowed(false)
                .build();
        Tokenizer tokenizer = new Tokenizer("2(", cfg);
        // WHEN / THEN
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse,
                "Expected ParseException due to missing operator");
        assertEquals("Missing operator", ex.getMessage(), "Exception message must be exactly 'Missing operator'");
    }

    @Test
    @DisplayName("TC06: Unexpected token after infix operator when ')' follows '+'")
    public void test_TC06() {
        // GIVEN "1+)" where after parsing '+', validateToken sees INFIX_OPERATOR followed by BRACE_CLOSE => invalidTokenAfterInfixOperator
        Tokenizer tokenizer = new Tokenizer("1+)", defaultConfig());
        // WHEN / THEN
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse,
                "Expected ParseException for unexpected token after infix operator");
        assertEquals("Unexpected token after infix operator", ex.getMessage(),
                "Exception message must be exactly 'Unexpected token after infix operator'");
    }

    @Test
    @DisplayName("TC07: Misplaced structure operator at start throws in parseStructureSeparator")
    public void test_TC07() {
        // GIVEN ".a" and structuresAllowed=true -> first token is '.', parseStructureSeparator throws
        ExpressionConfiguration cfg = ExpressionConfiguration
                .builder()
                .implicitMultiplicationAllowed(false)
                .arraysAllowed(false)
                .structuresAllowed(true)
                .build();
        Tokenizer tokenizer = new Tokenizer(".a", cfg);
        // WHEN / THEN
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse,
                "Expected ParseException due to misplaced structure operator");
        assertEquals("Structure separator not allowed here", ex.getMessage(),
                "Exception message must be exactly 'Structure separator not allowed here'");
    }

    @Test
    @DisplayName("TC08: Unclosed brace alone throws Closing brace not found")
    public void test_TC08() {
        // GIVEN "(" only and default config -> parse one BRACE_OPEN then missing closing brace
        Tokenizer tokenizer = new Tokenizer("(", defaultConfig());
        // WHEN / THEN
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse,
                "Expected ParseException due to missing closing brace");
        assertTrue(ex.getMessage().contains("Closing brace not found"),
                "Exception message should contain 'Closing brace not found'");
    }

    @Test
    @DisplayName("TC09: Unclosed array throws Closing array not found")
    public void test_TC09() {
        // GIVEN "[1" with arraysAllowed=true -> parse ARRAY_OPEN, NUMBER_LITERAL then missing array close
        ExpressionConfiguration cfg = ExpressionConfiguration
                .builder()
                .implicitMultiplicationAllowed(false)
                .arraysAllowed(true)
                .structuresAllowed(false)
                .build();
        Tokenizer tokenizer = new Tokenizer("[1", cfg);
        // WHEN / THEN
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse,
                "Expected ParseException due to missing closing array");
        assertTrue(ex.getMessage().contains("Closing array not found"),
                "Exception message should contain 'Closing array not found'");
    }

    @Test
    @DisplayName("TC10: Array with one element parses ARRAY_OPEN, NUMBER_LITERAL, ARRAY_CLOSE")
    public void test_TC10() throws Exception {
        // GIVEN "[5]" with arraysAllowed=true -> parse ARRAY_OPEN, NUMBER_LITERAL, ARRAY_CLOSE, then balanced
        ExpressionConfiguration cfg = ExpressionConfiguration
                .builder()
                .implicitMultiplicationAllowed(false)
                .arraysAllowed(true)
                .structuresAllowed(false)
                .build();
        Tokenizer tokenizer = new Tokenizer("[5]", cfg);
        // WHEN
        List<Token> tokens = tokenizer.parse();
        // THEN
        assertEquals(3, tokens.size(), "Should parse three tokens for array with one element");
        assertEquals(TokenType.ARRAY_OPEN, tokens.get(0).getType(), "First token should be ARRAY_OPEN");
        assertEquals("5", tokens.get(1).getText(), "Second token should be the number literal '5'");
        assertEquals(TokenType.ARRAY_CLOSE, tokens.get(2).getType(), "Third token should be ARRAY_CLOSE");
    }
}