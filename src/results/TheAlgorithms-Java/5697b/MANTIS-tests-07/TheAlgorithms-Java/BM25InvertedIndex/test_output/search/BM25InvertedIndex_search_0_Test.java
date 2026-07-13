package com.thealgorithms.searches;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.thealgorithms.searches.BM25InvertedIndex;
import com.thealgorithms.searches.SearchResult;

import java.lang.reflect.Field;
import java.util.*;
public class BM25InvertedIndex_search_0_Test {

    @Test
    @DisplayName("TC01: search with term not in index should return empty list (branch index.containsKey false)")
    void test_TC01() {
        // GIVEN a new BM25InvertedIndex with empty index
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // WHEN
        List<SearchResult> results = idx.search("nonexistent");
        // THEN an empty list is returned because index.containsKey is false (path B0→B1)
        assertEquals(0, results.size(), "Expected no results for term not in index");
    }

    @Test
    @DisplayName("TC02: search with term present in index but no documents mapped (branch index.containsKey true, termDocs.size 0)")
    void test_TC02() throws Exception {
        // GIVEN an index with entry "emptyTerm" → emptyMap
        BM25InvertedIndex idx = new BM25InvertedIndex();
        Field fIndex = BM25InvertedIndex.class.getDeclaredField("index");
        fIndex.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Map<Integer, Integer>> indexMap = (Map<String, Map<Integer, Integer>>) fIndex.get(idx);
        indexMap.put("emptyterm", new HashMap<>()); // normalized lowercase
        // WHEN
        List<SearchResult> results = idx.search("emptyTerm");
        // THEN empty list because loop over termDocs (size 0) never iterates (path B0→B2→B7→B6)
        assertEquals(0, results.size(), "Expected no results for term mapped to empty doc list");
    }

    @Test
    @DisplayName("TC03: search skips documents whose Movie is missing (branch movie == null)")
    void test_TC03() throws Exception {
        // GIVEN index term -> {1->1} but movies map is empty
        BM25InvertedIndex idx = new BM25InvertedIndex();
        Field fIndex = BM25InvertedIndex.class.getDeclaredField("index");
        Field fMovies = BM25InvertedIndex.class.getDeclaredField("movies");
        fIndex.setAccessible(true);
        fMovies.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Map<Integer, Integer>> indexMap = (Map<String, Map<Integer, Integer>>) fIndex.get(idx);
        @SuppressWarnings("unchecked")
        Map<Integer, Object> moviesMap = (Map<Integer, Object>) fMovies.get(idx);
        indexMap.put("term", new HashMap<>(Map.of(1, 1)));
        moviesMap.clear(); // ensure movie == null in search
        // WHEN
        List<SearchResult> results = idx.search("term");
        // THEN skip the entry because movie == null yields no SearchResult (path B0→B2→B7→B3→B4→B7→B6)
        assertEquals(0, results.size(), "Expected no results when movie is missing in movies map");
    }

