package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.Tokenizer;

public class Tokenizer_parse_1_Test {

    // A minimal stub configuration to control parser behavior
    static class StubConfig implements ExpressionConfiguration {
        private final boolean arraysAllowed;
        private final boolean structuresAllowed;
        private final boolean singleQuotesAllowed;
        private final OperatorDictionaryIfc opDict;
        private final FunctionDictionaryIfc funcDict;

        StubConfig(boolean arraysAllowed, boolean structuresAllowed, boolean singleQuotesAllowed) {
            this.arraysAllowed = arraysAllowed;
            this.structuresAllowed = structuresAllowed;
            this.singleQuotesAllowed = singleQuotesAllowed;
            this.opDict = new OperatorDictionaryIfc() {
                @Override public boolean hasPrefixOperator(String s) { return false; }
                @Override public OperatorIfc getPrefixOperator(String s) { return null; }
                @Override public boolean hasPostfixOperator(String s) { return false; }
                @Override public OperatorIfc getPostfixOperator(String s) { return null; }
                @Override public boolean hasInfixOperator(String s) { return false; }
                @Override public OperatorIfc getInfixOperator(String s) { return null; }
                @Override public void addOperator(String s, OperatorIfc op) {} // Added missing method
            };
            this.funcDict = new FunctionDictionaryIfc() {
                @Override public boolean hasFunction(String name) { return false; }
                @Override public FunctionIfc getFunction(String name) { return null; }
                @Override public void addFunction(String name, FunctionIfc func) {} // Added missing method
            };
        }
        @Override public OperatorDictionaryIfc getOperatorDictionary() { return opDict; }
        @Override public FunctionDictionaryIfc getFunctionDictionary() { return funcDict; }
        @Override public boolean isImplicitMultiplicationAllowed() { return false; }
        @Override public boolean isArraysAllowed() { return arraysAllowed; }
        @Override public boolean isStructuresAllowed() { return structuresAllowed; }
        @Override public boolean isSingleQuoteStringLiteralsAllowed() { return singleQuotesAllowed; }
    }

    @Test
    @DisplayName("TC06: Unclosed array throws Closing array not found when expression ends with '['")
    void test_TC06() {
        String expr = "[1";
        ExpressionConfiguration config = new StubConfig(true, true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertEquals("Closing array not found", ex.getMessage());
    }

    @Test
    @DisplayName("TC07: Unexpected closing brace at start throws Unexpected closing brace")
    void test_TC07() {
        String expr = ")";
        ExpressionConfiguration config = new StubConfig(true, true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertEquals("Unexpected closing brace", ex.getMessage());
    }

    @Test
    @DisplayName("TC08: Unexpected closing array at start throws Unexpected closing array")
    void test_TC08() {
        String expr = "]";
        ExpressionConfiguration config = new StubConfig(true, true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertEquals("Unexpected closing array", ex.getMessage());
    }

    @Test
    @DisplayName("TC09: Misplaced structure operator at start throws Misplaced structure operator")
    void test_TC09() {
        String expr = ".";
        ExpressionConfiguration config = new StubConfig(true, true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertEquals("Misplaced structure operator", ex.getMessage());
    }

    @Test
    @DisplayName("TC10: Undefined operator for '.' when structures disallowed throws Undefined operator")
    void test_TC10() {
        String expr = ".";
        ExpressionConfiguration config = new StubConfig(true, false, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertTrue(ex.getMessage().contains("Undefined operator '.'"));
    }

    @Test
    @DisplayName("TC11: Hexadecimal number literal '0x1F' yields one NUMBER_LITERAL token")
    void test_TC11() throws ParseException {
        String expr = "0x1F";
        ExpressionConfiguration config = new StubConfig(true, true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        List<Token> tokens = tokenizer.parse();
        assertEquals(1, tokens.size());
        Token t = tokens.get(0);
        assertEquals(Token.TokenType.NUMBER_LITERAL, t.getType());
        assertEquals("0x1F", t.getToken());
    }

    @Test
    @DisplayName("TC12: Decimal with two dots throws Number contains more than one decimal point")
    void test_TC12() {
        String expr = "1.2.3";
        ExpressionConfiguration config = new StubConfig(true, true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertEquals("Number contains more than one decimal point", ex.getMessage());
    }

    @Test
    @DisplayName("TC13: Illegal scientific format in '1e+' throws Illegal scientific format")
    void test_TC13() {
        String expr = "1e+";
        ExpressionConfiguration config = new StubConfig(true, true, true);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertEquals("Illegal scientific format", ex.getMessage());
    }
}