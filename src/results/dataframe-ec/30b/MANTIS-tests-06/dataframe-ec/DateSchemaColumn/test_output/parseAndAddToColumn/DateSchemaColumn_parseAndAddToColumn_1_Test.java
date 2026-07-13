package io.github.vmzakharov.ecdataframe.dataset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import io.github.vmzakharov.ecdataframe.dataset.DateSchemaColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DfColumn;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class DateSchemaColumn_parseAndAddToColumn_1_Test {

    /**
     * A simple stub of DfColumn that records the last object added via addObject().
     * We assume DfColumn has a no-arg constructor; if not, adjust accordingly.
     */
    static class StubColumn extends DfColumn {
        Object lastAddedObject;

        public StubColumn() {
            super(); // assume DfColumn has a no-arg constructor
        }

        @Override
        public void addObject(Object obj) {
            super.addObject(obj); // Call the superclass method
            this.lastAddedObject = obj;
        }

        // Other methods of DfColumn are not required for this test
    }

    @Test
    @DisplayName("parseAndAddToColumn with valid date string using a custom pattern adds correct LocalDate")
    public void test_TC05() {
        // This input exercises path B1 (aString != null), B2 (trimmed non-empty), B3 (custom formatter parse), B4 (addObject)
        String aString = "05/03/2022";
        StubColumn dfColumn = new StubColumn();
        DateSchemaColumn column = new DateSchemaColumn(null, "col", "dd/MM/yyyy"); // Changed to yyyy
        column.parseAndAddToColumn(aString, dfColumn); // Removed cast to DfColumn
        // Assert that the stub received the correct LocalDate instance
        assertEquals(LocalDate.of(2022, 3, 5), dfColumn.lastAddedObject);
    }

    @Test
    @DisplayName("parseAndAddToColumn trims whitespace and parses default-pattern date correctly")
    public void test_TC06() {
        // This input exercises path B1 (aString != null), B2 (trimmed non-empty), B3 (default formatter parse), B4 (addObject)
        String aString = " 2022-3-5 ";
        StubColumn dfColumn = new StubColumn();
        DateSchemaColumn column = new DateSchemaColumn(null, "col", null);
        column.parseAndAddToColumn(aString, dfColumn); // Removed cast to DfColumn
        // Assert that whitespace is trimmed and default pattern "uuuu-M-d" is used
        assertEquals(LocalDate.of(2022, 3, 5), dfColumn.lastAddedObject);
    }
}