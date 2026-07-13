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

    /**
     * A minimal stub implementation of DfColumn that records the last added object.
     * Assumes DfColumn has a single addObject(Object) method; any other methods are not used in tests.
     */
    private static class StubColumn implements DfColumn {
        private Object lastAdded = null;

        @Override
        public void addObject(Object obj) {
            this.lastAdded = obj;
        }

        public Object getLastAdded() {
            return this.lastAdded;
        }

        @Override
        public void copyTo(DataFrame df) {
            // No-op implementation for the test
        }
    }

    @Test
    @DisplayName("parseAndAddToColumn adds null when input string is null (aString == null)")
    void test_TC01() {
        // GIVEN aString = null (branch aString == null true) and stubColumn initialized
        StubColumn stubColumn = new StubColumn();
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", null);

        // WHEN
        schema.parseAndAddToColumn(null, stubColumn);

        // THEN stubColumn.getLastAdded() == null
        assertNull(stubColumn.getLastAdded(), "Expected null to be added when input string is null");
    }

    @Test
    @DisplayName("parseAndAddToColumn adds null when input string is empty after trim")
    void test_TC02() {
        // GIVEN aString = "" (branch aString != null, then trimmed.isEmpty() true) and stubColumn initialized
        StubColumn stubColumn = new StubColumn();
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", null);

        // WHEN
        schema.parseAndAddToColumn("", stubColumn);

        // THEN stubColumn.getLastAdded() == null
        assertNull(stubColumn.getLastAdded(), "Expected null to be added when trimmed input is empty");
    }

    @Test
    @DisplayName("parseAndAddToColumn adds null when input string contains only whitespace")
    void test_TC03() {
        // GIVEN aString = "   " (branch aString != null, then trimmed.isEmpty() true) and stubColumn initialized
        StubColumn stubColumn = new StubColumn();
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", null);

        // WHEN
        schema.parseAndAddToColumn("   ", stubColumn);

        // THEN stubColumn.getLastAdded() == null
        assertNull(stubColumn.getLastAdded(), "Expected null to be added when input is whitespace only");
    }

    @Test
    @DisplayName("parseAndAddToColumn parses and adds LocalDate when input matches default pattern uuuu-M-d")
    void test_TC04() {
        // GIVEN aString = "2021-12-31" (branch aString != null, trimmed not empty, valid parse)
        StubColumn stubColumn = new StubColumn();
        // instantiate with null pattern to trigger default "uuuu-M-d"
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", null);

        // WHEN
        schema.parseAndAddToColumn("2021-12-31", stubColumn);

        // THEN stubColumn.getLastAdded().equals(LocalDate.of(2021,12,31))
        Object added = stubColumn.getLastAdded();
        assertTrue(added instanceof LocalDate, "Expected a LocalDate instance");
        assertEquals(LocalDate.of(2021, 12, 31), added, "Parsed date should match 2021-12-31 with default pattern");
    }

    @Test
    @DisplayName("parseAndAddToColumn parses and adds LocalDate when input matches custom pattern dd/MM/uuuu")
    void test_TC05() {
        // GIVEN aString = "31/12/2021" (branch aString != null, trimmed not empty, valid parse)
        StubColumn stubColumn = new StubColumn();
        // instantiate with custom pattern "dd/MM/uuuu"
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "dd/MM/uuuu");

        // WHEN
        schema.parseAndAddToColumn("31/12/2021", stubColumn);

        // THEN stubColumn.getLastAdded().equals(LocalDate.of(2021,12,31))
        Object added = stubColumn.getLastAdded();
        assertTrue(added instanceof LocalDate, "Expected a LocalDate instance");
        assertEquals(LocalDate.of(2021, 12, 31), added, "Parsed date should match 31/12/2021 with custom pattern");
    }

    @Test
    @DisplayName("parseAndAddToColumn throws DateTimeParseException when input does not match default pattern")
    void test_TC06() {
        // GIVEN aString = "12-31-2021" (branch aString != null, trimmed not empty, invalid format for default pattern)
        StubColumn stubColumn = new StubColumn();
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", null);

        // WHEN / THEN throws DateTimeParseException and stubColumn unchanged
        DateTimeParseException ex = assertThrows(DateTimeParseException.class,
            () -> schema.parseAndAddToColumn("12-31-2021", stubColumn),
            "Expected parse failure for mismatched pattern");
        assertNull(stubColumn.getLastAdded(), "Stub column should remain unchanged on parse error");
    }

    @Test
    @DisplayName("parseAndAddToColumn throws DateTimeParseException for invalid date under strict resolver")
    void test_TC07() {
        // GIVEN aString = "2021-02-29" (branch aString != null, trimmed not empty, strict-resolver rejects invalid date)
        StubColumn stubColumn = new StubColumn();
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", null);

        // WHEN / THEN throws DateTimeParseException and stubColumn unchanged
        DateTimeParseException ex = assertThrows(DateTimeParseException.class,
            () -> schema.parseAndAddToColumn("2021-02-29", stubColumn),
            "Expected strict resolver to reject invalid leap-date");
        assertNull(stubColumn.getLastAdded(), "Stub column should remain unchanged when date is invalid");
    }
}