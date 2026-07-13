package com.thealgorithms.searches;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
public class BM25InvertedIndex_search_0_Test {

    @Test
    @DisplayName("search('unknown') returns empty list when term not in index (B0 false)")
    void test_TC01() {
        // Given a fresh index with no entries, B0 condition (term exists) is false
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // When searching for a term not in the index
        List<SearchResult> results = idx.search("unknown");
        // Then the result list should be empty
        assertTrue(results.isEmpty(), "Expected no search results for unknown term");
    }

    @Test
    @DisplayName("search(null) throws NullPointerException during normalization")
    void test_TC02() {
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // Passing null should throw NPE when calling toLowerCase()
        assertThrows(NullPointerException.class, () -> idx.search(null));
    }

    @Test
    @DisplayName("search('term') returns one result when exactly one movie contains the term (B0 true, loop-1, branch-true)")
    void test_TC03() {
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // Add a movie containing 'term' once => B0 true and loop enters once, movie != null branch
        idx.addMovie(1, "Term Movie", 8.0, 2020, "term appears here");
        List<SearchResult> results = idx.search("term");
        // Expect exactly one result and that its docId is 1
        assertEquals(1, results.size(), "Expected one search result for 'term'");
        assertEquals(1, results.get(0).getDocId(), "Expected result docId to be 1");
    }

    @Test
    @DisplayName("search('a') returns two results sorted by descending score (loop-N with N=2)")
    void test_TC04() {
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // Add two movies each containing 'a', to produce two iterations in loop
        idx.addMovie(1, "A a a", 7.0, 2019, "a");    // movie1 term frequency higher\n        idx.addMovie(2, "A", 9.0, 2021, "a a");      // movie2 term frequency medium
        List<SearchResult> results = idx.search("a");
        // Expect two results sorted by descending BM25 score: docId 2 should rank higher than 1
        assertEquals(2, results.size(), "Expected two results for 'a'");
        assertEquals(2, results.get(0).getDocId(), "Expected first result docId to be 2");
        assertEquals(1, results.get(1).getDocId(), "Expected second result docId to be 1");
    }

    @Test
    @DisplayName("search('term') skips entries when movie is missing (branch-false in B3, loop-1 yields empty)")
    void test_TC05() throws Exception {
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "Term", 8.5, 2018, "term");
        // Use reflection to remove the Movie from the private movies map to simulate missing movie => branch-false
        Field moviesField = BM25InvertedIndex.class.getDeclaredField("movies");
        moviesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, Movie> movies = (Map<Integer, Movie>) moviesField.get(idx);
        movies.remove(1);
        // Now search should skip the null movie and return empty
        List<SearchResult> results = idx.search("term");
        assertTrue(results.isEmpty(), "Expected empty results when movie entry is missing");
    }

    @Test
    @DisplayName("search('empty') returns empty when index contains term but no docs (loop-0 after B0 true)")
    void test_TC06() throws Exception {
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // Populate private index map reflectively to have term 'empty' with no documents => B0 true, loop 0
        Field indexField = BM25InvertedIndex.class.getDeclaredField("index");
        indexField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Map<Integer, Integer>> indexMap = (Map<String, Map<Integer, Integer>>) indexField.get(idx);
        indexMap.put("empty", new HashMap<>());
        // Search should find the term key but no documents => empty result
        List<SearchResult> results = idx.search("empty");
        assertTrue(results.isEmpty(), "Expected empty results for term with no associated docs");
    }
}