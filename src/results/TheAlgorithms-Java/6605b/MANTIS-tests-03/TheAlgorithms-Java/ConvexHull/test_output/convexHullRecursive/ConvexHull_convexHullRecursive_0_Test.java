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
    @DisplayName("convexHullRecursive(null) throws NullPointerException when input list is null")
    void test_TC01() {
        // GIVEN: a null list should trigger NPE immediately
        List<Point> points = null;
        // WHEN & THEN: calling with null should throw NullPointerException
        assertThrows(NullPointerException.class, () -> ConvexHull.convexHullRecursive(points));
    }

    @Test
    @DisplayName("convexHullRecursive(emptyList) throws IndexOutOfBoundsException for size<2")
    void test_TC02() {
        // GIVEN: an empty list has size 0, so accessing elements 0 and size-1 should fail
        List<Point> points = new ArrayList<>();
        // WHEN & THEN: calling should throw IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> ConvexHull.convexHullRecursive(points));
    }

    @Test
    @DisplayName("convexHullRecursive(singleton) throws IndexOutOfBoundsException for size==1")
    void test_TC03() {
        // GIVEN: a singleton list size==1 still cannot get two endpoints
        List<Point> points = Collections.singletonList(new Point(0, 0));
        // WHEN & THEN: calling should throw IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> ConvexHull.convexHullRecursive(points));
    }

    @Test
    @DisplayName("convexHullRecursive(twoPoints) returns both points sorted")
    void test_TC04() {
        // GIVEN: exactly two points, loop over interior doesn't run (size-2 == 0)
        Point p1 = new Point(0, 0);
        Point p2 = new Point(1, 1);
        List<Point> points = new ArrayList<>(Arrays.asList(p2, p1)); // unsorted input
        // WHEN: compute hull
        List<Point> hull = ConvexHull.convexHullRecursive(points);
        // THEN: both endpoints returned sorted by natural order
        assertEquals(Arrays.asList(p1, p2), hull);
    }

    @Test
    @DisplayName("convexHullRecursive(triangle non-colinear) returns all three vertices")
    void test_TC05() {
        // GIVEN: three non-colinear points produce a triangle, upperHull and lowerHull each non-empty
        Point a = new Point(0, 0);
        Point b = new Point(2, 0);
        Point c = new Point(1, 1);
        List<Point> points = new ArrayList<>(Arrays.asList(a, b, c));
        // WHEN: compute hull
        List<Point> hull = ConvexHull.convexHullRecursive(points);
        // THEN: all three vertices appear, sorted
        assertEquals(Arrays.asList(a, b, c), hull);
    }

    @Test
    @DisplayName("convexHullRecursive(three colinear) returns all three vertices")
    void test_TC06() {
        // GIVEN: three colinear points but none is considered strictly interior by orientation==0 and compareTo logic allows all
        Point a = new Point(0, 0);
        Point b = new Point(1, 0);
        Point c = new Point(2, 0);
        List<Point> points = new ArrayList<>(Arrays.asList(a, b, c));
        // WHEN: compute hull
        List<Point> hull = ConvexHull.convexHullRecursive(points);
        // THEN: all three colinear points retained, sorted
        assertEquals(Arrays.asList(a, b, c), hull);
    }

    @Test
    @DisplayName("convexHullRecursive(collinear interior) excludes point inside edge and returns endpoints only")
    void test_TC07() {
        // GIVEN: three colinear where middle point is interior; intended logic should exclude strict interior
        Point a = new Point(0, 0);
        Point b = new Point(1, 0);
        Point c = new Point(2, 0);
        List<Point> points = new ArrayList<>(Arrays.asList(a, b, c));
        // WHEN: compute hull
        List<Point> hull = ConvexHull.convexHullRecursive(points);
        // THEN: only extreme endpoints returned
        assertEquals(Arrays.asList(a, c), hull);
    }

    @Test
    @DisplayName("convexHullRecursive(square with center) returns four corner vertices")
    void test_TC08() {
        // GIVEN: square corners and one interior center; center should be discarded in hull computation
        Point p0 = new Point(0, 0);
        Point p1 = new Point(0, 2);
        Point p2 = new Point(2, 2);
        Point p3 = new Point(2, 0);
        Point center = new Point(1, 1);
        List<Point> points = new ArrayList<>(Arrays.asList(p0, p1, p2, p3, center));
        // WHEN: compute hull
        List<Point> hull = ConvexHull.convexHullRecursive(points);
        // THEN: only four corner points, sorted
        assertEquals(Arrays.asList(p0, p1, p3, p2), hull);
    }

    @Test
    @DisplayName("convexHullRecursive(unsorted input) sorts input then computes hull")
    void test_TC09() {
        // GIVEN: unsorted input of three points making a right triangle
        Point p1 = new Point(2, 2);
        Point p2 = new Point(0, 0);
        Point p3 = new Point(2, 0);
        List<Point> points = new ArrayList<>(Arrays.asList(p1, p2, p3));
        // WHEN: compute hull
        List<Point> hull = ConvexHull.convexHullRecursive(points);
        // THEN: sorted hull of the three points
        assertEquals(Arrays.asList(p2, p3, p1), hull);
    }

    @Test
    @DisplayName("convexHullRecursive(duplicate points) returns unique hull vertices")
    void test_TC10() {
        // GIVEN: input contains duplicates; duplicates should be removed in final hull
        Point p0 = new Point(0, 0);
        Point p1 = new Point(1, 1);
        Point p2 = new Point(0, 0); // duplicate of p0
        Point p3 = new Point(2, 0);
        List<Point> points = new ArrayList<>(Arrays.asList(p0, p1, p2, p3));
        // WHEN: compute hull
        List<Point> hull = ConvexHull.convexHullRecursive(points);
        // THEN: only unique extreme points returned, sorted
        assertEquals(Arrays.asList(p0, p3), hull);
    }
}