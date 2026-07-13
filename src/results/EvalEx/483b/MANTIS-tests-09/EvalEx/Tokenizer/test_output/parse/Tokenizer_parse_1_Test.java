package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import com.ezylang.evalex.parser.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

public class Tokenizer_parse_1_Test {

    // A minimal stub config that allows customizing behavior for tests
    private static class StubConfig implements ExpressionConfiguration {
        private boolean implicitMulAllowed = false;
        private boolean structuresAllowed = false;
        private boolean arraysAllowed = false;
        private boolean singleQuoteAllowed = false;
        private OperatorDictionaryIfc opDict = new OperatorDictionaryIfc() {
            @Override
            public boolean hasPrefixOperator(String op) { return false; }
            @Override
            public OperatorIfc getPrefixOperator(String op) { return new OperatorIfc() { public EvaluationValue evaluate(Expression expr, Token token, EvaluationValue... values) { return null; } }; }
            @Override
            public boolean hasPostfixOperator(String op) { return false; }
            @Override
            public OperatorIfc getPostfixOperator(String op) { return new OperatorIfc() { public EvaluationValue evaluate(Expression expr, Token token, EvaluationValue... values) { return null; } }; }
            @Override
            public boolean hasInfixOperator(String op) { return false; }
            @Override
            public OperatorIfc getInfixOperator(String op) { return new OperatorIfc() { public EvaluationValue evaluate(Expression expr, Token token, EvaluationValue... values) { return null; } }; }
            @Override
            public void addOperator(String op, OperatorIfc operator) {} // Implemented method
        };
        private FunctionDictionaryIfc fnDict = new FunctionDictionaryIfc() {
            @Override public boolean hasFunction(String name) { return false; }
            @Override public FunctionIfc getFunction(String name) { return null; }
            @Override public void addFunction(String name, FunctionIfc function) {} // Implemented method
        };

        StubConfig allowImplicitMul(boolean b) { this.implicitMulAllowed = b; return this; }
        StubConfig allowStructures(boolean b) { this.structuresAllowed = b; return this; }
        StubConfig allowArrays(boolean b) { this.arraysAllowed = b; return this; }
        StubConfig allowSingleQuotes(boolean b) { this.singleQuoteAllowed = b; return this; }
        StubConfig withPrefix(String op) {
            this.opDict = new OperatorDictionaryIfc() {
                @Override public boolean hasPrefixOperator(String o) { return o.equals(op); }
                @Override public OperatorIfc getPrefixOperator(String o) { return new OperatorIfc() { public EvaluationValue evaluate(Expression expr, Token token, EvaluationValue... values) { return null; } }; }
                @Override public boolean hasPostfixOperator(String o) { return false; }
                @Override public OperatorIfc getPostfixOperator(String o) { return new OperatorIfc() { public EvaluationValue evaluate(Expression expr, Token token, EvaluationValue... values) { return null; } }; }
                @Override public boolean hasInfixOperator(String o) { return false; }
                @Override public OperatorIfc getInfixOperator(String o) { return new OperatorIfc() { public EvaluationValue evaluate(Expression expr, Token token, EvaluationValue... values) { return null; } }; }
                @Override public void addOperator(String op, OperatorIfc operator) {} // Implemented method
            };
            return this;
        }
        StubConfig withInfix(String op) {
            this.opDict = new OperatorDictionaryIfc() {
                @Override public boolean hasPrefixOperator(String o) { return false; }
                @Override public OperatorIfc getPrefixOperator(String o) { return new OperatorIfc() { public EvaluationValue evaluate(Expression expr, Token token, EvaluationValue... values) { return null; } }; }
                @Override public boolean hasPostfixOperator(String o) { return false; }
                @Override public OperatorIfc getPostfixOperator(String o) { return new OperatorIfc() { public EvaluationValue evaluate(Expression expr, Token token, EvaluationValue... values) { return null; } }; }
                @Override public boolean hasInfixOperator(String o) { return o.equals(op); }
                @Override public OperatorIfc getInfixOperator(String o) { return new OperatorIfc() { public EvaluationValue evaluate(Expression expr, Token token, EvaluationValue... values) { return null; } }; }
                @Override public void addOperator(String op, OperatorIfc operator) {} // Implemented method
            };
            return this;
        }
        @Override public OperatorDictionaryIfc getOperatorDictionary() { return opDict; }
        @Override public FunctionDictionaryIfc getFunctionDictionary() { return fnDict; }
        @Override public boolean isImplicitMultiplicationAllowed() { return implicitMulAllowed; }
        @Override public boolean isArraysAllowed() { return arraysAllowed; }
        @Override public boolean isStructuresAllowed() { return structuresAllowed; }
        @Override public boolean isSingleQuoteStringLiteralsAllowed() { return singleQuoteAllowed; }
    }

