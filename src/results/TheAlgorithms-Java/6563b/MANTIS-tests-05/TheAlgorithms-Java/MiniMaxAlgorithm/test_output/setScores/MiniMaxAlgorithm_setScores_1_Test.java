package com.thealgorithms.others;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class MiniMaxAlgorithm_setScores_1_Test {

    @Test
    @DisplayName("setScores with length=1 triggers log2 base-case branch and sets height to 0")
    public void test_TC04() {
        // GIVEN: a MiniMaxAlgorithm instance and a single-element scores array
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        int[] newScores = {42};
        // WHEN: setting scores of length 1 (1 % 1 == 0) takes the B0->B1 path
        alg.setScores(newScores);
        // THEN: scores replaced and height should be log2(1) == 0 (base-case branch in log2)
        assertArrayEquals(newScores, alg.getScores(), "Scores should be updated to the new single-element array");
        assertEquals(0, alg.getHeight(), "Height must be 0 for an array of length 1 (log2 base case)");
    }

    @Test
    @DisplayName("setScores with zero-length array recurses in log2 and throws StackOverflowError")
    public void test_TC05() {
        // GIVEN: a MiniMaxAlgorithm instance and an empty scores array
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        int[] newScores = {};
        // WHEN/THEN: setting scores of length 0 (0 % 1 == 0) will hit B0->B1 and call log2(0), causing infinite recursion
        assertThrows(StackOverflowError.class, () -> alg.setScores(newScores),
            "Expected StackOverflowError due to infinite recursion in log2 for zero-length array");
    }
}