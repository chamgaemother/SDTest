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
    @DisplayName("search(term) returns empty list when term not in index (B0 branch-false)")
    void test_TC01() {
        // GIVEN: fresh index with no movies => index.containsKey(term) is false (B0 branch-false)
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // WHEN: search for a term not present
        List<SearchResult> results = idx.search("unknown");
        // THEN: should return empty list
        assertTrue(results.isEmpty(), "Expected empty results when term not in index");
    }

    @Test
    @DisplayName("search(term) returns single result when one document contains term (B0 branch-true, loop-1, movie!=null)")
    void test_TC02() {
        // GIVEN: index with one movie containing term "apple" => branch B0 true, one iteration, movie exists
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "ApplePie", 8.0, 2020, "apple fruit");
        // WHEN: search for "apple"
        List<SearchResult> results = idx.search("apple");
        // THEN: exactly one result and correct docId
        assertEquals(1, results.size(), "Expected one search result for term present in one document");
        assertEquals(1, results.get(0).getDocId(), "Expected result docId to match the added movie");
    }

    @Test
    @DisplayName("search(term) skips index entries when movie missing in movies map (B0 branch-true, loop-1, movie==null)")
    void test_TC03() throws Exception {
        // GIVEN: manually inject an index entry for term "ghost" with docId 99, but do not add movie => movie == null branch
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // reflectively access private field 'index'
        Field indexField = BM25InvertedIndex.class.getDeclaredField("index");
        indexField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Map<Integer, Integer>> indexMap = (Map<String, Map<Integer, Integer>>) indexField.get(idx);
        Map<Integer, Integer> postings = new HashMap<>();
        postings.put(99, 1);
        indexMap.put("ghost", postings);
        // Ensure 'movies' map does not contain key 99 (default state)
        // WHEN: search for "ghost"
        List<SearchResult> results = idx.search("ghost");
        // THEN: skip null movie entry and return empty list
        assertTrue(results.isEmpty(), "Expected empty results when movie entry is missing in movies map");
    }

    @Test
    @DisplayName("search(term) returns sorted results for multiple documents (B0 branch-true, loop-N>1, movie!=null)")
    void test_TC04() {
        // GIVEN: two movies with different term frequencies for "star" => loop over two entries, movies exist
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // doc1 has frequency 2
        idx.addMovie(1, "Star Wars", 9.0, 1977, "star star");
        // doc2 has frequency 1
        idx.addMovie(2, "Star Trek", 8.5, 1979, "star");
        // WHEN: search for "star"
        List<SearchResult> results = idx.search("star");
        // THEN: two results, and first has >= relevance score than second
        assertEquals(2, results.size(), "Expected two results for term in two documents");
        double score0 = results.get(0).getRelevanceScore();
        double score1 = results.get(1).getRelevanceScore();
        assertTrue(score0 >= score1,
            String.format("Expected first result score (%.4f) >= second (%.4f)", score0, score1));
    }

    @Test
    @DisplayName("search(null) throws NullPointerException due to term.toLowerCase()")
    void test_TC05() {
        // GIVEN: any BM25InvertedIndex instance
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // WHEN/THEN: search(null) invokes term.toLowerCase() and should throw NullPointerException
        assertThrows(NullPointerException.class, () -> idx.search(null),
            "Expected NullPointerException when searching with null term");
    }
}