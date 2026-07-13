package com.thealgorithms.searches;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class RowColumnWiseSorted2dArrayBinarySearch_search_0_Test {

    @Test
    @DisplayName("TC01: empty matrix returns [-1,-1] (rowPointer>=length branch)")
    void test_TC01() {
        // rowPointer = 0 and matrix.length = 0 so rowPointer >= length immediately
        Integer[][] matrix = new Integer[0][];
        int[] result = com.thealgorithms.searches.RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 5);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC02: single-element matrix where comp==0 returns its coordinates")
    void test_TC02() {
        // matrix[0][0] == target so comp == 0 and direct hit branch
        Integer[][] matrix = {{5}};
        int[] result = com.thealgorithms.searches.RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 5);
        assertArrayEquals(new int[]{0, 0}, result);
    }

    @Test
    @DisplayName("TC03: single-element matrix comp>0 increments row then not found")
    void test_TC03() {
        // matrix[0][0] = 3, target > 3 so comp>0 triggers rowPointer++ then rowPointer==1 >= length
        Integer[][] matrix = {{3}};
        int[] result = com.thealgorithms.searches.RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 5);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC04: single-element matrix comp<0 decrements col then not found")
    void test_TC04() {
        // matrix[0][0] = 7, target < 7 so comp<0 triggers colPointer-- => -1 and exit
        Integer[][] matrix = {{7}};
        int[] result = com.thealgorithms.searches.RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 5);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC05: multi-iteration search finds target 28 at (2,1) after mixed moves")
    void test_TC05() {
        // Traverses: start at (0,3):40>28=>col--, (0,2):30>28=>col--, (0,1):20<28=>row++, (1,1):25<28=>row++,
        // (2,1):28==28 direct hit
        Integer[][] matrix = {
            {10, 20, 30, 40},
            {15, 25, 35, 45},
            {18, 28, 38, 48},
            {21, 31, 41, 51}
        };
        int[] result = com.thealgorithms.searches.RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 28);
        assertArrayEquals(new int[]{2, 1}, result);
    }

    @Test
    @DisplayName("TC06: multi-iteration search for absent target returns [-1,-1] after exiting bounds")
    void test_TC06() {
        // target=100 always > current => increment rowPointer until rowPointer==3 >= length and exit
        Integer[][] matrix = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        int[] result = com.thealgorithms.searches.RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 100);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC07: null matrix input throws NullPointerException at length access")
    void test_TC07() {
        // matrix is null, accessing matrix.length throws NPE
        Integer[][] matrix = null;
        assertThrows(NullPointerException.class, () -> 
            com.thealgorithms.searches.RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 5)
        );
    }

    @Test
    @DisplayName("TC08: null target throws NullPointerException at compareTo")
    void test_TC08() {
        // target is null, calling compareTo on null throws NPE
        Integer[][] matrix = {{1, 2}};
        assertThrows(NullPointerException.class, () -> 
            com.thealgorithms.searches.RowColumnWiseSorted2dArrayBinarySearch.search(matrix, null)
        );
    }

    @Test
    @DisplayName("TC09: null element in matrix throws NullPointerException during compareTo")
    void test_TC09() {
        // matrix[0][1] is null, compareTo(null) on target throws NPE
        Integer[][] matrix = {{1, null}};
        assertThrows(NullPointerException.class, () -> 
            com.thealgorithms.searches.RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 5)
        );
    }

    @Test
    @DisplayName("TC10: 2x2 matrix finds bottom-left element 3 at (1,0) via one decrement then one increment")
    void test_TC10() {
        // start at (0,1):2<3=>row++, (1,1):4>3=>col--, (1,0):3==3 direct hit
        Integer[][] matrix = {
            {1, 2},
            {3, 4}
        };
        int[] result = com.thealgorithms.searches.RowColumnWiseSorted2dArrayBinarySearch.search(matrix, 3);
        assertArrayEquals(new int[]{1, 0}, result);
    }
}