    @Test
    @DisplayName("TC04: search returns single result for one matching movie")
    void test_TC04() {
        // GIVEN index built with movie(1, "A", 0.0, 2010, "term")
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "A", 0.0, 2010, "term");
        // WHEN
        List<SearchResult> results = idx.search("term");
        // THEN exactly one result with docId=1 and relevanceScore == computeIDF(1)
        assertEquals(1, results.size(), "Expected one result for single matching movie");
        SearchResult r = results.get(0);
        assertEquals(1, r.getDocId(), "Expected docId 1");
        double expectedIdf = Math.log((1 - 1 + 0.5) / (1 + 0.5));
        assertEquals(expectedIdf, r.getRelevanceScore(), 
                "Expected relevanceScore equal to IDF(docFreq=1)");
    }

    @Test
    @DisplayName("TC05: search returns sorted results for two movies by descending score")
    void test_TC05() {
        // GIVEN index built with two movies both containing "term" once
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "A", 0.0, 2010, "term");
        idx.addMovie(2, "B", 0.0, 2011, "term");
        // WHEN
        List<SearchResult> results = idx.search("term");
        // THEN two results sorted by descending relevanceScore
        assertEquals(2, results.size(), "Expected two results");
        double score0 = results.get(0).getRelevanceScore();
        double score1 = results.get(1).getRelevanceScore();
        assertTrue(score0 >= score1, "Expected results sorted in descending order of score");
        Set<Integer> docIds = Set.of(results.get(0).getDocId(), results.get(1).getDocId());
        assertEquals(Set.of(1, 2), docIds, "Expected both docIds present");
    }

    @Test
    @DisplayName("TC06: search scoring accounts for term frequency >1")
    void test_TC06() {
        // GIVEN index built with movie(1, \"\", 0.0, 2012, \"term term other\")
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "", 0.0, 2012, "term term other");
        // WHEN
        List<SearchResult> results = idx.search("term");
        // THEN one result with score > IDF(1) since TF=2 (numerator increases)
        assertEquals(1, results.size(), "Expected one result for movie with two term occurrences");
        double idf = Math.log((1 - 1 + 0.5) / (1 + 0.5));
        double score = results.get(0).getRelevanceScore();
        assertTrue(score > idf, "Expected relevanceScore > base IDF when term frequency >1");
    }

    @Test
    @DisplayName("TC07: search is case-insensitive: uppercase query matches lowercase index")
    void test_TC07() {
        // GIVEN index with movie(1, \"\", 0.0, 2013, \"Term\")
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "", 0.0, 2013, "Term");
        // WHEN using uppercase query
        List<SearchResult> results = idx.search("TERM");
        // THEN returned list size==1 and docId==1 regardless of case normalization
        assertEquals(1, results.size(), "Expected one result despite uppercase query");
        assertEquals(1, results.get(0).getDocId(), "Expected docId 1 for case-insensitive match");
    }

    @Test
    @DisplayName("TC08: search with null term should throw NullPointerException at toLowerCase")
    void test_TC08() {
        // GIVEN term = null
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // WHEN/THEN NPE thrown by term.toLowerCase() (path B0)
        assertThrows(NullPointerException.class, () -> idx.search(null),
                "Expected NullPointerException when search term is null");
    }

    @Test
    @DisplayName("TC09: search with empty string term normalizes to \"\" and returns empty list")
    void test_TC09() {
        // GIVEN a new BM25InvertedIndex and empty-string query
        BM25InvertedIndex idx = new BM25InvertedIndex();
        // WHEN
        List<SearchResult> results = idx.search("");
        // THEN index.containsKey(\"\") is false and empty list returned (path B0→B1)
        assertEquals(0, results.size(), "Expected no results for empty-string term");
    }

    @Test
    @DisplayName("TC10: search with three matching movies exercises loop with N>2 iterations")
    void test_TC10() {
        // GIVEN three movies each containing "term" exactly once
        BM25InvertedIndex idx = new BM25InvertedIndex();
        idx.addMovie(1, "", 0.0, 2020, "term");
        idx.addMovie(2, "", 0.0, 2021, "term");
        idx.addMovie(3, "", 0.0, 2022, "term");
        // WHEN
        List<SearchResult> results = idx.search("term");
        // THEN three results sorted by descending relevanceScore
        assertEquals(3, results.size(), "Expected three results for three matching movies");
        double prev = Double.MAX_VALUE;
        for (SearchResult r : results) {
            double cur = r.getRelevanceScore();
            assertTrue(prev >= cur, "Expected non-increasing order of scores");
            prev = cur;
        }
        Set<Integer> ids = Set.of(results.get(0).getDocId(), results.get(1).getDocId(), results.get(2).getDocId());
        assertEquals(Set.of(1, 2, 3), ids, "Expected all three docIds present");
    }
}