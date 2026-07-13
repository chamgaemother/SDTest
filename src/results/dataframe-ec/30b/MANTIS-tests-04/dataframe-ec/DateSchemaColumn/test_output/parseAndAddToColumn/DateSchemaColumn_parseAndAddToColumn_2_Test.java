package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataset.DateSchemaColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DfColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

public class DateSchemaColumn_parseAndAddToColumn_2_Test {

    /**
     * A simple stub of DfColumn that records calls to addObject(Object).
     * We assume a public no-arg constructor exists for DfColumn for testing.
     */
    static class StubDfColumn extends io.github.vmzakharov.ecdataframe.dataframe.DfColumn {
        int addCount = 0;
        Object lastAdded = null;

        public StubDfColumn() {
            super();
        }

        @Override
        public void addObject(Object obj) throws Exception {
            this.addCount++;
            this.lastAdded = obj;
        }
    }

    @Test
    @DisplayName("parseAndAddToColumn adds null when input string is empty (\"\" triggers trimmed.isEmpty branch)")
    void test_TC07() {
        // Given a DateSchemaColumn with default pattern (null pattern argument)
        DateSchemaColumn col = new DateSchemaColumn(null, "d", null);
        DfColumn stub = new StubDfColumn();
        String aString = ""; // empty input

        // When: parsing empty string should lead to trimmed.isEmpty -> parseAsLocalDate returns null
        col.parseAndAddToColumn(aString, stub);

        // Then: addObject called once with null
        assertNull(((StubDfColumn) stub).lastAdded, "Expected lastAdded to be null for empty input");
        assertEquals(1, ((StubDfColumn) stub).addCount, "addObject should be called exactly once");
    }

    @Test
    @DisplayName("parseAndAddToColumn throws DateTimeParseException for non-date text (invalid-format branch)")
    void test_TC08() {
        // Given a DateSchemaColumn with default pattern
        DateSchemaColumn col = new DateSchemaColumn(null, "d", null);
        DfColumn stub = new StubDfColumn();
        String aString = "not-a-date"; // non-date text, not parsable

        // When & Then: parsing invalid text should throw DateTimeParseException and not call addObject
        DateTimeParseException ex = assertThrows(
            DateTimeParseException.class,
            () -> col.parseAndAddToColumn(aString, stub),
            "Expected a DateTimeParseException for invalid date text"
        );
        // After exception, ensure no objects were added
        assertEquals(0, ((StubDfColumn) stub).addCount, "addObject must not be called when parsing fails");
    }

    @Test
    @DisplayName("parseAndAddToColumn throws DateTimeParseException for out-of-range numeric values (month=13 strict-resolver branch)")
    void test_TC09() {
        // Given a DateSchemaColumn with default pattern
        DateSchemaColumn col = new DateSchemaColumn(null, "d", null);
        DfColumn stub = new StubDfColumn();
        String aString = "2023-13-01"; // month 13 is out of range under strict resolver

        // When & Then: parsing out-of-range month should throw DateTimeParseException and not call addObject
        DateTimeParseException ex = assertThrows(
            DateTimeParseException.class,
            () -> col.parseAndAddToColumn(aString, stub),
            "Expected a DateTimeParseException for out-of-range month"
        );
        // After exception, ensure no objects were added
        assertEquals(0, ((StubDfColumn) stub).addCount, "addObject must not be called when parsing fails");
    }
}