package com.thealgorithms.others;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test class for MiniMaxAlgorithm#setScores(int[]).
 */
public class MiniMaxAlgorithm_setScores_2_Test {

    @Test
    @DisplayName("TC06: setScores accepts a large power-of-2 length (8) and correctly updates height via log2 recursion")
    public void test_TC06() {
        // GIVEN: a MiniMaxAlgorithm instance and a new scores array of length 8 (a power of 2)
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        int[] newScores = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        // WHEN: calling setScores with length 8, (length % 1 == 0) is true -> B0->B1 path
        alg.setScores(newScores);
        // THEN: scores array should be updated and height = log2(8) = 3
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7, 8}, alg.getScores(),
                "Scores should match the new 8-element array");
        assertEquals(3, alg.getHeight(),
                "Height should be log2(8) = 3");
    }

    @Test
    @DisplayName("TC07: setScores accepts a minimal single-element array (size=1) and height remains zero")
    public void test_TC07() {
        // GIVEN: a MiniMaxAlgorithm instance and a single-element array -> minimal branch
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        int[] single = new int[]{42};
        // WHEN: calling setScores with length 1, (length % 1 == 0) true -> B0->B1 path
        alg.setScores(single);
        // THEN: scores should update and height = log2(1) = 0
        assertArrayEquals(new int[]{42}, alg.getScores(),
                "Scores should contain the single provided element");
        assertEquals(0, alg.getHeight(),
                "Height should be log2(1) = 0");
    }

    @Test
    @DisplayName("TC08: setScores(null) triggers NullPointerException immediately")
    public void test_TC08() {
        // GIVEN: a MiniMaxAlgorithm instance
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        // WHEN & THEN: calling setScores(null) should throw NPE before any state change -> B0 exception path
        assertThrows(NullPointerException.class, () -> alg.setScores(null),
                "Setting scores to null must throw NullPointerException immediately");
    }
}