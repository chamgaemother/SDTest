package com.thealgorithms.searches;

import com.thealgorithms.searches.BM25InvertedIndex;
import com.thealgorithms.searches.SearchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
public class BM25InvertedIndex_search_0_Test {

    @Test
    @DisplayName("TC01: search(term) returns empty list when index does not contain the term (B0 false)")
    void test_TC01() {
        // GIVEN a fresh index with no movies added => index.containsKey(term) is false, so B0 false
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // WHEN searching for a term not in index
        List<SearchResult> results = idx.search("foobar");
        // THEN the result list must be empty
        assertTrue(results.isEmpty(), "Expected empty result list when term is missing in index");
    }

    @Test
    @DisplayName("TC02: search(term) returns single result for one document containing term (B0 true, loop-1, movie non-null)")
    void test_TC02() {
        // GIVEN an index with one movie whose name contains 'hello' => B0 true, loop executes once, movie != null
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "Hello World", 8.0, 2000, "");
        // WHEN searching for 'hello'
        List<SearchResult> results = idx.search("hello");
        // THEN exactly one result with docId=1
        assertAll(
            () -> assertEquals(1, results.size(), "Expected exactly one result for term 'hello'."),
            () -> assertEquals(1, results.get(0).getDocId(), "Expected the document ID to be 1.")
        );
    }

    @Test
    @DisplayName("TC03: search(term) skips entries with missing Movie object (movie==null branch-false)")
    void test_TC03() throws Exception {
        // GIVEN an index stubbed with a term mapping to a docId without adding the movie => movie == null branch
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // Use reflection to access and modify private 'index' field
        Field indexField = BM25InvertedIndex.class.getDeclaredField("index");
        indexField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Map<Integer, Integer>> indexMap = (Map<String, Map<Integer, Integer>>) indexField.get(idx);
        // stub inner map with a docId that has no corresponding Movie in 'movies'
        Map<Integer, Integer> inner = new HashMap<>();
        inner.put(42, 1);
        indexMap.put("ghost", inner);
        // WHEN searching for 'ghost'
        List<SearchResult> results = idx.search("ghost");
        // THEN the result list must be empty because movie==null entries are skipped
        assertTrue(results.isEmpty(), "Expected empty result list when the indexed movie is missing.");
    }

    @Test
    @DisplayName("TC04: search(term) returns multiple results sorted by descending score (loop-N, branch-true)")
    void test_TC04() {
        // GIVEN an index with two movies both containing 'test' but with different lengths => two iterations, both movie != null
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "Test One", 7.0, 2010, "alpha beta gamma");  // longer content
        idx.addMovie(2, "Test Two", 9.0, 2015, "alpha beta");        // shorter content
        // WHEN searching for 'test'
        List<SearchResult> results = idx.search("test");
        // THEN we get two results sorted by descending relevanceScore
        assertAll(
            () -> assertEquals(2, results.size(), "Expected two results for term 'test'."),
            () -> assertTrue(
                    results.get(0).getRelevanceScore() >= results.get(1).getRelevanceScore(),
                    "Expected results sorted by descending relevanceScore."),
            () -> assertEquals(1, results.get(0).getDocId(), "Expected the top result to be the one with the higher score, docId=1 or 2 depending on score.")
        );
    }
}