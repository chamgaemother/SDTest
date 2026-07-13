package com.ezylang.evalex.parser;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.parser.Token.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class Tokenizer_parse_1_Test {

    @Test
    @DisplayName("Implicit multiplication inserted between ')' and '(' when allowed")
    void test_TC12() throws Exception {
        // Input ")(" causes implicitMultiplicationPossible true (previous BRACE_CLOSE, current BRACE_OPEN)
        String expr = ")(";
        ExpressionConfiguration config = new ExpressionConfiguration(true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);

        List<Token> tokens = tokenizer.parse();
        // Expect three tokens: ')' then '*' then '('
        assertEquals(3, tokens.size());
        assertEquals(TokenType.BRACE_CLOSE, tokens.get(0).getType());
        assertEquals(TokenType.INFIX_OPERATOR, tokens.get(1).getType());
        assertEquals("*", tokens.get(1).getValue());
        assertEquals(TokenType.BRACE_OPEN, tokens.get(2).getType());
    }

    @Test
    @DisplayName("Array open '[' throws when arrays not allowed")
    void test_TC13() {
        // '[' at start, arraysAllowed=false so intended behavior is to disallow array open
        String expr = "[";
        ExpressionConfiguration config = new ExpressionConfiguration(false, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);

        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertTrue(ex.getMessage().contains("Array open not allowed here"));
    }

    @Test
    @DisplayName("Array close ']' at start throws when arrays allowed")
    void test_TC14() {
        // ']' at start, arraysAllowed=true but no opening => arrayCloseAllowed false
        String expr = "]";
        ExpressionConfiguration config = new ExpressionConfiguration(true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);

        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertTrue(ex.getMessage().contains("Array close not allowed here"));
    }

    @Test
    @DisplayName("Proper single-element array '[1]' parsed with open, literal, close")
    void test_TC15() throws Exception {
        // '[1]' triggers parseArrayOpen, parseDecimalNumberLiteral, parseArrayClose
        String expr = "[1]";
        ExpressionConfiguration config = new ExpressionConfiguration(true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);

        List<Token> tokens = tokenizer.parse();
        assertEquals(3, tokens.size());
        assertEquals(TokenType.ARRAY_OPEN, tokens.get(0).getType());
        assertEquals(TokenType.NUMBER_LITERAL, tokens.get(1).getType());
        assertEquals("1", tokens.get(1).getValue());
        assertEquals(TokenType.ARRAY_CLOSE, tokens.get(2).getType());
    }

    @Test
    @DisplayName("Valid structure separator '.' between variables when allowed")
    void test_TC16() throws Exception {
        // "a.b": first variable, then '.', then variable
        String expr = "a.b";
        ExpressionConfiguration config = new ExpressionConfiguration(true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);

        List<Token> tokens = tokenizer.parse();
        assertEquals(3, tokens.size());
        assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(0).getType());
        assertEquals(TokenType.STRUCTURE_SEPARATOR, tokens.get(1).getType());
        assertEquals(TokenType.VARIABLE_OR_CONSTANT, tokens.get(2).getType());
    }

    @Test
    @DisplayName("Undefined operator '$' throws undefined operator exception")
    void test_TC17() {
        // "$" not recognized by operatorDictionary -> undefined operator
        String expr = "$";
        ExpressionConfiguration config = new ExpressionConfiguration(true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);

        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertTrue(ex.getMessage().contains("Undefined operator '$'"));
    }

    @Test
    @DisplayName("Undefined function call 'foo()' throws undefined function exception")
    void test_TC18() {
        // "foo()": identifier then '(', functionDictionary.hasFunction=false -> undefined
        String expr = "foo()";
        ExpressionConfiguration config = new ExpressionConfiguration(true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);

        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertTrue(ex.getMessage().contains("Undefined function 'foo'"));
    }

    @Test
    @DisplayName("User-defined function 'bar()' parsed as FUNCTION token")
    void test_TC19() throws Exception {
        // "bar()": we add function "bar" so parseIdentifier returns FUNCTION
        String expr = "bar()";
        ExpressionConfiguration config = new ExpressionConfiguration(true, true);
        // register a dummy bar function
        config.getFunctionDictionary().addFunction("bar", (FunctionIfc) args -> null);
        Tokenizer tokenizer = new Tokenizer(expr, config);

        List<Token> tokens = tokenizer.parse();
        assertEquals(1, tokens.size());
        assertEquals(TokenType.FUNCTION, tokens.get(0).getType());
        assertEquals("bar", tokens.get(0).getValue());
    }

    @Test
    @DisplayName("String literal with escape sequences parsed correctly")
    void test_TC20() throws Exception {
        // "'a\\nb'": single quotes allowed and escape \n gives newline
        String expr = "'a\\nb'";
        ExpressionConfiguration config = new ExpressionConfiguration(true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);

        List<Token> tokens = tokenizer.parse();
        assertEquals(1, tokens.size());
        assertEquals(TokenType.STRING_LITERAL, tokens.get(0).getType());
        assertEquals("a\nb", tokens.get(0).getValue());
    }

    @Test
    @DisplayName("String literal missing closing quote throws exception")
    void test_TC21() {
        // "'abc": no closing quote -> Closing quote not found
        String expr = "'abc";
        ExpressionConfiguration config = new ExpressionConfiguration(true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);

        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertTrue(ex.getMessage().contains("Closing quote not found"));
    }

    @Test
    @DisplayName("Unknown escape character in string literal throws exception")
    void test_TC22() {
        // "'\\z'": escape \z unsupported -> Unknown escape character
        String expr = "'\\z'";
        ExpressionConfiguration config = new ExpressionConfiguration(true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);

        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertTrue(ex.getMessage().contains("Unknown escape character"));
    }

    @Test
    @DisplayName("Decimal number with multiple dots throws exception")
    void test_TC23() {
        // "1.2.3": second dot triggers number contains more than one decimal point
        String expr = "1.2.3";
        ExpressionConfiguration config = new ExpressionConfiguration(true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);

        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertTrue(ex.getMessage().contains("Number contains more than one decimal point"));
    }
}