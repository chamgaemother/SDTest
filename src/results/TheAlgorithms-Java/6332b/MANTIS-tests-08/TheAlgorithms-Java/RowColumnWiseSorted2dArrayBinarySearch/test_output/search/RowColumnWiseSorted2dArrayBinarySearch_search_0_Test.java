package com.thealgorithms.searches;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class RowColumnWiseSorted2dArrayBinarySearch_search_0_Test {

    @Test
    @DisplayName("TC01: Empty matrix returns {-1,-1} covering initial rowPointer>=length branch")
    void test_TC01() {
        // matrix length is 0, so rowPointer (0) >= matrix.length triggers immediate not-found
        Integer[][] matrix = new Integer[0][0];
        Integer target = 1;
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC02: Single-element matrix where target equals element returns [0,0] covering comp==0 branch")
    void test_TC02() {
        // comp == 0 at first compare when matrix[0][0] equals target
        Integer[][] matrix = new Integer[][]{{5}};
        Integer target = 5;
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        assertArrayEquals(new int[]{0, 0}, result);
    }

    @Test
    @DisplayName("TC03: Single-element matrix where target greater than element returns {-1,-1} covering comp>0 branch then exit")
    void test_TC03() {
        // comp > 0 at compare (3 > 2) moves rowPointer to 1, then rowPointer>=length exits
        Integer[][] matrix = new Integer[][]{{2}};
        Integer target = 3;
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC04: Single-element matrix where target less than element returns {-1,-1} covering comp<0 branch then colPointer<0")
    void test_TC04() {
        // comp < 0 (1 < 2) moves colPointer to -1, then colPointer<0 exits loop
        Integer[][] matrix = new Integer[][]{{2}};
        Integer target = 1;
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC05: 2×2 matrix target at [1,1] reached via comp>0 then comp==0 branches")
    void test_TC05() {
        // First comp>0 (4 > 2) moves row to 1, then comp == 0 at matrix[1][1]
        Integer[][] matrix = new Integer[][]{{1, 2}, {3, 4}};
        Integer target = 4;
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        assertArrayEquals(new int[]{1, 1}, result);
    }

    @Test
    @DisplayName("TC06: 2×2 matrix target at [0,0] reached via comp<0 then comp==0 branches")
    void test_TC06() {
        // First comp<0 (1 < 2) moves col to 0, then comp == 0 at matrix[0][0]
        Integer[][] matrix = new Integer[][]{{1, 2}, {3, 4}};
        Integer target = 1;
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        assertArrayEquals(new int[]{0, 0}, result);
    }

    @Test
    @DisplayName("TC07: 2×2 matrix not containing target returns {-1,-1} after multiple comp>0 iterations")
    void test_TC07() {
        // comp>0 twice moves rowPointer beyond end then exit not-found
        Integer[][] matrix = new Integer[][]{{1, 2}, {3, 4}};
        Integer target = 5;
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        assertArrayEquals(new int[]{-1, -1}, result);
    }

    @Test
    @DisplayName("TC08: 4×4 matrix target at middle [2,2] after mixed comp>0 and comp<0 iterations")
    void test_TC08() {
        /*
         * Steps:
         * compare 38 with [0][3]=40 -> comp<0, col=2
         * compare 38 with [0][2]=30 -> comp>0, row=1
         * compare 38 with [1][2]=35 -> comp>0, row=2
         * compare 38 with [2][2]=38 -> comp==0, found
         */
        Integer[][] matrix = new Integer[][]{
            {10, 20, 30, 40},
            {15, 25, 35, 45},
            {18, 28, 38, 48},
            {21, 31, 41, 51}
        };
        Integer target = 38;
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        assertArrayEquals(new int[]{2, 2}, result);
    }

    @Test
    @DisplayName("TC09: Null matrix input throws NullPointerException at length access")
    void test_TC09() {
        // matrix null causes NPE on access to matrix.length
        Integer[][] matrix = null;
        Integer target = 1;
        assertThrows(NullPointerException.class, () ->
            RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target)
        );
    }

    @Test
    @DisplayName("TC10: Null key input throws NullPointerException at compareTo call")
    void test_TC10() {
        // target null causes NPE when calling target.compareTo(...)
        Integer[][] matrix = new Integer[][]{{1}};
        Integer target = null;
        assertThrows(NullPointerException.class, () ->
            RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target)
        );
    }
}