package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataframe.DfDateColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Test class for DateSchemaColumn's parseAndAddToColumn method
public class DateSchemaColumn_parseAndAddToColumn_0_Test {

    @Test
    @DisplayName("aString is null branch-true at r0==null, adds null to column")
    public void test_TC01() {
        // GIVEN a DateSchemaColumn and a DfDateColumn stub
        DateSchemaColumn schemaCol = new DateSchemaColumn(null, "col", "uuuu-M-d");
        DfDateColumn stubColumn = new ConcreteDfDateColumn();
        String aString = null;
        // WHEN parsing null -> parseAsLocalDate returns null (branch r0==null true)
        schemaCol.parseAndAddToColumn(aString, stubColumn);
        // THEN the column should have a single entry null at index 0
        assertEquals(null, stubColumn.getTypedObject(0));
    }

    @Test
    @DisplayName("aString is whitespace-only branch-true at trimmed.isEmpty(), adds null to column")
    public void test_TC02() {
        // GIVEN a DateSchemaColumn and a DfDateColumn stub
        DateSchemaColumn schemaCol = new DateSchemaColumn(null, "col", "uuuu-M-d");
        DfDateColumn stubColumn = new ConcreteDfDateColumn();
        String aString = "   ";
        // WHEN parsing whitespace -> trimmed.isEmpty() true -> returns null
        schemaCol.parseAndAddToColumn(aString, stubColumn);
        // THEN the column should have a single entry null at index 0
        assertEquals(null, stubColumn.getTypedObject(0));
    }

    @Test
    @DisplayName("aString is valid date string branch-false at trimmed.isEmpty(), parsed with strict formatter, adds LocalDate")
    public void test_TC03() {
        // GIVEN a DateSchemaColumn with strict pattern and a real DfDateColumn
        DateSchemaColumn schemaCol = new DateSchemaColumn(null, "col", "uuuu-M-d");
        DfDateColumn realColumn = new ConcreteDfDateColumn();
        String aString = "2023-04-05";
        // WHEN parsing a valid date string -> trimmed.isEmpty() false, parse succeeds
        schemaCol.parseAndAddToColumn(aString, realColumn);
        // THEN the column's first value equals LocalDate.of(2023,4,5)
        assertEquals(LocalDate.of(2023, 4, 5), realColumn.getTypedObject(0));
    }

    @Test
    @DisplayName("aString is invalid date string triggers DateTimeParseException in LocalDate.parse")
    public void test_TC04() {
        // GIVEN a DateSchemaColumn and a DfDateColumn stub
        DateSchemaColumn schemaCol = new DateSchemaColumn(null, "col", "uuuu-M-d");
        DfDateColumn stubColumn = new ConcreteDfDateColumn();
        String aString = "not-a-date";
        // WHEN parsing invalid date -> parse throws DateTimeParseException before addObject
        assertThrows(DateTimeParseException.class, () -> schemaCol.parseAndAddToColumn(aString, stubColumn));
        // THEN no value was added: accessing index 0 should fail
        assertThrows(IndexOutOfBoundsException.class, () -> stubColumn.getTypedObject(0));
    }
}