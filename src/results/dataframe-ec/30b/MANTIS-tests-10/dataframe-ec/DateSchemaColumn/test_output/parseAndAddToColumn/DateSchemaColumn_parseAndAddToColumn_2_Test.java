package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataset.DateSchemaColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DfColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DataFrame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for DateSchemaColumn.parseAndAddToColumn(...)
 */
public class DateSchemaColumn_parseAndAddToColumn_2_Test {

    private static class StubColumn implements DfColumn {
        Object addedObject;

        @Override
        public void addObject(Object obj) {
            this.addedObject = obj;
        }

        @Override
        public void copyTo(DataFrame df) {
            // No-op implementation for stub
        }
    }

    @Test
    @DisplayName("parseAndAddToColumn with custom slash-based pattern parses valid date")
    public void test_TC07() {
        // GIVEN a DateSchemaColumn with custom pattern "dd/MM/uuuu"
        DateSchemaColumn column = new DateSchemaColumn(null, "d", "dd/MM/uuuu");
        StubColumn stub = new StubColumn();
        String aString = "31/12/2022";
        // WHEN parseAndAddToColumn is called
        column.parseAndAddToColumn(aString, stub);
        // THEN the stub should receive a LocalDate of 2022-12-31
        // This input exercises B1 (newPattern != null) and B3 (trimmed non-empty) branches
        assertEquals(LocalDate.of(2022, 12, 31), stub.addedObject);
    }

    @Test
    @DisplayName("parseAndAddToColumn throws DateTimeParseException for non-leap Feb 29 under strict resolver")
    public void test_TC08() {
        // GIVEN a DateSchemaColumn with default pattern "uuuu-M-d" (passing non-null pattern here)
        DateSchemaColumn column = new DateSchemaColumn(null, "d", "uuuu-M-d");
        StubColumn stub = new StubColumn();
        String aString = "2019-2-29";
        // WHEN/THEN expect strict resolver to reject invalid date -> DateTimeParseException
        // This input exercises B1 (newPattern != null) and B3 (trimmed non-empty) and triggers exception in parsing
        assertThrows(DateTimeParseException.class, () -> column.parseAndAddToColumn(aString, stub));
    }
}