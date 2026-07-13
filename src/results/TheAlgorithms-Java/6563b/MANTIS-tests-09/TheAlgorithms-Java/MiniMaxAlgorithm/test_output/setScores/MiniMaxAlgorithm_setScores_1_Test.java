package com.thealgorithms.others;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thealgorithms.others.MiniMaxAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class MiniMaxAlgorithm_setScores_1_Test {

    @Test
    @DisplayName("TC06: setScores(null) throws NullPointerException before any branch")
    void test_TC06() {
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        // Passing null should trigger a NullPointerException when accessing scores.length
        assertThrows(NullPointerException.class, () -> alg.setScores(null));
    }

    @Test
    @DisplayName("TC07: setScores(length=2) updates scores and height to 1 (valid power-of-2)")
    void test_TC07() {
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        int[] newScores = new int[2];
        // Length is 2, which is a power of 2, so B0→B1 path taken: scores set and height computed
        alg.setScores(newScores);
        // Verify the internal scores reference was updated
        assertSame(newScores, alg.getScores());
        // For length 2, log2(2) == 1, so height should be 1
        assertEquals(1, alg.getHeight());
    }
}