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
import java.util.List;
public class Tokenizer_parse_2_Test {

  // A stub configuration to control allowed features and operator/function dictionaries
  static class StubConfig implements ExpressionConfiguration, OperatorDictionaryIfc, FunctionDictionaryIfc {
    private final boolean arraysAllowed;
    private final boolean structuresAllowed;
    private final OperatorDictionaryIfc opDict;
    private final FunctionDictionaryIfc fnDict;

    StubConfig(boolean arraysAllowed, boolean structuresAllowed,
               OperatorDictionaryIfc opDict, FunctionDictionaryIfc fnDict) {
      this.arraysAllowed = arraysAllowed;
      this.structuresAllowed = structuresAllowed;
      this.opDict = opDict;
      this.fnDict = fnDict;
    }

    StubConfig allowArrays(boolean v) {
      return new StubConfig(v, this.structuresAllowed, this.opDict, this.fnDict);
    }
    StubConfig allowStructures(boolean v) {
      return new StubConfig(this.arraysAllowed, v, this.opDict, this.fnDict);
    }
    StubConfig withOperatorDict(OperatorDictionaryIfc d) {
      return new StubConfig(this.arraysAllowed, this.structuresAllowed, d, this.fnDict);
    }
    StubConfig withFunctionDict(FunctionDictionaryIfc d) {
      return new StubConfig(this.arraysAllowed, this.structuresAllowed, this.opDict, d);
    }

    @Override public OperatorDictionaryIfc getOperatorDictionary() { return opDict; }
    @Override public FunctionDictionaryIfc getFunctionDictionary() { return fnDict; }
    @Override public boolean isArraysAllowed() { return arraysAllowed; }
    @Override public boolean isStructuresAllowed() { return structuresAllowed; }
    @Override public boolean isImplicitMultiplicationAllowed() { return false; }
    @Override public boolean isSingleQuoteStringLiteralsAllowed() { return false; }

    @Override public boolean hasPrefixOperator(String s) { return false; }
    @Override public OperatorIfc getPrefixOperator(String s) { return null; }
    @Override public boolean hasPostfixOperator(String s) { return false; }
    @Override public OperatorIfc getPostfixOperator(String s) { return null; }
    @Override public boolean hasInfixOperator(String s) { return false; }
    @Override public OperatorIfc getInfixOperator(String s) { return null; }
    @Override public boolean hasFunction(String name) { return false; }
    @Override public FunctionIfc getFunction(String name) { return null; }
  }

  // Empty operator dictionary: no operators registered
  static final OperatorDictionaryIfc EMPTY_OP_DICT = new OperatorDictionaryIfc() {
    @Override public boolean hasPrefixOperator(String s) { return false; }
    @Override public OperatorIfc getPrefixOperator(String s) { return null; }
    @Override public boolean hasPostfixOperator(String s) { return false; }
    @Override public OperatorIfc getPostfixOperator(String s) { return null; }
    @Override public boolean hasInfixOperator(String s) { return false; }
    @Override public OperatorIfc getInfixOperator(String s) { return null; }
  };

  // Empty function dictionary
  static final FunctionDictionaryIfc EMPTY_FN_DICT = new FunctionDictionaryIfc() {
    @Override public boolean hasFunction(String name) { return false; }
    @Override public FunctionIfc getFunction(String name) { return null; }
  };

  @Test
  @DisplayName("Array open at start throws exception when no previous token (arrayOpenOrStructureSeparatorNotAllowed)")
  void test_TC19() {
    // '[' at start with arraysAllowed=true triggers arrayOpenOrStructureSeparatorNotAllowed in parseArrayOpen
    ExpressionConfiguration config = new StubConfig(true, false, EMPTY_OP_DICT, EMPTY_FN_DICT).allowArrays(true);
    Tokenizer tokenizer = new Tokenizer("[", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Array open not allowed here", ex.getMessage());
  }

  @Test
  @DisplayName("Structure separator after infix operator throws exception (Structure not allowed here)")
  void test_TC20() {
    // Input "1+." where '.' after INFIX_OPERATOR '+' causes parseStructureSeparator failure
    // Provide a dictionary registering '+' as infix operator
    OperatorDictionaryIfc plusDict = new OperatorDictionaryIfc() {
      @Override public boolean hasPrefixOperator(String s) { return false; }
      @Override public OperatorIfc getPrefixOperator(String s) { return null; }
      @Override public boolean hasPostfixOperator(String s) { return false; }
      @Override public OperatorIfc getPostfixOperator(String s) { return null; }
      @Override public boolean hasInfixOperator(String s) { return "+".equals(s); }
      @Override public OperatorIfc getInfixOperator(String s) { return (t, u) -> null; }
    };
    ExpressionConfiguration config = new StubConfig(false, true, plusDict, EMPTY_FN_DICT).allowStructures(true);
    Tokenizer tokenizer = new Tokenizer("1+.", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Structure separator not allowed here", ex.getMessage());
  }

  @Test
  @DisplayName("Undefined operator throws exception in parseOperator")
  void test_TC21() {
    // '$' not registered in any operator dict leads to undefined operator exception
    ExpressionConfiguration config = new StubConfig(false, false, EMPTY_OP_DICT, EMPTY_FN_DICT);
    Tokenizer tokenizer = new Tokenizer("$", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Undefined operator '$'", ex.getMessage());
  }

  @Test
  @DisplayName("Valid scientific notation decimal literal is accepted")
  void test_TC22() throws ParseException {
    // "3.14e+2" should produce a single NUMBER_LITERAL token in scientific notation
    ExpressionConfiguration config = new StubConfig(false, false, EMPTY_OP_DICT, EMPTY_FN_DICT);
    Tokenizer tokenizer = new Tokenizer("3.14e+2", config);
    List<Token> tokens = tokenizer.parse();
    assertAll(
      () -> assertEquals(1, tokens.size()),
      () -> assertEquals(Token.TokenType.NUMBER_LITERAL, tokens.get(0).getType()),
      () -> assertEquals("3.14e+2", tokens.get(0).getTokenString())
    );
  }

  @Test
  @DisplayName("Unexpected closing array throws exception when arrayBalance drops below zero")
  void test_TC23() {
    // "[1]]" will parse '[', '1', ']', then extra ']' causes arrayBalance < 0 error
    ExpressionConfiguration config = new StubConfig(true, false, EMPTY_OP_DICT, EMPTY_FN_DICT).allowArrays(true);
    Tokenizer tokenizer = new Tokenizer("[1]]", config);
    ParseException ex = assertThrows(ParseException.class, tokenizer::parse);
    assertEquals("Unexpected closing array", ex.getMessage());
  }
}