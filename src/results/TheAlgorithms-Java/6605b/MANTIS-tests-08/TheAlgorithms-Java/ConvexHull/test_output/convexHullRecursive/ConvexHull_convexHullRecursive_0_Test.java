package com.thealgorithms.geometry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
public class ConvexHull_convexHullRecursive_0_Test {

    @Test
    @DisplayName("convexHullRecursive(null) throws NullPointerException when input list is null")
    public void test_TC01() {
        // Scenario: points == null should immediately throw NPE at entry
        List<Point> points = null;
        assertThrows(NullPointerException.class, () -> ConvexHull.convexHullRecursive(points));
    }

    @Test
    @DisplayName("convexHullRecursive(empty) throws IndexOutOfBoundsException when list is empty")
    public void test_TC02() {
        // Scenario: empty list size 0, attempting get(0) causes IndexOutOfBoundsException
        List<Point> points = new ArrayList<>();
        assertThrows(IndexOutOfBoundsException.class, () -> ConvexHull.convexHullRecursive(points));
    }

    @Test
    @DisplayName("convexHullRecursive(singleton) returns the same single point for size==1")
    public void test_TC03() {
        // Scenario: list has one point, loops skip, returns same point
        Point p = new Point(0, 0);
        List<Point> points = new ArrayList<>(Arrays.asList(p));
        List<Point> result = ConvexHull.convexHullRecursive(points);
        assertAll(
            () -> assertEquals(1, result.size(), "Result size should be 1 for singleton list"),
            () -> assertEquals(p, result.get(0), "Result should contain the original point")
        );
    }

    @Test
    @DisplayName("convexHullRecursive(twoPoints) returns both points sorted for size==2")
    public void test_TC04() {
        // Scenario: two points unsorted, after sort should be (0,0), (1,1)
        Point p1 = new Point(1, 1);
        Point p2 = new Point(0, 0);
        List<Point> points = new ArrayList<>(Arrays.asList(p1, p2));
        List<Point> result = ConvexHull.convexHullRecursive(points);
        assertAll(
            () -> assertEquals(2, result.size(), "Result size should be 2 for two-point list"),
            () -> assertEquals(p2, result.get(0), "First point should be the smaller (0,0)"),
            () -> assertEquals(p1, result.get(1), "Second point should be the larger (1,1)")
        );
    }

    @Test
    @DisplayName("convexHullRecursive(triangle) returns all three points for non-colinear triple")
    public void test_TC05() {
        // Scenario: three non-colinear points form a triangle, hull contains all three
        Point p1 = new Point(0, 0);
        Point p2 = new Point(1, 0);
        Point p3 = new Point(0, 1);
        List<Point> points = new ArrayList<>(Arrays.asList(p1, p2, p3));
        List<Point> result = ConvexHull.convexHullRecursive(points);
        assertAll(
            () -> assertEquals(3, result.size(), "Triangle hull should contain 3 points"),
            () -> assertTrue(result.containsAll(Arrays.asList(p1, p2, p3)), "Result should contain all input points")
        );
    }

    @Test
    @DisplayName("convexHullRecursive(colinear) returns only endpoints for three colinear points")
    public void test_TC06() {
        // Scenario: three colinear points, only endpoints should be in hull
        Point p1 = new Point(0, 0);
        Point p2 = new Point(1, 1);
        Point p3 = new Point(2, 2);
        List<Point> points = new ArrayList<>(Arrays.asList(p1, p2, p3));
        List<Point> result = ConvexHull.convexHullRecursive(points);
        assertAll(
            () -> assertEquals(2, result.size(), "Colinear triple hull should contain only 2 endpoints"),
            () -> assertEquals(p1, result.get(0), "First endpoint should be the smallest point"),
            () -> assertEquals(p3, result.get(1), "Second endpoint should be the largest point")
        );
    }

    @Test
    @DisplayName("convexHullRecursive(mixed) returns hull excluding interior point for four points")
    public void test_TC07() {
        // Scenario: four points with one interior, hull should exclude interior point
        Point p1 = new Point(0, 0);
        Point p2 = new Point(2, 0);
        Point p3 = new Point(1, 2);
        Point interior = new Point(1, 1);
        List<Point> points = new ArrayList<>(Arrays.asList(p1, p2, p3, interior));
        List<Point> result = ConvexHull.convexHullRecursive(points);
        assertAll(
            () -> assertEquals(3, result.size(), "Hull of four points with interior should have 3 vertices"),
            () -> assertFalse(result.contains(interior), "Interior point should not be in hull"),
            () -> assertTrue(result.containsAll(Arrays.asList(p1, p2, p3)), "Hull should contain the outer triangle points")
        );
    }
}