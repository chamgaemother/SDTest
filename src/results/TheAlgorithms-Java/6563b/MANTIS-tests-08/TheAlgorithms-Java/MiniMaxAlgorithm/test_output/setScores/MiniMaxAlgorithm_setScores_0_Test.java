package com.thealgorithms.others;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class MiniMaxAlgorithm_setScores_0_Test {

    @Test
    @DisplayName("TC01: setScores accepts an array of power-of-2 length 1 and updates scores and height (branch-false)")
    void test_TC01() {
        // GIVEN a new algorithm and a minimal power-of-2 array of length 1
        MiniMaxAlgorithm algo = new MiniMaxAlgorithm();
        int[] newScores = new int[]{5};
        
        // WHEN setting the scores to an array of length 1 (1 is 2^0, so log2(1)=0)
        algo.setScores(newScores);
        
        // THEN the internal scores reference must update to the provided array and height must be 0
        assertSame(newScores, algo.getScores(), "The scores reference should be replaced when length is a power of 2 (1)");
        assertEquals(0, algo.getHeight(), "Height should be log2(1) == 0");
    }

    @Test
    @DisplayName("TC02: setScores accepts an array of power-of-2 length 4 and updates scores and height (branch-false)")
    void test_TC02() {
        // GIVEN a new algorithm and a typical power-of-2 array of length 4
        MiniMaxAlgorithm algo = new MiniMaxAlgorithm();
        int[] newScores = new int[]{1, 2, 3, 4};
        
        // WHEN setting the scores to an array of length 4 (4 is 2^2, so log2(4)=2)
        algo.setScores(newScores);
        
        // THEN the internal scores reference must update to the provided array and height must be 2
        assertSame(newScores, algo.getScores(), "The scores reference should be replaced when length is a power of 2 (4)");
        assertEquals(2, algo.getHeight(), "Height should be log2(4) == 2");
    }

    @Test
    @DisplayName("TC03: setScores rejects an array of non-power-of-2 length 3 without changing state (branch-true)")
    void test_TC03() {
        // GIVEN a new algorithm and capturing its original state
        MiniMaxAlgorithm algo = new MiniMaxAlgorithm();
        int[] originalScores = algo.getScores();
        int originalHeight = algo.getHeight();
        int[] newScores = new int[]{7, 8, 9};
        
        // WHEN setting the scores to an array of length 3 (not a power of 2)
        algo.setScores(newScores);
        
        // THEN the internal state must remain unchanged (scores reference and height)
        assertSame(originalScores, algo.getScores(), "Scores should remain unchanged when length is not a power of 2");
        assertEquals(originalHeight, algo.getHeight(), "Height should remain unchanged when length is not a power of 2");
    }

    @Test
    @DisplayName("TC04: setScores throws NullPointerException when passed null")
    void test_TC04() {
        // GIVEN a new algorithm
        MiniMaxAlgorithm algo = new MiniMaxAlgorithm();
        
        // WHEN and THEN calling setScores with null should throw NullPointerException immediately
        assertThrows(NullPointerException.class,
            () -> algo.setScores(null),
            "Passing null to setScores should throw NullPointerException"
        );
    }
}