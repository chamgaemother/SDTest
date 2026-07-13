package com.thealgorithms.others;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JUnit 5 tests for MiniMaxAlgorithm#setScores(int[]).
 */
public class MiniMaxAlgorithm_setScores_1_Test {

    @Test
    @DisplayName("TC04: setScores(null) throws NullPointerException when scores array is null")
    void test_TC04() {
        // GIVEN a fresh MiniMaxAlgorithm instance
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        // WHEN & THEN calling setScores(null) should throw NullPointerException
        // Explanation: scores parameter is null, so accessing scores.length in the method triggers NPE (branch B0 exception path)
        assertThrows(NullPointerException.class, () -> alg.setScores(null));
    }

    @Test
    @DisplayName("TC05: setScores(empty array) triggers StackOverflowError due to log2(0) recursion")
    void test_TC05() {
        // GIVEN a fresh MiniMaxAlgorithm instance
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        // an empty array will have length 0, 0 % 1 == 0 so enters branch B1,
        // then height = log2(0) leads to infinite recursion and StackOverflowError
        int[] emptyScores = new int[0];
        // WHEN & THEN calling setScores on empty array should throw StackOverflowError
        assertThrows(StackOverflowError.class, () -> alg.setScores(emptyScores));
    }
}