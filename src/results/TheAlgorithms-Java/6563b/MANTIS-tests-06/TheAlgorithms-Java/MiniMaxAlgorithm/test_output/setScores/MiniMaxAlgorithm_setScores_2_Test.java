package com.thealgorithms.others;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
public class MiniMaxAlgorithm_setScores_2_Test {

    @Test
    @DisplayName("TC05: setScores updates scores and height when given a valid power-of-two array of length 2 (branch: valid length, minimal non-trivial case)")
    void test_TC05() {
        // Given a new MiniMaxAlgorithm instance
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        // Use a minimal non-trivial power-of-two length (2) to exercise the valid branch B0->B1
        int[] newScores = {7, 3};

        // When setting valid scores
        alg.setScores(newScores);

        // Then scores should be updated and height recalculated to log2(2)=1
        assertArrayEquals(newScores, alg.getScores(), "Scores array was not updated correctly for length 2");
        assertEquals(1, alg.getHeight(), "Height was not correctly set to 1 for array length 2");
    }

    @Test
    @DisplayName("TC06: setScores updates scores and height when given a larger valid power-of-two array of length 16 (branch: valid length, deeper log2 recursion)")
    void test_TC06() {
        // Given a new MiniMaxAlgorithm instance
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        // Prepare a power-of-two length 16 array to drive deeper log2 recursion
        int[] newScores = new int[16];
        for (int i = 0; i < 16; i++) {
            newScores[i] = i + 1;
        }

        // When setting valid scores of length 16
        alg.setScores(newScores);

        // Then scores should be updated and height recalculated to log2(16)=4
        assertArrayEquals(newScores, alg.getScores(), "Scores array was not updated correctly for length 16");
        assertEquals(4, alg.getHeight(), "Height was not correctly set to 4 for array length 16");
    }

    @Test
    @DisplayName("TC07: setScores prints error and leaves state unchanged for an empty array of length 0 (branch: empty array treated invalid leading to B2)")
    void test_TC07() {
        // Given a new MiniMaxAlgorithm instance and capture original state
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        int[] originalScores = alg.getScores().clone();
        int originalHeight = alg.getHeight();
        // Provide an empty array to trigger the invalid branch B0->B2
        int[] newScores = new int[0];

        // Capture System.out output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            // When setting invalid (empty) scores
            alg.setScores(newScores);
        } finally {
            // Restore original System.out
            System.setOut(originalOut);
        }

        // Then scores and height should remain unchanged
        assertArrayEquals(originalScores, alg.getScores(), "Scores should remain unchanged on invalid input");
        assertEquals(originalHeight, alg.getHeight(), "Height should remain unchanged on invalid input");
        // And an error message should be printed indicating invalid power-of-two length
        String output = outContent.toString();
        assertTrue(output.contains("must be a power of 2"),
            "Expected error message about power-of-two requirement was not printed");
    }
}