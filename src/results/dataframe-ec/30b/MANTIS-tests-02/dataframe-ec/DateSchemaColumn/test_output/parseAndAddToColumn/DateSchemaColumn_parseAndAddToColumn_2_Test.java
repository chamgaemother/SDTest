package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataframe.DfColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DfDateColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class DateSchemaColumn_parseAndAddToColumn_2_Test {
    /**
     * A simple stub of DfDateColumn that records added LocalDate objects.
     */
    private static class DummyDfDateColumn extends DfDateColumn {
        private final List<LocalDate> storage = new ArrayList<>();

        @Override
        public LocalDate getTypedObject(int index) {
            return this.storage.get(index);
        }

        @Override
        public void addObject(LocalDate obj) {
            // The production code calls addObject on a DfColumn with a LocalDate (or null).
            this.storage.add(obj);
        }
    }

    @Test
    @DisplayName("parseAndAddToColumn throws NullPointerException when dfColumn is null")
    public void test_TC08() {
        // B0→B3→B5: passing a non-empty string but null column should hit the null-target branch 
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "uuuu-M-d");
        String aString = "2023-01-01";
        DummyDfDateColumn column = null;

        assertThrows(NullPointerException.class,
            () -> schema.parseAndAddToColumn(aString, column),
            "Expected a NullPointerException when dfColumn is null");
    }

    @Test
    @DisplayName("parseAndAddToColumn adds null when input is empty string (trimmed empty)")
    public void test_TC09() {
        // B0→B3(true)→B5: passing an empty string which trims to empty, expecting parseAsLocalDate to return null
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "uuuu-M-d");
        DummyDfDateColumn column = new DummyDfDateColumn();
        String aString = "";  // trimmed.isEmpty() == true

        schema.parseAndAddToColumn(aString, column);

        // The first added value should be null
        assertEquals(null,
                     column.getTypedObject(0),
                     "Empty trimmed input should result in a null LocalDate being added");
    }

    @Test
    @DisplayName("parseAndAddToColumn parses February 29 on a leap year correctly")
    public void test_TC10() {
        // B0→B3(false)→B5: passing "2020-2-29" which is valid leap-year date under strict resolver style
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "uuuu-M-d");
        DummyDfDateColumn column = new DummyDfDateColumn();
        String aString = "2020-2-29";  // valid leap year date under pattern uuuu-M-d

        schema.parseAndAddToColumn(aString, column);

        LocalDate expected = LocalDate.of(2020, 2, 29);
        assertEquals(expected,
                     column.getTypedObject(0),
                     "Leap year date '2020-2-29' should be parsed into LocalDate.of(2020,2,29)");
    }
}