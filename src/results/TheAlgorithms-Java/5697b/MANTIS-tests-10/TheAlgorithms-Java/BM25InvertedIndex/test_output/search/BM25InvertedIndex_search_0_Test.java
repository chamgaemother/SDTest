package com.thealgorithms.searches;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
public class BM25InvertedIndex_search_0_Test {

    @Test
    @DisplayName("TC01: search(nonexistent) returns empty list when index contains no entry for term (branch-false at B0)")
    public void test_TC01() {
        // No movies added, so index does not contain "ghost" -> B0 false
        BM25InvertedIndex idx = new BM25InvertedIndex();
        List<SearchResult> results = idx.search("ghost");
        assertTrue(results.isEmpty(), "Expected empty result list for nonexistent term");
    }

    @Test
    @DisplayName("TC02: search(term) returns single result for one document (branch-true at B0, one loop iteration)")
    public void test_TC02() {
        // Add one movie containing "hero" -> index contains term, B0 true, one iteration
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "Hero Movie", 8.0, 2020, "A hero story");
        List<SearchResult> results = idx.search("hero");
        assertEquals(1, results.size(), "Should find exactly one SearchResult");
        SearchResult res = results.get(0);
        assertEquals(1, res.getDocId(), "Document ID must match the added movie");
        assertTrue(res.getRelevanceScore() > 0, "Relevance score should be positive for single occurrence");
    }

    @Test
    @DisplayName("TC03: search(term) orders multiple documents by descending score (branch-true at B0, multiple loop iterations)")
    public void test_TC03() {
        // Two movies with different frequencies of "love"; higher freq yields higher score -> ordering test
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "Love Story", 7.5, 1990, "love love");
        idx.addMovie(2, "Romantic Love", 8.5, 2000, "love love love");
        List<SearchResult> results = idx.search("love");
        assertEquals(2, results.size(), "Should return two results for term present in two docs");
        SearchResult first = results.get(0);
        SearchResult second = results.get(1);
        assertEquals(2, first.getDocId(), "First result should be docId=2 with higher freq");
        assertTrue(first.getRelevanceScore() > second.getRelevanceScore(),
                "First score must exceed second score for descending order");
    }

    @Test
    @DisplayName("TC04: search(term) skips entries when movie record is null (branch-true at B0 and branch-false at B3)")
    public void test_TC04() throws Exception {
        // Add a movie then remove it from internal movies map to simulate null movie -> skip at B3
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "Ghost", 5.0, 2010, "spooky");
        // Reflectively remove the movie record
        Field moviesField = BM25InvertedIndex.class.getDeclaredField("movies");
        moviesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, Movie> moviesMap = (Map<Integer, Movie>) moviesField.get(idx);
        moviesMap.remove(1);
        List<SearchResult> results = idx.search("ghost");
        assertTrue(results.isEmpty(), "Expected no results after movie record removal");
    }

    @Test
    @DisplayName("TC05: search(term) yields negative IDF when term appears in all documents (boundary at computeIDF)")
    public void test_TC05() {
        // Two movies both containing "all" -> IDF negative, both scores negative, ordered by freq
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "All In", 6.0, 2015, "all all");   // frequency 2
        idx.addMovie(2, "All Out", 7.0, 2018, "all all all"); // frequency 3
        List<SearchResult> results = idx.search("all");
        assertEquals(2, results.size(), "Should return two results for 'all'");
        SearchResult first = results.get(0);
        SearchResult second = results.get(1);
        // Both relevance scores should be negative due to IDF < 0
        assertTrue(first.getRelevanceScore() < 0, "Expected negative relevance score for first");
        assertTrue(second.getRelevanceScore() < 0, "Expected negative relevance score for second");
        // Higher term frequency should still sort first despite negative values
        assertEquals(2, first.getDocId(), "DocId=2 has higher term frequency and should come first");
    }
}