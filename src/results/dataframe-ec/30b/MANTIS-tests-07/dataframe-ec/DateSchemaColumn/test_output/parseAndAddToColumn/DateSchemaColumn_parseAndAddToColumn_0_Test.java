package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataframe.DfColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;
public class DateSchemaColumn_parseAndAddToColumn_0_Test {

    /**
     * A simple stub for DfColumn that captures the last added object.
     */
    private static class StubDfColumn extends DfColumn {
        private Object lastAdded;

        @Override
        public void addObject(Object o) {
            this.lastAdded = o;
        }

        public Object getLastAdded() {
            return this.lastAdded;
        }
    }

    @Test
    @DisplayName("When aString is null, the method should add null to the column (tests branch aString==null)")
    public void test_TC01() {
        DateSchemaColumn col = new DateSchemaColumn(null, "name", "uuuu-M-d");
        StubDfColumn stubColumn = new StubDfColumn();

        col.parseAndAddToColumn(null, stubColumn);

        assertNull(stubColumn.getLastAdded(), "Expected null to be added when input is null");
    }

    @Test
    @DisplayName("When aString is blank after trim, the method should add null to the column (tests trimmed.isEmpty()==true)")
    public void test_TC02() {
        DateSchemaColumn col = new DateSchemaColumn(null, "name", "uuuu-M-d");
        StubDfColumn stubColumn = new StubDfColumn();
        String input = "   "; // blank after trim

        col.parseAndAddToColumn(input, stubColumn);

        assertNull(stubColumn.getLastAdded(), "Expected null to be added when trimmed input is empty");
    }

    @Test
    @DisplayName("When aString is a valid date string matching default pattern, it should parse and add correct LocalDate")
    public void test_TC03() {
        DateSchemaColumn col = new DateSchemaColumn(null, "name", "uuuu-M-d");
        StubDfColumn stubColumn = new StubDfColumn();
        String input = "2021-12-5"; // matches uuuu-M-d

        col.parseAndAddToColumn(input, stubColumn);

        LocalDate expected = LocalDate.of(2021, 12, 5);
        assertEquals(expected, stubColumn.getLastAdded(),
            "Expected LocalDate.of(2021,12,5) to be added for valid default-pattern input");
    }

    @Test
    @DisplayName("When aString has leading/trailing spaces around a valid date, it should parse after trim")
    public void test_TC04() {
        DateSchemaColumn col = new DateSchemaColumn(null, "name", "uuuu-M-d");
        StubDfColumn stubColumn = new StubDfColumn();
        String input = " 2021-1-1 "; // valid date with spaces

        col.parseAndAddToColumn(input, stubColumn);

        LocalDate expected = LocalDate.of(2021, 1, 1);
        assertEquals(expected, stubColumn.getLastAdded(),
            "Expected LocalDate.of(2021,1,1) to be added when input has leading/trailing spaces");
    }

    @Test
    @DisplayName("When aString does not match formatter pattern, a DateTimeParseException should be thrown")
    public void test_TC05() {
        DateSchemaColumn col = new DateSchemaColumn(null, "name", "uuuu-M-d");
        StubDfColumn stubColumn = new StubDfColumn();
        String input = "12/31/2020"; // does not match uuuu-M-d

        DateTimeParseException ex = assertThrows(
            DateTimeParseException.class,
            () -> col.parseAndAddToColumn(input, stubColumn),
            "Expected DateTimeParseException for invalid date format"
        );
        assertNull(stubColumn.getLastAdded(), "No object should be added on parse failure");
    }

    @Test
    @DisplayName("When using custom pattern dd/MM/uuuu and valid input, it should parse according to custom formatter")
    public void test_TC06() {
        DateSchemaColumn col = new DateSchemaColumn(null, "name", "dd/MM/uuuu");
        StubDfColumn stubColumn = new StubDfColumn();
        String input = "31/01/2020"; // matches custom pattern

        col.parseAndAddToColumn(input, stubColumn);

        LocalDate expected = LocalDate.of(2020, 1, 31);
        assertEquals(expected, stubColumn.getLastAdded(),
            "Expected LocalDate.of(2020,1,31) for valid custom-pattern input");
    }
}