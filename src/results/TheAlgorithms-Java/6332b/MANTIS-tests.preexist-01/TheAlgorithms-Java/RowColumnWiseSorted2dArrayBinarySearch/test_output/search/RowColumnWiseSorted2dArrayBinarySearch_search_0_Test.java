package com.thealgorithms.searches;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class RowColumnWiseSorted2dArrayBinarySearch_search_0_Test {

    @Test
    @DisplayName("TC01: Empty matrix returns [-1,-1] via immediate rowPointer>=length check (loop-0)")
    void test_TC01() {
        Integer[][] matrix = new Integer[0][0];
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 5);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC02: Single-element matrix equal target returns [0,0] (loop-1, comp==0)")
    void test_TC02() {
        Integer[][] matrix = new Integer[][]{{7}};
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 7);
        assertArrayEquals(new int[]{0, 0}, result);
    }

    @Test
    @DisplayName("TC03: Single-element matrix target greater triggers rowPointer++ then exit returns [-1,-1] (loop-1, comp>0)")
    void test_TC03() {
        Integer[][] matrix = new Integer[][]{{3}};
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 5);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC04: Single-element matrix target lesser triggers colPointer-- then exit returns [-1,-1] (loop-1, comp<0)")
    void test_TC04() {
        Integer[][] matrix = new Integer[][]{{10}};
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 5);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC05: 4x4 matrix find 35 via comp<0 then comp>0 then comp==0 (loop-N mixed)")
    void test_TC05() {
        Integer[][] matrix = new Integer[][]{
            {10, 20, 30, 40},
            {15, 25, 35, 45},
            {18, 28, 38, 48},
            {21, 31, 41, 51}
        };
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 35);
        assertArrayEquals(new int[]{1, 2}, result);
    }

    @Test
    @DisplayName("TC06: 4x4 matrix target > all triggers repeated rowPointer++ until exit returns [-1,-1] (loop-N, comp>0)")
    void test_TC06() {
        Integer[][] matrix = new Integer[][]{
            {10, 20, 30, 40},
            {15, 25, 35, 45},
            {18, 28, 38, 48},
            {21, 31, 41, 51}
        };
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 100);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC07: 4x4 matrix target < all triggers repeated colPointer-- until exit returns [-1,-1] (loop-N, comp<0)")
    void test_TC07() {
        Integer[][] matrix = new Integer[][]{
            {10, 20, 30, 40},
            {15, 25, 35, 45},
            {18, 28, 38, 48},
            {21, 31, 41, 51}
        };
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 5);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC08: Null matrix parameter throws NullPointerException at entry")
    void test_TC08() {
        Integer[][] matrix = null;
        assertThrows(NullPointerException.class, () -> 
            RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 1)
        );
    }

    @Test
    @DisplayName("TC09: Null target throws NullPointerException during compareTo")
    void test_TC09() {
        Integer[][] matrix = new Integer[][]{{1}};
        Integer target = null;
        assertThrows(NullPointerException.class, () -> 
            RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target)
        );
    }

    @Test
    @DisplayName("TC10: Matrix with null element throws NullPointerException during compareTo")
    void test_TC10() {
        Integer[][] matrix = new Integer[][]{{null}};
        Integer target = 5;
        assertThrows(NullPointerException.class, () -> 
            RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target)
        );
    }
}