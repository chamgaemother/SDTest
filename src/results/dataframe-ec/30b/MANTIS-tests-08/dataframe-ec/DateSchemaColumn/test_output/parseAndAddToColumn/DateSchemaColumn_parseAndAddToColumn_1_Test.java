package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataset.DateSchemaColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DfColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DataFrame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;
public class DateSchemaColumn_parseAndAddToColumn_1_Test {

    // A simple stub implementing DfColumn to capture added objects
    private static class StubDfColumn implements DfColumn {
        private int countAdded = 0;
        private Object lastAdded = null;

        @Override
        public void addObject(Object obj) {
            this.countAdded++;
            this.lastAdded = obj;
        }

        // Implementing the missing method from DfColumn with correct signature
        @Override
        public void copyTo(DataFrame df) { /* implementation needed */ }

        // Other methods of DfColumn are not needed for this test

        public int getCountAdded() {
            return this.countAdded;
        }

        public Object getLastAdded() {
            return this.lastAdded;
        }
    }

    @Test
    @DisplayName("parseAndAddToColumn trims input and correctly parses a valid date with surrounding whitespace")
    public void test_TC06() {
        // GIVEN a DateSchemaColumn with default pattern (null triggers 'uuuu-M-d')
        DateSchemaColumn col = new DateSchemaColumn(null, "d", null);
        StubDfColumn dfColumn = new StubDfColumn();
        // Input has whitespace to test trimming branch (aString != null, trimmed not empty)
        String aString = " 2020-1-1 ";

        // WHEN parsing and adding to column
        col.parseAndAddToColumn(aString, dfColumn);

        // THEN one object should be added, and it should equal LocalDate.of(2020,1,1)
        assertEquals(1, dfColumn.getCountAdded(), "Expected exactly one addition after parsing valid date");
        assertEquals(LocalDate.of(2020, 1, 1), dfColumn.getLastAdded(),
                "Expected parsed LocalDate 2020-01-01 from trimmed input");
    }

    @Test
    @DisplayName("parseAndAddToColumn throws DateTimeParseException for invalid date under custom pattern")
    public void test_TC07() {
        // GIVEN a DateSchemaColumn with custom pattern 'dd/MM/uuuu'
        DateSchemaColumn col = new DateSchemaColumn(null, "d", "dd/MM/uuuu");
        StubDfColumn dfColumn = new StubDfColumn();
        // Input does not match the pattern (year-day-month vs day/month/year)
        String aString = "2020-31-12";

        // WHEN and THEN a DateTimeParseException should be thrown; addition count remains zero
        DateTimeParseException ex = assertThrows(
                DateTimeParseException.class,
                () -> col.parseAndAddToColumn(aString, dfColumn),
                "Expected DateTimeParseException for input not matching dd/MM/uuuu"
        );
        // After exception, no objects should have been added
        assertEquals(0, dfColumn.getCountAdded(), "No object should be added when parsing fails");
    }

    @Test
    @DisplayName("parseAndAddToColumn throws DateTimeParseException for non-existent leap-day under strict default pattern")
    public void test_TC08() {
        // GIVEN a DateSchemaColumn with default strict pattern 'uuuu-M-d'
        DateSchemaColumn col = new DateSchemaColumn(null, "d", null);
        StubDfColumn dfColumn = new StubDfColumn();
        // Input '2019-02-29' is invalid because 2019 is not a leap year
        String aString = "2019-02-29";

        // WHEN and THEN a DateTimeParseException should be thrown due to strict resolver
        assertThrows(
                DateTimeParseException.class,
                () -> col.parseAndAddToColumn(aString, dfColumn),
                "Expected DateTimeParseException for invalid leap-day under strict pattern"
        );
        // Ensure no object was added
        assertEquals(0, dfColumn.getCountAdded(), "No object should be added when leap-day parsing fails");
    }
}