    @Test
    @DisplayName("Implicit '*' inserted between closing and opening brace when implicit multiplication allowed")
    void test_TC13() throws ParseException {
        // input "(1)(2)" triggers implicitMultiplicationPossible between ')' and '('
        ExpressionConfiguration config = new StubConfig()
            .allowImplicitMul(true)
            .withInfix("*");
        Tokenizer tokenizer = new Tokenizer("(1)(2)", config);
        List<Token> tokens = tokenizer.parse();
        // 0='(',1='1',2=')',3='*',4='(',5='2',6=')'
        assertEquals(7, tokens.size());
        assertEquals("*", tokens.get(3).getTokenString());
    }

    @Test
    @DisplayName("Missing operator exception when implicit multiplication between braces not allowed")
    void test_TC14() {
        // same "(1)(2)" but implicit mul disabled => exception at first possible insertion
        ExpressionConfiguration config = new StubConfig().allowImplicitMul(false).withInfix("*");
        Tokenizer tokenizer = new Tokenizer("(1)(2)", config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertTrue(ex.getMessage().contains("Missing operator"));
    }

    @Test
    @DisplayName("Valid structure separator between variable and variable when structures allowed")
    void test_TC15() throws ParseException {
        // input "a.b" -> 'a', '.', 'b'; structure separator allowed
        ExpressionConfiguration config = new StubConfig().allowStructures(true);
        Tokenizer tokenizer = new Tokenizer("a.b", config);
        List<Token> tokens = tokenizer.parse();
        assertEquals(3, tokens.size());
        assertEquals(Token.TokenType.STRUCTURE_SEPARATOR, tokens.get(1).getType());
    }

    @Test
    @DisplayName("Hexadecimal number literal parsed correctly")
    void test_TC16() throws ParseException {
        // input "0x1A" triggers parseHexNumberLiteral path
        ExpressionConfiguration config = new StubConfig().withInfix("+"); // infix irrelevant
        Tokenizer tokenizer = new Tokenizer("0x1A", config);
        List<Token> tokens = tokenizer.parse();
        assertEquals(1, tokens.size());
        assertEquals("0x1A", tokens.get(0).getTokenString());
        assertEquals(Token.TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    }

    @Test
    @DisplayName("Unknown escape character in string literal throws exception")
    void test_TC17() {
        // input "'\\z'" => parseStringLiteral then escapeCharacter with 'z' triggers exception
        ExpressionConfiguration config = new StubConfig().allowSingleQuotes(true);
        Tokenizer tokenizer = new Tokenizer("'\\z'", config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertTrue(ex.getMessage().contains("Unknown escape character"));
    }

    @Test
    @DisplayName("Prefix operator recognized before number when allowed")
    void test_TC18() throws ParseException {
        // input "-5" at start: previousToken null -> prefixOperatorAllowed true -> parseIdentifier-or-operator picks prefix
        ExpressionConfiguration config = new StubConfig().withPrefix("-");
        Tokenizer tokenizer = new Tokenizer("-5", config);
        List<Token> tokens = tokenizer.parse();
        assertEquals(2, tokens.size());
        assertEquals(Token.TokenType.PREFIX_OPERATOR, tokens.get(0).getType());
        assertEquals("5", tokens.get(1).getTokenString());
    }
}