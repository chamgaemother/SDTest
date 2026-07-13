package com.thealgorithms.geometry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class ConvexHull_convexHullRecursive_1_Test {

    @Test
    @DisplayName("convexHullRecursive with all interior points strictly left of baseline exercises only upperHull recursion path")
    void test_TC08() {
        // All interior points lie above the baseline from (0,0) to (4,0): det > 0 for each
        List<Point> points = Arrays.asList(
            new Point(0, 0),
            new Point(1, 2),
            new Point(2, 3),
            new Point(3, 2),
            new Point(4, 0)
        );
        List<Point> result = ConvexHull.convexHullRecursive(points);
        // Expect endpoints and intermediate extreme points, sorted in natural order
        assertAll(
            () -> assertTrue(result.size() > 2, "Hull should contain more than two points"),
            () -> assertEquals(new Point(0, 0), result.get(0), "First point must be the leftmost endpoint"),
            () -> assertEquals(new Point(4, 0), result.get(result.size() - 1), "Last point must be the rightmost endpoint")
        );
    }

    @Test
    @DisplayName("convexHullRecursive with all interior points strictly right of baseline exercises only lowerHull recursion path")
    void test_TC09() {
        // All interior points lie below the baseline from (0,0) to (3,0): det < 0 for each
        List<Point> points = Arrays.asList(
            new Point(0, 0),
            new Point(1, -2),
            new Point(2, -3),
            new Point(3, 0)
        );
        List<Point> result = ConvexHull.convexHullRecursive(points);
        // Only the two endpoints should remain on the hull
        assertAll(
            () -> assertEquals(2, result.size(), "Hull should contain exactly two endpoints"),
            () -> assertTrue(result.contains(new Point(0, 0)), "Hull must contain the leftmost endpoint"),
            () -> assertTrue(result.contains(new Point(3, 0)), "Hull must contain the rightmost endpoint")
        );
    }

    @Test
    @DisplayName("convexHullRecursive throws NullPointerException when a null element is in the list")
    void test_TC10() {
        // Introducing a null in the input list to trigger NPE during orientation computations
        List<Point> points = Arrays.asList(
            new Point(0, 0),
            null,
            new Point(1, 1)
        );
        assertThrows(NullPointerException.class,
            () -> ConvexHull.convexHullRecursive(points),
            "Expected NullPointerException when input list contains null"
        );
    }
}