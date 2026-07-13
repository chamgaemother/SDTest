package com.thealgorithms.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class ConvexHull_convexHullRecursive_0_Test {

    @Test
    @DisplayName("convexHullRecursive([]) throws IndexOutOfBoundsException when input list is empty")
    void test_TC01() {
        // GIVEN an empty list
        List<Point> points = new ArrayList<>();
        // WHEN & THEN calling convexHullRecursive should fail at accessing points.get(0)
        assertThrows(IndexOutOfBoundsException.class, () -> ConvexHull.convexHullRecursive(points));
    }

    @Test
    @DisplayName("convexHullRecursive([P]) returns the single point when list size is 1")
    void test_TC02() {
        // GIVEN points = [P] of size 1, loop from i=1 to size-1 never executes (loop-0), direct return
        Point p = new Point(0, 0);
        List<Point> points = new ArrayList<>(Collections.singletonList(p));
        // WHEN
        List<Point> result = ConvexHull.convexHullRecursive(points);
        // THEN exactly the same single point is returned
        assertEquals(1, result.size());
        assertEquals(p, result.get(0));
    }

    @Test
    @DisplayName("convexHullRecursive([P1,P2]) returns both points in sorted order when list size is 2")
    void test_TC03() {
        // GIVEN two points P1=(1,1) and P2=(0,0); sorted order is P2, P1 (loop-0, branch-true)
        Point p1 = new Point(1, 1);
        Point p2 = new Point(0, 0);
        List<Point> points = new ArrayList<>(Arrays.asList(p1, p2));
        // WHEN
        List<Point> result = ConvexHull.convexHullRecursive(points);
        // THEN returns sorted list [p2, p1]
        assertEquals(2, result.size());
        assertEquals(p2, result.get(0));
        assertEquals(p1, result.get(1));
    }

    @Test
    @DisplayName("convexHullRecursive(colinear three points) returns only the endpoints for colinear input")
    void test_TC04() {
        // GIVEN three colinear points P1(0,0) < P2(1,1) < P3(2,2): for each interior point det==0 => filtered out
        Point p1 = new Point(0, 0);
        Point p2 = new Point(1, 1);
        Point p3 = new Point(2, 2);
        List<Point> points = new ArrayList<>(Arrays.asList(p1, p2, p3));
        // WHEN
        List<Point> result = ConvexHull.convexHullRecursive(points);
        // THEN only endpoints remain [p1, p3]
        assertEquals(2, result.size());
        assertEquals(p1, result.get(0));
        assertEquals(p3, result.get(1));
    }

    @Test
    @DisplayName("convexHullRecursive(triangle) returns all three vertices for a non‐colinear three‐point set")
    void test_TC05() {
        // GIVEN triangle points A(0,0), B(1,0), C(0,1): non‐colinear yields det != 0 for at least one branch
        Point a = new Point(0, 0);
        Point b = new Point(1, 0);
        Point c = new Point(0, 1);
        List<Point> points = new ArrayList<>(Arrays.asList(a, b, c));
        // WHEN
        List<Point> result = ConvexHull.convexHullRecursive(points);
        // THEN all three sorted: (0,0),(0,1),(1,0)
        List<Point> expected = Arrays.asList(a, c, b);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("convexHullRecursive(four corners of square) returns all four corners for a convex quadrilateral")
    void test_TC06() {
        // GIVEN square corners SW(0,0), SE(1,0), NE(1,1), NW(0,1): multiple branches det>0 and det<0
        Point sw = new Point(0, 0);
        Point se = new Point(1, 0);
        Point ne = new Point(1, 1);
        Point nw = new Point(0, 1);
        List<Point> points = new ArrayList<>(Arrays.asList(sw, se, ne, nw));
        // WHEN
        List<Point> result = ConvexHull.convexHullRecursive(points);
        // THEN all four sorted: (0,0),(0,1),(1,0),(1,1)
        List<Point> expected = Arrays.asList(sw, nw, se, ne);
        assertEquals(expected, result);
    }
}