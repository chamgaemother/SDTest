package com.ezylang.evalex.parser;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import com.ezylang.evalex.parser.ParseException;
import com.ezylang.evalex.parser.Token;
import com.ezylang.evalex.parser.Tokenizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class Tokenizer_parse_1_Test {

    // Minimal stub for operator dictionary
    private static class StubOperatorDictionary implements OperatorDictionaryIfc {
        private final Set<String> infix = new HashSet<>();
        public StubOperatorDictionary(String... infixOps) {
            infix.addAll(Arrays.asList(infixOps));
        }
        @Override public boolean hasInfixOperator(String op) { return infix.contains(op); }
        @Override public OperatorIfc getInfixOperator(String op) { return new OperatorIfc() { 
            public Object apply(Object ctx, List<Object> args) { return null; }
            @Override public boolean evaluate(com.ezylang.evalex.Expression expression, Token token, com.ezylang.evalex.data.EvaluationValue... values) { return false; }
        }}; }
        @Override public boolean hasPrefixOperator(String op) { return false; }
        @Override public OperatorIfc getPrefixOperator(String op) { throw new UnsupportedOperationException(); }
        @Override public boolean hasPostfixOperator(String op) { return false; }
        @Override public OperatorIfc getPostfixOperator(String op) { throw new UnsupportedOperationException(); }
        @Override public void addOperator(String op, OperatorIfc operator) {} // Added method to comply with interface
    }

    // Minimal stub for function dictionary
    private static class StubFunctionDictionary implements FunctionDictionaryIfc {
        private final Set<String> funcs = new HashSet<>();
        public StubFunctionDictionary(String... names) { funcs.addAll(Arrays.asList(names)); }
        @Override public boolean hasFunction(String name) { return funcs.contains(name); }
        @Override public FunctionIfc getFunction(String name) { return new FunctionIfc() { 
            public Object apply(List<Object> args) { return null; }
            @Override public boolean hasVarArgs() { return false; }
        }}; }
        @Override public void addFunction(String name, FunctionIfc function) {} // Added method to comply with interface
    }

    // Default configuration stub
    private static class StubConfig implements ExpressionConfiguration {
        private final boolean implicitMult;
        private final boolean singleQuote;
        private final OperatorDictionaryIfc opDict;
        private final FunctionDictionaryIfc funcDict;

        public StubConfig(boolean implicitMult, boolean singleQuote,
                          OperatorDictionaryIfc opDict, FunctionDictionaryIfc funcDict) {
            this.implicitMult = implicitMult;
            this.singleQuote = singleQuote;
            this.opDict = opDict;
            this.funcDict = funcDict;
        }

        @Override public OperatorDictionaryIfc getOperatorDictionary() { return opDict; }
        @Override public FunctionDictionaryIfc getFunctionDictionary() { return funcDict; }
        @Override public boolean isImplicitMultiplicationAllowed() { return implicitMult; }
        @Override public boolean isSingleQuoteStringLiteralsAllowed() { return singleQuote; }
        @Override public boolean isArraysAllowed() { return false; }
        @Override public boolean isStructuresAllowed() { return false; }
        @Override public void addOperator(String op, OperatorIfc operator) {} // Added method to comply with interface
        @Override public void addFunction(String name, FunctionIfc function) {} // Added method to comply with interface
    }

    @Test
    @DisplayName("Implicit multiplication between two parenthesized groups when allowed inserts '*' (BRACE_CLOSE→BRACE_OPEN)")
    public void test_TC11() throws Exception {
        String expr = "(1)(2)";
        StubOperatorDictionary opDict = new StubOperatorDictionary("*");
        StubFunctionDictionary funcDict = new StubFunctionDictionary();
        ExpressionConfiguration config = new StubConfig(true, false, opDict, funcDict);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        List<Token> tokens = tokenizer.parse();
        List<String> texts = new ArrayList<>();
        for (Token t : tokens) texts.add(t.getToken());
        assertEquals(Arrays.asList("(", "1", ")", "*", "(", "2", ")"), texts);
    }

    @Test
    @DisplayName("Implicit multiplication between number and '(' when disabled throws \"Missing operator\" (NUMBER_LITERAL→BRACE_OPEN)")
    public void test_TC12() {
        String expr = "2(3)";
        StubOperatorDictionary opDict = new StubOperatorDictionary("*");
        StubFunctionDictionary funcDict = new StubFunctionDictionary();
        ExpressionConfiguration config = new StubConfig(false, false, opDict, funcDict);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertEquals("Missing operator", ex.getMessage());
    }

    @Test
    @DisplayName("Hexadecimal number literal '0x1A3F' is parsed as NUMBER_LITERAL via parseHexNumberLiteral")
    public void test_TC13() throws Exception {
        String expr = "0x1A3F";
        StubOperatorDictionary opDict = new StubOperatorDictionary();
        StubFunctionDictionary funcDict = new StubFunctionDictionary();
        ExpressionConfiguration config = new StubConfig(false, false, opDict, funcDict);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        List<Token> tokens = tokenizer.parse();
        assertEquals(1, tokens.size());
        assertEquals("0x1A3F", tokens.get(0).getToken());
        assertEquals(Token.TokenType.NUMBER_LITERAL, tokens.get(0).getType());
    }

    @Test
    @DisplayName("Decimal literal with two dots '1.2.3' throws ParseException \"Number contains more than one decimal point\"")
    public void test_TC14() {
        String expr = "1.2.3";
        StubOperatorDictionary opDict = new StubOperatorDictionary();
        StubFunctionDictionary funcDict = new StubFunctionDictionary();
        ExpressionConfiguration config = new StubConfig(false, false, opDict, funcDict);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertEquals("Number contains more than one decimal point", ex.getMessage());
    }

    @Test
    @DisplayName("Scientific notation without mantissa '2e' throws ParseException \"Illegal scientific format\"")
    public void test_TC15() {
        String expr = "2e";
        StubOperatorDictionary opDict = new StubOperatorDictionary();
        StubFunctionDictionary funcDict = new StubFunctionDictionary();
        ExpressionConfiguration config = new StubConfig(false, false, opDict, funcDict);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertEquals("Illegal scientific format", ex.getMessage());
    }

    @Test
    @DisplayName("Undefined function name 'foo(' throws ParseException \"Undefined function 'foo'\"")
    public void test_TC16() {
        String expr = "foo(";
        StubOperatorDictionary opDict = new StubOperatorDictionary();
        StubFunctionDictionary funcDict = new StubFunctionDictionary(); // no 'foo'
        ExpressionConfiguration config = new StubConfig(false, false, opDict, funcDict);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertTrue(ex.getMessage().contains("Undefined function 'foo'"));
    }

    @Test
    @DisplayName("Unclosed string literal throws ParseException \"Closing quote not found\"")
    public void test_TC17() {
        String expr = "\"abc";
        StubOperatorDictionary opDict = new StubOperatorDictionary();
        StubFunctionDictionary funcDict = new StubFunctionDictionary();
        ExpressionConfiguration config = new StubConfig(false, true, opDict, funcDict);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertEquals("Closing quote not found", ex.getMessage());
    }

    @Test
    @DisplayName("Unknown escape sequence '\\q' in string literal throws ParseException \"Unknown escape character\"")
    public void test_TC18() {
        String expr = "\"\\q\"";
        StubOperatorDictionary opDict = new StubOperatorDictionary();
        StubFunctionDictionary funcDict = new StubFunctionDictionary();
        ExpressionConfiguration config = new StubConfig(false, true, opDict, funcDict);
        Tokenizer tokenizer = new Tokenizer(expr, config);
        ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
        assertTrue(ex.getMessage().contains("Unknown escape character"));
    }
}