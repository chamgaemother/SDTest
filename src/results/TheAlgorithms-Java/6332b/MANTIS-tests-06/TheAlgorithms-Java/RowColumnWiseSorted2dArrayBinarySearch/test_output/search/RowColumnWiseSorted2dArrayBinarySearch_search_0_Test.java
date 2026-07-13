package com.thealgorithms.searches;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class RowColumnWiseSorted2dArrayBinarySearch_search_0_Test {

    @Test
    @DisplayName("TC01: Empty matrix returns [-1,-1] without entering loop (matrix.length=0)")
    void test_TC01() {
        // Given an empty matrix, matrix.length == 0 triggers immediate exit (B9 true)
        Integer[][] matrix = new Integer[0][];
        Integer target = 5;
        // When
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        // Then
        assertArrayEquals(new int[]{-1, -1}, result, "Empty matrix should return [-1, -1]");
    }

    @Test
    @DisplayName("TC02: Single-element match returns [0,0] on first comparison")
    void test_TC02() {
        // Given a 1x1 matrix where element equals target, so comp==0 at first check (B2 true)
        Integer[][] matrix = new Integer[][] {{42}};
        Integer target = 42;
        // When
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        // Then
        assertArrayEquals(new int[]{0, 0}, result, "Single-element match should locate at [0,0]");
    }

    @Test
    @DisplayName("TC03: Single-element lower-than-target returns [-1,-1] after one row increment")
    void test_TC03() {
        // Given a 1x1 matrix where matrix[0][0] < target, so comp>0 false? Actually target.compareTo(matrix)<0 -> comp>0? Wait: comp=target.compareTo(mat)<0? No, target>mat => comp>0, follows rowPointer++ (B5), then loop ends by B9
        Integer[][] matrix = new Integer[][] {{10}};
        Integer target = 20;
        // When
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        // Then
        assertArrayEquals(new int[]{-1, -1}, result, "After one row increment and out-of-bounds, should return [-1,-1]");
    }

    @Test
    @DisplayName("TC04: Single-element greater-than-target returns [-1,-1] after one column decrement")
    void test_TC04() {
        // Given a 1x1 matrix where matrix[0][0] > target, so comp<0, triggers colPointer-- (B6), then B9->B1 sees col<0
        Integer[][] matrix = new Integer[][] {{30}};
        Integer target = 20;
        // When
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        // Then
        assertArrayEquals(new int[]{-1, -1}, result, "After one column decrement and col<0, should return [-1,-1]");
    }

    @Test
    @DisplayName("TC05: Match found in second row after one row increment and one column decrement")
    void test_TC05() {
        // Given a 2x2 matrix {{1,2},{3,4}}, target=3. First compare at [0,1]: 2<3 comp>0 row++ to 1; then at [1,1]:4>3 comp<0 col-- to 0; then at [1,0]: equals target
        Integer[][] matrix = new Integer[][] {{1, 2}, {3, 4}};
        Integer target = 3;
        // When
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        // Then
        assertArrayEquals(new int[]{1, 0}, result, "Should find 3 at position [1,0] after two moves");
    }

    @Test
    @DisplayName("TC06: No match in 2×2 matrix returns [-1,-1] after pointers cross")
    void test_TC06() {
        // Given a 2x2 matrix {{1,2},{3,4}}, target=5. Moves: [0,1](2<5)->row1; [1,1](4<5)->row2 -> rowPointer>=length -> exit
        Integer[][] matrix = new Integer[][] {{1, 2}, {3, 4}};
        Integer target = 5;
        // When
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        // Then
        assertArrayEquals(new int[]{-1, -1}, result, "Pointers cross without match, should return [-1,-1]");
    }

    @Test
    @DisplayName("TC07: Null matrix input throws NullPointerException immediately")
    void test_TC07() {
        // Given a null matrix, invocation should throw NullPointerException before any logic
        Integer[][] matrix = null;
        Integer target = 1;
        // When & Then
        assertThrows(NullPointerException.class,
            () -> RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target),
            "Null matrix should throw NullPointerException");
    }

    @Test
    @DisplayName("TC08: Matrix with null row throws NullPointerException on access")
    void test_TC08() {
        // Given a matrix with one null row; matrix.length>0 so enters loop then matrix[0] is null causing NPE
        Integer[][] matrix = new Integer[1][]; // row is null
        Integer target = 1;
        // When & Then
        assertThrows(NullPointerException.class,
            () -> RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target),
            "Null row should throw NullPointerException on access");
    }

    @Test
    @DisplayName("TC09: Matrix with null element throws NullPointerException when comparing")
    void test_TC09() {
        // Given a matrix containing null element at [0][0], accessing element but comparing target.compareTo(null) throws NPE
        Integer[][] matrix = new Integer[][] {{null}};
        Integer target = 1;
        // When & Then
        assertThrows(NullPointerException.class,
            () -> RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target),
            "Null element should throw NullPointerException during comparison");
    }

    @Test
    @DisplayName("TC10: Generic type Double works identically, find at [0,1]")
    void test_TC10() {
        // Given a 2x2 Double matrix where target 2.2 is at [0,1], direct match on first iteration (at B2 true)
        Double[][] matrix = new Double[][] {{1.1, 2.2}, {3.3, 4.4}};
        Double target = 2.2;
        // When
        int[] result = RowColumnWiseSorted2dArrayBinarySearch.search(matrix, target);
        // Then
        assertArrayEquals(new int[]{0, 1}, result, "Double matrix search should find 2.2 at [0,1]");
    }
}