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

    // A simple stub for DfColumn that captures the last added object
    private static class StubDfColumn implements DfColumn {
        private Object lastAdded;

        @Override
        public void addObject(Object obj) {
            this.lastAdded = obj;
        }

        public Object getLastAdded() {
            return this.lastAdded;
        }

        // Implementing the missing method from DfColumn correctly
        @Override
        public void copyTo(DataFrame df) {
            // No implementation needed for this test
        }
    }

    @Test
    @DisplayName("Null pattern in constructor leads to default pattern and valid date is parsed correctly")
    public void test_TC07() {
        // Branch B0->B1: aString != null; B2: trimmed not empty; B3 parse; B5 return
        DateSchemaColumn col = new DateSchemaColumn(null, "name", null);
        StubDfColumn stub = new StubDfColumn();
        String input = "2021-12-5"; // matches default pattern "uuuu-M-d"

        col.parseAndAddToColumn(input, stub);

        // Expect LocalDate.of(2021,12,5) added to stub
        assertEquals(LocalDate.of(2021, 12, 5), stub.getLastAdded());
    }

    @Test
    @DisplayName("Null pattern in constructor with structurally matching but invalid date throws parse exception")
    public void test_TC08() {
        // Branch B0->B1: aString != null; B2: trimmed not empty; B3 parse; B4 exception
        DateSchemaColumn col = new DateSchemaColumn(null, "name", null);
        StubDfColumn stub = new StubDfColumn();
        String input = "2021-02-29"; // non-leap year invalid date under default strict resolver

        DateTimeParseException thrown = assertThrows(
                DateTimeParseException.class,
                () -> col.parseAndAddToColumn(input, stub),
                "Expected parseAndAddToColumn to throw DateTimeParseException for invalid date"
        );
        // No object should be added upon exception
        assertNull(stub.getLastAdded());
    }

    @Test
    @DisplayName("Valid leap year date parsed correctly under default pattern")
    public void test_TC09() {
        // Branch B0->B1: aString != null; B2: trimmed not empty; B3 parse; B5 return
        DateSchemaColumn col = new DateSchemaColumn(null, "name", "uuuu-M-d");
        StubDfColumn stub = new StubDfColumn();
        String input = "2020-2-29"; // valid leap day under default pattern

        col.parseAndAddToColumn(input, stub);

        // Expect LocalDate.of(2020,2,29) added
        assertEquals(LocalDate.of(2020, 2, 29), stub.getLastAdded());
    }

    @Test
    @DisplayName("Structurally matching date with out-of-range month throws DateTimeParseException")
    public void test_TC10() {
        // Branch B0->B1: aString != null; B2: trimmed not empty; B3 parse; B4 exception
        DateSchemaColumn col = new DateSchemaColumn(null, "name", "uuuu-M-d");
        StubDfColumn stub = new StubDfColumn();
        String input = "2021-13-1"; // month=13 invalid under strict resolver

        assertThrows(
                DateTimeParseException.class,
                () -> col.parseAndAddToColumn(input, stub),
                "Expected parseAndAddToColumn to throw DateTimeParseException for out-of-range month"
        );
        // No object should be added upon exception
        assertNull(stub.getLastAdded());
    }
}