package com.thealgorithms.others;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class MiniMaxAlgorithm_setScores_0_Test {

    @Test
    @DisplayName("TC01: Valid power-of-2 length=1 sets scores and updates height to 0 (length%1==0 branch)")
    public void test_TC01() {
        // Given a new algorithm instance and a single-element array (power of 2 length=1)
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        int[] newScores = new int[]{42};
        // When setting scores to a valid power-of-2 length
        alg.setScores(newScores);
        // Then the scores reference must be updated to the provided array
        assertSame(newScores, alg.getScores(), "Scores reference should be updated for length 1");
        // And height should be log2(1) == 0
        assertEquals(0, alg.getHeight(), "Height should be updated to 0 for length 1");
    }

    @Test
    @DisplayName("TC02: Valid power-of-2 length=8 sets scores and updates height to 3")
    public void test_TC02() {
        // Given a new algorithm instance and an eight-element array (power of 2 length=8)
        MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
        int[] newScores = new int[8];
        // When setting scores to a valid power-of-2 length
        alg.setScores(newScores);
        // Then the scores reference must be updated to the provided array
        assertSame(newScores, alg.getScores(), "Scores reference should be updated for length 8");
        // And height should be log2(8) == 3
        assertEquals(3, alg.getHeight(), "Height should be updated to 3 for length 8");
    }

    @Test
    @DisplayName("TC03: Invalid non-power-of-2 length=3 rejects update and retains original scores (length%1!=0 branch)")
    public void test_TC03() {
        // Given System.out redirected and a new algorithm
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
            int[] originalScores = alg.getScores().clone();
            int originalHeight = alg.getHeight();
            int[] newScores = new int[3]; // length=3, not a power of 2
            // When attempting to set invalid scores
            alg.setScores(newScores);
            // Then the scores and height remain unchanged
            assertArrayEquals(originalScores, alg.getScores(), "Scores should remain unchanged for invalid length 3");
            assertEquals(originalHeight, alg.getHeight(), "Height should remain unchanged for invalid length 3");
            // And the output must contain the rejection message
            String printed = out.toString();
            assertTrue(printed.contains("must be a power of 2"), "Output should mention power of 2 requirement");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("TC04: Invalid non-power-of-2 length=5 rejects update and retains original scores")
    public void test_TC04() {
        // Given System.out redirected and a new algorithm
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
            int[] originalScores = alg.getScores().clone();
            int originalHeight = alg.getHeight();
            int[] newScores = new int[5]; // length=5, not a power of 2
            // When attempting to set invalid scores
            alg.setScores(newScores);
            // Then the scores and height remain unchanged
            assertArrayEquals(originalScores, alg.getScores(), "Scores should remain unchanged for invalid length 5");
            assertEquals(originalHeight, alg.getHeight(), "Height should remain unchanged for invalid length 5");
            // And the output must contain the rejection message
            String printed = out.toString();
            assertTrue(printed.contains("must be a power of 2"), "Output should mention power of 2 requirement");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("TC05: Invalid zero-length array rejects update and retains original scores")
    public void test_TC05() {
        // Given System.out redirected and a new algorithm
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            MiniMaxAlgorithm alg = new MiniMaxAlgorithm();
            int[] originalScores = alg.getScores().clone();
            int originalHeight = alg.getHeight();
            int[] newScores = new int[0]; // length=0, not a power of 2
            // When attempting to set invalid scores
            alg.setScores(newScores);
            // Then the scores and height remain unchanged
            assertArrayEquals(originalScores, alg.getScores(), "Scores should remain unchanged for zero length");
            assertEquals(originalHeight, alg.getHeight(), "Height should remain unchanged for zero length");
            // And the output must contain the rejection message
            String printed = out.toString();
            assertTrue(printed.contains("must be a power of 2"), "Output should mention power of 2 requirement");
        } finally {
            System.setOut(originalOut);
        }
    }
}