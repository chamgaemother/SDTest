package com.thealgorithms.others;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import com.thealgorithms.others.MiniMaxAlgorithm;
public class MiniMaxAlgorithm_setScores_0_Test {

    @Test
    @DisplayName("TC01: Calling setScores with null should throw NullPointerException for scores parameter")
    public void test_TC01() {
        MiniMaxAlgorithm algo = new MiniMaxAlgorithm();
        int[] originalScores = algo.getScores();
        int originalHeight = algo.getHeight();
        // Passing null should cause a NullPointerException before modifying any state (B0 path)
        assertThrows(NullPointerException.class, () -> algo.setScores(null));
        // State must remain unchanged after exception
        assertArrayEquals(originalScores, algo.getScores(), "Scores should be unchanged after NPE");
        assertEquals(originalHeight, algo.getHeight(), "Height should be unchanged after NPE");
    }

    @Test
    @DisplayName("TC02: Calling setScores with non-power-of-two length triggers rejection branch and prints error")
    public void test_TC02() {
        MiniMaxAlgorithm algo = new MiniMaxAlgorithm();
        int[] originalScores = algo.getScores();
        int originalHeight = algo.getHeight();
        int[] invalidScores = new int[3]; // length 3 is not a power of two -> triggers B0 true branch to B2

        // Capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            algo.setScores(invalidScores);
        } finally {
            // Restore System.out
            System.setOut(originalOut);
        }

        // Scores and height should remain unchanged
        assertArrayEquals(originalScores, algo.getScores(), "Scores should remain unchanged on invalid length");
        assertEquals(originalHeight, algo.getHeight(), "Height should remain unchanged on invalid length");
        // Output should contain the rejection message
        String output = outContent.toString();
        assertTrue(output.contains("The number of scores must be a power of 2."),
            "Expected error message when non-power-of-two scores length is provided");
    }

    @Test
    @DisplayName("TC03: Calling setScores with length=1 (2^0) sets scores and height=0")
    public void test_TC03() {
        MiniMaxAlgorithm algo = new MiniMaxAlgorithm();
        int[] newScores = new int[1]; // length is 1, a power-of-two -> B0 false branch to B1

        algo.setScores(newScores);

        // After setting, getScores should reference the provided array
        assertSame(newScores, algo.getScores(), "Scores should be set to the provided array of length 1");
        // log2(1) == 0, so height must be 0
        assertEquals(0, algo.getHeight(), "Height must be 0 for a single-element scores array");
    }

    @Test
    @DisplayName("TC04: Calling setScores with length=8 (2^3) sets scores and height=3")
    public void test_TC04() {
        MiniMaxAlgorithm algo = new MiniMaxAlgorithm();
        int[] newScores = new int[8]; // length is 8, a power-of-two -> B0 false branch to B1

        algo.setScores(newScores);

        // After setting, getScores should reference the provided array
        assertSame(newScores, algo.getScores(), "Scores should be set to the provided array of length 8");
        // log2(8) == 3, so height must be 3
        assertEquals(3, algo.getHeight(), "Height must be 3 for an eight-element scores array");
    }
}