package com.ezylang.evalex.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Tokenizer_parse_2_Test {

    /**
     * STUBS & HELPERS
     */
    private static class StubOpDict implements OperatorDictionaryIfc {
        // control which operators are available
        java.util.Set<String> prefix = Collections.emptySet();
        java.util.Set<String> infix = Collections.emptySet();
        java.util.Set<String> postfix = Collections.emptySet();
        @Override public boolean hasPrefixOperator(String s) { return prefix.contains(s); }
        @Override public OperatorIfc getPrefixOperator(String s) { return new OperatorIfc() {}; }
        @Override public boolean hasInfixOperator(String s) { return infix.contains(s); }
        @Override public OperatorIfc getInfixOperator(String s) { return new OperatorIfc() {}; }
        @Override public boolean hasPostfixOperator(String s) { return postfix.contains(s); }
        @Override public OperatorIfc getPostfixOperator(String s) { return new OperatorIfc() {}; }
        @Override public void addOperator(String s, OperatorIfc op) {} // Implemented as per interface requirement
    }

    private static class StubFuncDict implements FunctionDictionaryIfc {
        java.util.Set<String> funcs = Collections.emptySet();
        @Override public boolean hasFunction(String name) { return funcs.contains(name); }
        @Override public FunctionIfc getFunction(String name) { return new FunctionIfc() {}; }
        @Override public void addFunction(String name, FunctionIfc func) {} // Implemented as per interface requirement
    }

    private static ExpressionConfiguration makeConfig(
            boolean arraysAllowed,
            boolean structuresAllowed,
            boolean implicitMult,
            boolean singleQuote,
            StubOpDict od,
            StubFuncDict fd
    ) {
        return new ExpressionConfiguration(arraysAllowed, structuresAllowed, implicitMult, singleQuote);
    }

    private void assertSequence(List<Token> tokens, Token.TokenType[] types, String[] values) {
        assertEquals(types.length, tokens.size(), "token count");
        for (int i = 0; i < types.length; i++) {
            assertEquals(types[i], tokens.get(i).getType(), "type at " + i);
            assertEquals(values[i], tokens.get(i).getValue(), "value at " + i);
        }
    }

    @Test
    @DisplayName("Valid structure separator between variable and identifier inserts '.' token without error")
    void test_TC19() throws Exception {
        String expr = "x.y";
        StubOpDict od = new StubOpDict();
        StubFuncDict fd = new StubFuncDict();
        ExpressionConfiguration config = makeConfig(false, true, false, false, od, fd);

        List<Token> tokens = new Tokenizer(expr, config).parse();
        assertSequence(tokens,
                new Token.TokenType[]{Token.TokenType.VARIABLE_OR_CONSTANT, Token.TokenType.STRUCTURE_SEPARATOR, Token.TokenType.VARIABLE_OR_CONSTANT},
                new String[]{"x", ".", "y"});
    }

    @Test
    @DisplayName("Valid array indexing produces ARRAY_OPEN, NUMBER_LITERAL, ARRAY_CLOSE tokens")
    void test_TC20() throws Exception {
        String expr = "a[1]";
        StubOpDict od = new StubOpDict();
        StubFuncDict fd = new StubFuncDict();
        ExpressionConfiguration config = makeConfig(true, false, false, false, od, fd);

        List<Token> tokens = new Tokenizer(expr, config).parse();
        assertSequence(tokens,
                new Token.TokenType[]{Token.TokenType.VARIABLE_OR_CONSTANT, Token.TokenType.ARRAY_OPEN, Token.TokenType.NUMBER_LITERAL, Token.TokenType.ARRAY_CLOSE},
                new String[]{"a", "[", "1", "]"});
    }

    @Test
    @DisplayName("Defined function name followed by parentheses yields FUNCTION then BRACE_OPEN and BRACE_CLOSE")
    void test_TC21() throws Exception {
        String expr = "f()";
        StubOpDict od = new StubOpDict();
        StubFuncDict fd = new StubFuncDict(); fd.funcs = Collections.singleton("f");
        ExpressionConfiguration config = makeConfig(false, false, false, false, od, fd);

        List<Token> tokens = new Tokenizer(expr, config).parse();
        assertSequence(tokens,
                new Token.TokenType[]{Token.TokenType.FUNCTION, Token.TokenType.BRACE_OPEN, Token.TokenType.BRACE_CLOSE},
                new String[]{"f", "(", ")"});
    }

    @Test
    @DisplayName("Unary prefix operator '-' is recognized when previous token is null")
    void test_TC22() throws Exception {
        String expr = "-5";
        StubOpDict od = new StubOpDict(); od.prefix = Collections.singleton("-");
        StubFuncDict fd = new StubFuncDict();
        ExpressionConfiguration config = makeConfig(false, false, false, false, od, fd);

        List<Token> tokens = new Tokenizer(expr, config).parse();
        assertSequence(tokens,
                new Token.TokenType[]{Token.TokenType.PREFIX_OPERATOR, Token.TokenType.NUMBER_LITERAL},
                new String[]{"-", "5"});
    }

    @Test
    @DisplayName("Postfix operator '?' is recognized after a variable")
    void test_TC23() throws Exception {
        String expr = "a?";
        StubOpDict od = new StubOpDict(); od.postfix = Collections.singleton("?");
        StubFuncDict fd = new StubFuncDict();
        ExpressionConfiguration config = makeConfig(false, false, false, false, od, fd);

        List<Token> tokens = new Tokenizer(expr, config).parse();
        assertSequence(tokens,
                new Token.TokenType[]{Token.TokenType.VARIABLE_OR_CONSTANT, Token.TokenType.POSTFIX_OPERATOR},
                new String[]{"a", "?"});
    }

    @Test
    @DisplayName("Multi-character infix operator '==' is parsed via looping in parseOperator")
    void test_TC24() throws Exception {
        String expr = "==";
        StubOpDict od = new StubOpDict();
        od.infix = new HashSet<>();
        od.infix.add("=");
        od.infix.add("==");
        StubFuncDict fd = new StubFuncDict();
        ExpressionConfiguration config = makeConfig(false, false, false, false, od, fd);

        List<Token> tokens = new Tokenizer(expr, config).parse();
        assertEquals(1, tokens.size());
        assertEquals(Token.TokenType.INFIX_OPERATOR, tokens.get(0).getType());
        assertEquals("==", tokens.get(0).getValue());
    }

    @Test
    @DisplayName("Undefined single-character operator '$' throws ParseException for undefined operator")
    void test_TC25() {
        String expr = "$";
        StubOpDict od = new StubOpDict();
        StubFuncDict fd = new StubFuncDict();
        ExpressionConfiguration config = makeConfig(false, false, false, false, od, fd);

        ParseException ex = assertThrows(ParseException.class, () -> {
            new Tokenizer(expr, config).parse();
        });
        assertTrue(ex.getMessage().contains("Undefined operator '$'"));
    }

    @Test
    @DisplayName("Decimal number starting with dot ".5" is parsed as NUMBER_LITERAL")
    void test_TC26() throws Exception {
        String expr = ".5";
        StubOpDict od = new StubOpDict();
        StubFuncDict fd = new StubFuncDict();
        ExpressionConfiguration config = makeConfig(false, false, false, false, od, fd);

        List<Token> tokens = new Tokenizer(expr, config).parse();
        assertEquals(1, tokens.size());
        assertEquals(Token.TokenType.NUMBER_LITERAL, tokens.get(0).getType());
        assertEquals(".5", tokens.get(0).getValue());
    }
}