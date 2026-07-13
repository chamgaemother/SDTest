package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataset.DateSchemaColumn;
import io.github.vmzakharov.ecdataframe.dataset.CsvSchema;
import io.github.vmzakharov.ecdataframe.dataframe.DfDateColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import org.mockito.Mockito;

/**
 * JUnit 5 tests for DateSchemaColumn.parseAndAddToColumn method,
 * covering scenarios TC10 through TC13.
 */
public class DateSchemaColumn_parseAndAddToColumn_2_Test {

    @Test
    @DisplayName("TC10: parseAndAddToColumn(\"2021-2-3\", column) handles one-digit month and day with default pattern")
    public void test_TC10() {
        // Branch B2 false (input not null), B5 false (trimmed non-empty), flows to B7
        CsvSchema schema = new CsvSchema();
        DateSchemaColumn dsc = new DateSchemaColumn(schema, "col", "uuuu-M-d");
        // Use mock to capture addObject argument without needing a concrete implementation
        DfDateColumn column = mock(DfDateColumn.class);
        String input = "2021-2-3";
        // WHEN
        dsc.parseAndAddToColumn(input, column);
        // THEN verify that the parsed LocalDate is passed to addObject
        verify(column).addObject(LocalDate.of(2021, 2, 3));
        verifyNoMoreInteractions(column);
    }

    @Test
    @DisplayName("TC11: parseAndAddToColumn(\"31-12-2021\", column) throws DateTimeParseException with custom pattern 'dd/MM/uuuu'")
    public void test_TC11() {
        // Branch B2 false (input not null), B5 false (non-empty), B6 throws
        CsvSchema schema = new CsvSchema();
        DateSchemaColumn dsc = new DateSchemaColumn(schema, "col", "dd/MM/uuuu");
        DfDateColumn column = mock(DfDateColumn.class);
        String input = "31-12-2021";
        // WHEN / THEN expect parsing exception and no addObject call
        assertThrows(DateTimeParseException.class, () -> {
            dsc.parseAndAddToColumn(input, column);
        }, "Should throw DateTimeParseException for mismatched format");
        verify(column, never()).addObject(any());
    }

    @Test
    @DisplayName("TC12: parseAndAddToColumn(null, column) with custom pattern adds null to column")
    public void test_TC12() {
        // Branch B2 true (input null), flows directly to add null at B3 then B7
        CsvSchema schema = new CsvSchema();
        DateSchemaColumn dsc = new DateSchemaColumn(schema, "col", "dd/MM/uuuu");
        DfDateColumn column = mock(DfDateColumn.class);
        String input = null;
        // WHEN
        dsc.parseAndAddToColumn(input, column);
        // THEN verify that addObject is called with null
        verify(column).addObject(isNull());
        verifyNoMoreInteractions(column);
    }

    @Test
    @DisplayName("TC13: parseAndAddToColumn(\"2000-2-29\", column) parses valid leap day in leap year with default pattern")
    public void test_TC13() {
        // Branch B2 false (input not null), B5 false (trimmed non-empty), flows through B6 successful parse then B7
        CsvSchema schema = new CsvSchema();
        DateSchemaColumn dsc = new DateSchemaColumn(schema, "col", "uuuu-M-d");
        DfDateColumn column = mock(DfDateColumn.class);
        String input = "2000-2-29";
        // WHEN
        dsc.parseAndAddToColumn(input, column);
        // THEN verify correct leap day parsed
        verify(column).addObject(eq(LocalDate.of(2000, 2, 29)));
        verifyNoMoreInteractions(column);
    }
}