package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataframe.DfColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DataFrame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateSchemaColumn_parseAndAddToColumn_2_Test {

    /**
     * A minimal stub implementation of DfColumn that only provides addObject(Object).
     * Other methods are not used by parseAndAddToColumn and thus throw if ever called.
     */
    static class StubColumn implements DfColumn {
        private Object lastAdded;

        @Override
        public void addObject(Object obj) {
            this.lastAdded = obj;
        }

        @Override public int size() { return 0; }
        @Override public Object get(int index) { return null; }
        @Override public void clear() { }
        @Override public boolean isReadOnly() { return false; }
        @Override public void setReadOnly(boolean readOnly) { }
        @Override public String getName() { return "col"; }
        @Override public void setName(String name) { }
        @Override public void remove(int index) { }
        @Override
        public void copyTo(DataFrame df) { /* No implementation needed for tests */ }
    }

    @Test
    @DisplayName("TC07: parseAndAddToColumn with zero-padded default-pattern date string throws DateTimeParseException")
    void test_TC07() {
        // The default pattern is "uuuu-M-d". Input "2022-03-05" has zero-padded month/day,
        // so strict resolver should fail on B0→B1→B2→B3 path.
        String aString = "2022-03-05";
        StubColumn dfColumn = new StubColumn();
        DateSchemaColumn column = new DateSchemaColumn(null, "col", null);

        assertThrows(
            DateTimeParseException.class,
            () -> column.parseAndAddToColumn(aString, dfColumn),
            "Expected a DateTimeParseException for zero-padded input against default pattern"
        );
    }

    @Test
    @DisplayName("TC08: parseAndAddToColumn with malformed custom-pattern date string throws DateTimeParseException")
    void test_TC08() {
        // Custom pattern "dd/MM/yyyy" expects slashes; input "5-3-2022" uses dashes,
        // so should fail at parsing (B0→B1→B2→B3).
        String aString = "5-3-2022";
        StubColumn dfColumn = new StubColumn();
        DateSchemaColumn column = new DateSchemaColumn(null, "col", "dd/MM/yyyy");

        assertThrows(
            DateTimeParseException.class,
            () -> column.parseAndAddToColumn(aString, dfColumn),
            "Expected a DateTimeParseException for malformed input against custom pattern"
        );
    }
}