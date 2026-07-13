package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataset.DateSchemaColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DfDateColumn;
import io.github.vmzakharov.ecdataframe.dataframe.ConcreteDfDateColumn; // Ensure this class exists
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class DateSchemaColumn_parseAndAddToColumn_0_Test {

    @Test
    @DisplayName("TC01: parseAndAddToColumn(null, dfColumn) covers aString == null branch, adding null")
    public void test_TC01() {
        // GIVEN a null input string triggers the aString == null branch
        DateSchemaColumn col = new DateSchemaColumn(null, "d", "uuuu-M-d");
        DfDateColumn dfColumn = new ConcreteDfDateColumn(); // Changed to use concrete subclass
        String aString = null;
        // WHEN parsing and adding to column
        col.parseAndAddToColumn(aString, dfColumn);
        // THEN the last added element should be null
        int lastIndex = dfColumn.size() - 1;
        assertNotEquals(-1, lastIndex, "No element was added, expected one null element");
        assertNull(dfColumn.getObjectList().get(lastIndex));
    }

    @Test
    @DisplayName("TC02: parseAndAddToColumn(\"   \", dfColumn) covers trimmed.isEmpty() == true branch, adding null")
    public void test_TC02() {
        // GIVEN a blank string input triggers trimmed.isEmpty() branch
        DateSchemaColumn col = new DateSchemaColumn(null, "d", "uuuu-M-d");
        DfDateColumn dfColumn = new ConcreteDfDateColumn(); // Changed to use concrete subclass
        String aString = "   ";
        // WHEN parsing and adding to column
        col.parseAndAddToColumn(aString, dfColumn);
        // THEN the last added element should be null
        int lastIndex = dfColumn.size() - 1;
        assertNotEquals(-1, lastIndex, "Expected a null element to be added for blank input");
        assertNull(dfColumn.getObjectList().get(lastIndex));
    }

    @Test
    @DisplayName("TC03: parseAndAddToColumn(\"2020-2-29\", dfColumn) covers valid leap-date parsing branch")
    public void test_TC03() {
        // GIVEN a valid leap day string triggers full parse path without exception
        DateSchemaColumn col = new DateSchemaColumn(null, "d", "uuuu-M-d");
        DfDateColumn dfColumn = new ConcreteDfDateColumn(); // Changed to use concrete subclass
        String aString = "2020-2-29";
        // WHEN parsing and adding to column
        col.parseAndAddToColumn(aString, dfColumn);
        // THEN the last added element should equal LocalDate.of(2020,2,29)
        int lastIndex = dfColumn.size() - 1;
        assertNotEquals(-1, lastIndex, "Expected an element to be added for valid leap date");
        Object added = dfColumn.getObjectList().get(lastIndex);
        assertTrue(added instanceof LocalDate, "Added object is not a LocalDate");
        assertEquals(LocalDate.of(2020, 2, 29), added);
    }

    @Test
    @DisplayName("TC04: parseAndAddToColumn(\"  1999-12-31 \", dfColumn) covers trimming before parse and correct date boundary")
    public void test_TC04() {
        // GIVEN a valid date string with surrounding whitespace requires trimming
        DateSchemaColumn col = new DateSchemaColumn(null, "d", "uuuu-M-d");
        DfDateColumn dfColumn = new ConcreteDfDateColumn(); // Changed to use concrete subclass
        String aString = "  1999-12-31 ";
        // WHEN parsing and adding to column
        col.parseAndAddToColumn(aString, dfColumn);
        // THEN the last added element should equal LocalDate.of(1999,12,31)
        int lastIndex = dfColumn.size() - 1;
        assertNotEquals(-1, lastIndex, "Expected an element to be added for trimmed valid date");
        Object added = dfColumn.getObjectList().get(lastIndex);
        assertTrue(added instanceof LocalDate, "Added object is not a LocalDate after trimming");
        assertEquals(LocalDate.of(1999, 12, 31), added);
    }

    @Test
    @DisplayName("TC05: parseAndAddToColumn(\"invalid-date\", dfColumn) covers parse exception branch")
    public void test_TC05() {
        // GIVEN an invalid date format string triggers a DateTimeParseException
        DateSchemaColumn col = new DateSchemaColumn(null, "d", "uuuu-M-d");
        DfDateColumn dfColumn = new ConcreteDfDateColumn(); // Changed to use concrete subclass
        String aString = "invalid-date";
        int initialSize = dfColumn.size();
        // WHEN parsing and adding to column EXPECTING exception
        DateTimeParseException exception = assertThrows(
            DateTimeParseException.class,
            () -> col.parseAndAddToColumn(aString, dfColumn)
        );
        // THEN no element should have been added and exception is thrown
        assertEquals(initialSize, dfColumn.size(), "Column size changed despite parse exception");
        assertNotNull(exception.getMessage(), "Exception message should not be null");
    }
}