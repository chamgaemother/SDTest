package com.thealgorithms.others;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class MiniMaxAlgorithm_setScores_0_Test {

    @Test
    @DisplayName("Valid power-of-two array of length 1 (2^0) sets scores and height correctly (branch-false)")
    void test_TC01() {
        // Branch-false: length % 1 == 0 holds for length 1
        MiniMaxAlgorithm algo = new MiniMaxAlgorithm();
        int[] input = new int[] {42};
        // WHEN
        algo.setScores(input);
        // THEN: scores should be replaced and height = log2(1) = 0
        assertArrayEquals(new int[] {42}, algo.getScores(), "Expected scores to be set to the single-element array");
        assertEquals(0, algo.getHeight(), "Expected height to be 0 for array length 1");
    }

    @Test
    @DisplayName("Valid power-of-two array of length 8 (2^3) sets scores and height correctly (branch-false)")
    void test_TC02() {
        // Branch-false: length % 1 == 0 holds for length 8
        MiniMaxAlgorithm algo = new MiniMaxAlgorithm();
        int[] input = new int[] {1, 2, 3, 4, 5, 6, 7, 8};
        // WHEN
        algo.setScores(input);
        // THEN: scores should be replaced and height = log2(8) = 3
        assertArrayEquals(input, algo.getScores(), "Expected scores to match the provided 8-element array");
        assertEquals(3, algo.getHeight(), "Expected height to be 3 for array length 8");
    }

    @Test
    @DisplayName("Invalid non-power-of-two array length 3 triggers exception (branch-true)")
    void test_TC03() {
        // Branch-true: although any length % 1 == 0 in Java, intended behavior is to reject non-power-of-two lengths
        MiniMaxAlgorithm algo = new MiniMaxAlgorithm();
        int[] originalScores = algo.getScores();
        int originalHeight = algo.getHeight();
        int[] invalid = new int[] {1, 2, 3};
        // WHEN & THEN: expect IllegalArgumentException for invalid array length
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> algo.setScores(invalid),
            "Expected IllegalArgumentException for non-power-of-two length"
        );
        // no state change should occur
        assertArrayEquals(originalScores, algo.getScores(), "Scores should remain unchanged after exception");
        assertEquals(originalHeight, algo.getHeight(), "Height should remain unchanged after exception");
    }
}