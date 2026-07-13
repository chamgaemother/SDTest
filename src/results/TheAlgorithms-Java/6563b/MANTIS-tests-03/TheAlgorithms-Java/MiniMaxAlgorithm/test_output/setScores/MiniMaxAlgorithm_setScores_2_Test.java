package com.thealgorithms.others;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class MiniMaxAlgorithm_setScores_2_Test {

    @Test
    @DisplayName("setScores assigns a 2-element array (length=2, power-of-two) and computes height=1 (branch: B0->B1)")
    void test_TC07() {
        // Given: a MiniMaxAlgorithm instance with default random scores
        MiniMaxAlgorithm instance = new MiniMaxAlgorithm();
        // Choose a new scores array of length 2 (power of two) to force the branch where length%1 == 0 -> B1
        int[] newScores = new int[]{7, 14};

        // When: setScores is invoked with a valid power-of-two length array
        instance.setScores(newScores);

        // Then: the internal scores field should be replaced and height recalculated using log2(2) = 1
        assertArrayEquals(new int[]{7, 14}, instance.getScores(),
            "Expected scores to be replaced with the provided 2-element array");
        assertEquals(1, instance.getHeight(),
            "Expected height to be recalculated as log2(2) = 1");
    }
}