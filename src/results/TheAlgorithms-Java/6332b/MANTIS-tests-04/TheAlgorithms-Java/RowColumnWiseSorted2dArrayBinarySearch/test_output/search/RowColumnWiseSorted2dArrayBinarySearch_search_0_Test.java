package com.thealgorithms.searches;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.thealgorithms.searches.RowColumnWiseSorted2dArrayBinarySearch;
public class RowColumnWiseSorted2dArrayBinarySearch_search_0_Test {

    @Test
    @DisplayName("TC01: Empty matrix returns [-1,-1] without entering the loop (loop–0)")
    void test_TC01() {
        // GIVEN an empty matrix => matrix.length == 0 triggers immediate return [-1,-1] via B9->B8
        Integer[][] matrix = new Integer[0][0];
        Integer key = 1;
        // WHEN
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, key);
        // THEN
        assertArrayEquals(new int[] { -1, -1 }, result);
    }

    @Test
    @DisplayName("TC02: Single-element matrix where target equals element (comp==0, loop–1)")
    void test_TC02() {
        // GIVEN matrix {{5}} and key=5 => first compare yields comp==0, returns [0,0] at B2->B3
        Integer[][] matrix = new Integer[][] { { 5 } };
        Integer key = 5;
        // WHEN
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, key);
        // THEN
        assertArrayEquals(new int[] { 0, 0 }, result);
    }

    @Test
    @DisplayName("TC03: Single-element matrix where target is less (comp<0 branch, loop–2)")
    void test_TC03() {
        // GIVEN matrix {{3}} and key=2 => comp<0 enters colPointer-- (B4->B6), then colPointer<0 triggers not found
        Integer[][] matrix = new Integer[][] { { 3 } };
        Integer key = 2;
        // WHEN
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, key);
        // THEN
        assertArrayEquals(new int[] { -1, -1 }, result);
    }

    @Test
    @DisplayName("TC04: Single-element matrix where target is greater (comp>0 branch, loop–2)")
    void test_TC04() {
        // GIVEN matrix {{3}} and key=4 => comp>0 enters rowPointer++ (B4->B5), then rowPointer>=length triggers not found
        Integer[][] matrix = new Integer[][] { { 3 } };
        Integer key = 4;
        // WHEN
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, key);
        // THEN
        assertArrayEquals(new int[] { -1, -1 }, result);
    }

    @Test
    @DisplayName("TC05: 2×2 matrix find in first row last column (comp==0 on first iteration, loop–1)")
    void test_TC05() {
        // GIVEN matrix {{1,2},{3,4}} and key=2 => compare at [0,1] yields comp==0, return [0,1]
        Integer[][] matrix = new Integer[][] { { 1, 2 }, { 3, 4 } };
        Integer key = 2;
        // WHEN
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, key);
        // THEN
        assertArrayEquals(new int[] { 0, 1 }, result);
    }

    @Test
    @DisplayName("TC06: 2×2 matrix find element by moving down then left (mixed comp>0 then comp<0, loop–2)")
    void test_TC06() {
        // GIVEN matrix {{1,2},{3,4}} and key=3:
        // first comp>0 at [0,1] moves down (rowPointer=1), then at [1,1] comp<0 moves left and finds at [1,0]
        Integer[][] matrix = new Integer[][] { { 1, 2 }, { 3, 4 } };
        Integer key = 3;
        // WHEN
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, key);
        // THEN
        assertArrayEquals(new int[] { 1, 0 }, result);
    }

    @Test
    @DisplayName("TC07: 3×3 matrix where target not present exercises multiple movements (loop–N)")
    void test_TC07() {
        // GIVEN matrix {{10,20,30},{15,25,35},{18,28,38}} and key=27:
        // series of comp>0 and comp<0 moves that eventually exhaust pointers and return [-1,-1]
        Integer[][] matrix = new Integer[][] {
            { 10, 20, 30 },
            { 15, 25, 35 },
            { 18, 28, 38 }
        };
        Integer key = 27;
        // WHEN
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, key);
        // THEN
        assertArrayEquals(new int[] { -1, -1 }, result);
    }

    @Test
    @DisplayName("TC08: 3×3 matrix find lower-left corner after mixed movements (loop–N)")
    void test_TC08() {
        // GIVEN matrix {{10,20,30},{15,25,35},{18,28,38}} and key=18:
        // moves down twice then left twice to locate element at [2,0]
        Integer[][] matrix = new Integer[][] {
            { 10, 20, 30 },
            { 15, 25, 35 },
            { 18, 28, 38 }
        };
        Integer key = 18;
        // WHEN
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, key);
        // THEN
        assertArrayEquals(new int[] { 2, 0 }, result);
    }

    @Test
    @DisplayName("TC09: Null matrix input throws NullPointerException immediately")
    void test_TC09() {
        // GIVEN a null matrix => accessing matrix.length throws NullPointerException at start
        Integer[][] matrix = null;
        Integer key = 1;
        // WHEN & THEN
        assertThrows(NullPointerException.class, () -> {
            RowColumnWiseSorted2dArrayBinarySearch.search(matrix, key);
        });
    }
}