package com.thealgorithms.others;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MiniMaxAlgorithm#setScores(int[]).
 * Covers null input and empty array scenarios.
 */
public class MiniMaxAlgorithm_setScores_1_Test {

    @Test
    @DisplayName("setScores(null) throws NullPointerException when scores parameter is null")
    void test_TC05() {
        // GIVEN a MiniMaxAlgorithm instance
        MiniMaxAlgorithm instance = new MiniMaxAlgorithm();
        // WHEN & THEN calling setScores with null should throw NullPointerException
        assertThrows(NullPointerException.class, () -> {
            // null input triggers NPE at parameter access
            instance.setScores(null);
        });
    }

    @Test
    @DisplayName("setScores(empty array) triggers infinite recursion in log2 and results in StackOverflowError")
    void test_TC06() {
        // GIVEN a MiniMaxAlgorithm instance and an empty array
        MiniMaxAlgorithm instance = new MiniMaxAlgorithm();
        int[] emptyScores = new int[0];
        // The length is 0, so log2(0) never reaches base case (n==1), causing infinite recursion
        assertThrows(StackOverflowError.class, () -> {
            instance.setScores(emptyScores);
        });
    }
}