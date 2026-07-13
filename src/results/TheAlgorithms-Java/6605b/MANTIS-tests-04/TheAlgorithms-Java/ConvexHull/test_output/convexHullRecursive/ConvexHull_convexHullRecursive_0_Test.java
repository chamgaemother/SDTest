package com.thealgorithms.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class ConvexHull_convexHullRecursive_0_Test {

    @Test
    @DisplayName("convexHullRecursive(null) throws NullPointerException for null input")
    public void test_TC01() {
        // GIVEN a null list, WHEN invoking convexHullRecursive, THEN expect NullPointerException
        List<Point> points = null;
        assertThrows(NullPointerException.class, () -> ConvexHull.convexHullRecursive(points));
    }

    @Test
    @DisplayName("convexHullRecursive(empty) throws IndexOutOfBoundsException for list size < 2")
    public void test_TC02() {
        // GIVEN an empty list (size 0), WHEN invoking convexHullRecursive, THEN expect IndexOutOfBoundsException
        List<Point> points = new ArrayList<>();
        assertThrows(IndexOutOfBoundsException.class, () -> ConvexHull.convexHullRecursive(points));
    }

    @Test
    @DisplayName("convexHullRecursive(twoPoints) returns both points without recursion (loop-0)")
    public void test_TC03() {
        // GIVEN exactly two points, no recursion path (loop-0)
        Point p1 = new Point(0, 0);
        Point p2 = new Point(1, 1);
        List<Point> points = Arrays.asList(p2, p1); // unsorted input
        // WHEN
        List<Point> hull = ConvexHull.convexHullRecursive(new ArrayList<>(points));
        // THEN sorted result should be [p1, p2]
        assertEquals(Arrays.asList(p1, p2), hull);
    }

    @Test
    @DisplayName("convexHullRecursive(threeNonCollinear) returns all three hull points (one interior, loop-1)")
    public void test_TC04() {
        // GIVEN three non-collinear points: interior on left of line
        Point leftMost = new Point(0, 0);
        Point interior = new Point(1, 1);
        Point rightMost = new Point(2, 0);
        List<Point> points = Arrays.asList(leftMost, interior, rightMost);
        // interior yields positive orientation -> goes to upperHull (branch-true)
        // WHEN
        List<Point> hull = ConvexHull.convexHullRecursive(new ArrayList<>(points));
        // THEN all three points are part of convex hull
        assertEquals(Arrays.asList(leftMost, interior, rightMost), hull);
    }

    @Test
    @DisplayName("convexHullRecursive(threeCollinear) returns only endpoints when interior is collinear (loop-1)")
    public void test_TC05() {
        // GIVEN three collinear points: interior exactly on the line (orientation == 0)
        Point leftMost = new Point(0, 0);
        Point midCollinear = new Point(1, 0);
        Point rightMost = new Point(2, 0);
        List<Point> points = Arrays.asList(leftMost, midCollinear, rightMost);
        // midCollinear yields det == 0 -> excluded from both upper and lower hulls
        // WHEN
        List<Point> hull = ConvexHull.convexHullRecursive(new ArrayList<>(points));
        // THEN only endpoints remain
        assertEquals(Arrays.asList(leftMost, rightMost), hull);
    }

    @Test
    @DisplayName("convexHullRecursive(squarePoints) returns four corners for multiple interior candidates (loop-N)")
    public void test_TC06() {
        // GIVEN a square with two interior points
        Point A = new Point(0, 0);
        Point B = new Point(0, 1);
        Point C = new Point(1, 1);
        Point D = new Point(1, 0);
        Point center1 = new Point(1, 1); // using duplicate to represent interior; orientation negative for lower
        Point center2 = new Point(0, 1); // duplicate for upper
        List<Point> points = Arrays.asList(C, B, D, A, center1, center2);
        // Some points go to upperHull, some to lowerHull, triggering multiple recursion calls
        // WHEN
        List<Point> hull = ConvexHull.convexHullRecursive(new ArrayList<>(points));
        // THEN only the four corners sorted remain
        List<Point> expected = Arrays.asList(A, B, D, C);
        assertEquals(expected, hull);
    }
}