package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataframe.DfColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DfDateColumnImpl; // hypothetical concrete implementation
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;
public class DateSchemaColumn_parseAndAddToColumn_1_Test {

    @Test
    @DisplayName("parseAndAddToColumn adds null when aString is null")
    public void test_TC01() {
        // Given: null input should trigger the aString==null branch → B1(true)
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "uuuu-M-d");
        DfColumn column = new DfDateColumnImpl();
        String aString = null;

        // When
        schema.parseAndAddToColumn(aString, column);

        // Then: column.getTypedObject(0) returns null
        assertNull(column.getTypedObject(0));
    }

    @Test
    @DisplayName("parseAndAddToColumn adds null when aString is whitespace-only after trim")
    public void test_TC02() {
        // Given: whitespace-only input triggers trim->empty string branch → B3(true)
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "uuuu-M-d");
        DfColumn column = new DfDateColumnImpl();
        String aString = "   \t  ";

        // When
        schema.parseAndAddToColumn(aString, column);

        // Then: column.getTypedObject(0) returns null
        assertNull(column.getTypedObject(0));
    }

    @Test
    @DisplayName("parseAndAddToColumn parses and adds a valid date string")
    public void test_TC03() {
        // Given: valid date string passes both null and empty checks → B3(false), then parse success → B5
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "uuuu-M-d");
        DfColumn column = new DfDateColumnImpl();
        String aString = "2023-04-05";

        // When
        schema.parseAndAddToColumn(aString, column);

        // Then: parsed LocalDate matches expected
        assertEquals(LocalDate.of(2023, 4, 5), column.getTypedObject(0));
    }

    @Test
    @DisplayName("parseAndAddToColumn throws DateTimeParseException on invalid date format")
    public void test_TC04() {
        // Given: non-date string triggers parse exception in LocalDate.parse → B4(exception)
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "uuuu-M-d");
        DfColumn column = new DfDateColumnImpl();
        String aString = "not-a-date";

        // Then: assert exception thrown before any addObject call
        assertThrows(DateTimeParseException.class, () ->
            schema.parseAndAddToColumn(aString, column)
        );
    }

    @Test
    @DisplayName("constructor default pattern branch uses \"uuuu-M-d\" when newPattern is null")
    public void test_TC05() {
        // Given: newPattern==null triggers default pattern assignment in ctor
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", null);
        DfColumn column = new DfDateColumnImpl();
        String aString = "2022-12-31";

        // When
        schema.parseAndAddToColumn(aString, column);

        // Then: with default pattern, date is parsed correctly
        assertEquals(LocalDate.of(2022, 12, 31), column.getTypedObject(0));
    }

    @Test
    @DisplayName("parseAndAddToColumn trims input before parsing with custom pattern")
    public void test_TC06() {
        // Given: custom pattern "uuuu/MM/dd" and input with leading/trailing spaces → trimmed before parse
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "uuuu/MM/dd");
        DfColumn column = new DfDateColumnImpl();
        String aString = " 2021/07/01 ";

        // When
        schema.parseAndAddToColumn(aString, column);

        // Then: trimming yields "2021/07/01" which parses to LocalDate.of(2021,7,1)
        assertEquals(LocalDate.of(2021, 7, 1), column.getTypedObject(0));
    }

    @Test
    @DisplayName("parseAndAddToColumn throws on non-leap-year February 29 with strict resolver")
    public void test_TC07() {
        // Given: "2019-2-29" on pattern "uuuu-M-d" is invalid with STRICT resolver → B4(exception)
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "uuuu-M-d");
        DfColumn column = new DfDateColumnImpl();
        String aString = "2019-2-29";

        // Then: expecting DateTimeParseException due to strict resolver disallowing Feb 29 on non-leap year
        assertThrows(DateTimeParseException.class, () ->
            schema.parseAndAddToColumn(aString, column)
        );
    }
}