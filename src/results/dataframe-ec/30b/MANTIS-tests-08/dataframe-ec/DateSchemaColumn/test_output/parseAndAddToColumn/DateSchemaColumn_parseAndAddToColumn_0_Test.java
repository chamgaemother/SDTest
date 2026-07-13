package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataset.DateSchemaColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DfColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DataFrame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

public class DateSchemaColumn_parseAndAddToColumn_0_Test {

    // A simple stub for DfColumn to record added objects
    private static class StubDfColumn implements DfColumn {
        private Object lastAdded;
        private int countAdded;

        @Override
        public void addObject(Object obj) {
            this.lastAdded = obj;
            this.countAdded++;
        }

        @Override
        @Override
        public void copyTo(DataFrame df) {
            // Stub implementation, not needed for tests
        }

        public Object getLastAdded() {
            return this.lastAdded;
        }

        public int getCountAdded() {
            return this.countAdded;
        }
    }

    @Test
    @DisplayName("parseAndAddToColumn(null, dfColumn) should add null when input string is null (r0 == null branch)")
    public void test_TC01() {
        // GIVEN a DateSchemaColumn with default pattern and a stub column
        DateSchemaColumn col = new DateSchemaColumn(null, "d", null);
        StubDfColumn dfColumn = new StubDfColumn();
        String aString = null;
        // WHEN calling parseAndAddToColumn with null input (r0 == null true branch)
        col.parseAndAddToColumn(aString, dfColumn);
        // THEN null should be added exactly once
        assertEquals(1, dfColumn.getCountAdded(), "Expected one addObject call");
        assertNull(dfColumn.getLastAdded(), "Expected last added object to be null");
    }

    @Test
    @DisplayName("parseAndAddToColumn(\"   \", dfColumn) should add null when trimmed string is empty (trimmed.isEmpty true)")
    public void test_TC02() {
        // GIVEN a DateSchemaColumn with default pattern and a stub column
        DateSchemaColumn col = new DateSchemaColumn(null, "d", null);
        StubDfColumn dfColumn = new StubDfColumn();
        String aString = "   ";
        // WHEN calling with whitespace input -> trimmed.isEmpty true branch
        col.parseAndAddToColumn(aString, dfColumn);
        // THEN null should be added
        assertEquals(1, dfColumn.getCountAdded(), "Expected one addObject call for empty trimmed string");
        assertNull(dfColumn.getLastAdded(), "Expected last added object to be null for empty trimmed string");
    }

    @Test
    @DisplayName("parseAndAddToColumn(\"2020-12-31\", dfColumn) should add LocalDate parsed using default pattern (parse success)")
    public void test_TC03() {
        // GIVEN a DateSchemaColumn with default pattern 'uuuu-M-d' and a stub column
        DateSchemaColumn col = new DateSchemaColumn(null, "d", null);
        StubDfColumn dfColumn = new StubDfColumn();
        String aString = "2020-12-31";
        // WHEN parsing a valid date string
        col.parseAndAddToColumn(aString, dfColumn);
        // THEN a LocalDate of 2020-12-31 should be added
        assertEquals(1, dfColumn.getCountAdded(), "Expected one addObject call for valid date");
        Object added = dfColumn.getLastAdded();
        assertTrue(added instanceof LocalDate, "Expected added object to be a LocalDate");
        assertEquals(LocalDate.of(2020, 12, 31), added,
                "Expected parsed LocalDate.of(2020,12,31)");
    }

    @Test
    @DisplayName("parseAndAddToColumn(\"31/12/2020\", dfColumn) with custom pattern dd/MM/uuuu should add correct LocalDate")
    public void test_TC04() {
        // GIVEN a DateSchemaColumn with custom pattern 'dd/MM/uuuu' and a stub column
        DateSchemaColumn col = new DateSchemaColumn(null, "d", "dd/MM/uuuu");
        StubDfColumn dfColumn = new StubDfColumn();
        String aString = "31/12/2020";
        // WHEN parsing with custom pattern -> branch for non-null, non-empty trimmed input
        col.parseAndAddToColumn(aString, dfColumn);
        // THEN a LocalDate of 2020-12-31 should be added
        assertEquals(1, dfColumn.getCountAdded(), "Expected one addObject call for custom pattern date");
        Object added = dfColumn.getLastAdded();
        assertTrue(added instanceof LocalDate, "Expected added object to be a LocalDate when using custom pattern");
        assertEquals(LocalDate.of(2020, 12, 31), added,
                "Expected parsed LocalDate.of(2020,12,31) with custom pattern");
    }

    @Test
    @DisplayName("parseAndAddToColumn(\"invalid-date\", dfColumn) throws DateTimeParseException on parse failure")
    public void test_TC05() {
        // GIVEN a DateSchemaColumn with default pattern and a stub column
        DateSchemaColumn col = new DateSchemaColumn(null, "d", null);
        StubDfColumn dfColumn = new StubDfColumn();
        String aString = "invalid-date";
        // WHEN parsing an invalid date string -> expect exception in parsing branch
        DateTimeParseException ex = assertThrows(
                DateTimeParseException.class,
                () -> col.parseAndAddToColumn(aString, dfColumn),
                "Expected DateTimeParseException for invalid date input"
        );
        // THEN no object should have been added before exception
        assertEquals(0, dfColumn.getCountAdded(), "Expected no additions on parse failure");
        // Optionally verify the message contains the input
        assertTrue(ex.getMessage().contains(aString), "Exception message should mention the invalid input");
    }
}