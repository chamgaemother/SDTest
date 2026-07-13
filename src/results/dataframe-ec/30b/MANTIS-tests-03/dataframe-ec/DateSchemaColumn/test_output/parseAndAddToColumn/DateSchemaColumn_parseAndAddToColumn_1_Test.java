package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataframe.DfColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

public class DateSchemaColumn_parseAndAddToColumn_1_Test {

    /**
     * Scenario TC06: parseAndAddToColumn trims whitespace and adds LocalDate for valid default-pattern date string.
     * Path B0→B3→B4→B5:
     * - B0: entry into parseAndAddToColumn
     * - B3: aString != null
     * - B4: trimmed is not empty
     * - B5: parse succeeds and addObject invoked
     */
    @Test
    @DisplayName("parseAndAddToColumn trims whitespace and adds LocalDate for valid default-pattern date string")
    public void test_TC06() {
        // GIVEN
        DateSchemaColumn col = new DateSchemaColumn(null, "date", null);
        // Use Mockito to stub DfColumn and capture the argument to addObject
        DfColumn stub = mock(DfColumn.class);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        String aString = " 2020-02-29 "; // includes whitespace -> triggers trim branch

        // WHEN
        col.parseAndAddToColumn(aString, stub);

        // THEN
        // verify addObject called exactly once with the parsed LocalDate
        verify(stub, times(1)).addObject(captor.capture());
        Object added = captor.getValue();
        assertEquals(LocalDate.of(2020, 2, 29), added,
            "Expected leap-year date 2020-02-29 to be parsed and added");
    }

    /**
     * Scenario TC07: parseAndAddToColumn throws DateTimeParseException for invalid leap-day under strict resolver style.
     * Path B0→B3→B4→B6:
     * - B0: entry into parseAndAddToColumn
     * - B3: aString != null
     * - B4: trimmed is not empty
     * - B6: parse fails -> exception
     */
    @Test
    @DisplayName("parseAndAddToColumn throws DateTimeParseException for invalid leap-day under strict resolver style")
    public void test_TC07() {
        // GIVEN
        DateSchemaColumn col = new DateSchemaColumn(null, "date", null);
        DfColumn stub = mock(DfColumn.class);
        String aString = "2021-02-29"; // invalid leap-day -> should throw

        // WHEN / THEN
        assertThrows(DateTimeParseException.class,
            () -> col.parseAndAddToColumn(aString, stub),
            "Expected DateTimeParseException when parsing invalid leap-day 2021-02-29");
    }
}