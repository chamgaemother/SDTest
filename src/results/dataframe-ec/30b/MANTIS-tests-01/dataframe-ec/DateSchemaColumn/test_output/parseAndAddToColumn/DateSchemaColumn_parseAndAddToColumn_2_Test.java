package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataset.DateSchemaColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DfColumn;
import io.github.vmzakharov.ecdataframe.dataframe.DataFrame;
import io.github.vmzakharov.ecdataframe.dataframe.DfDateColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DateSchemaColumn_parseAndAddToColumn_2_Test {
    
    /**
     * Stub implementation of DfDateColumn that records added objects for verification.
     */
    private static class TestDfDateColumn implements DfColumn {
        private final List<Object> list = new ArrayList<>();
        public void addObject(Object obj) {
            list.add(obj);
        }
        public int size() {
            return list.size();
        }
        @SuppressWarnings("unchecked")
        public List<Object> getObjectList() {
            return list;
        }
        // Implementing the missing copyTo method from DfColumn interface
        public void copyTo(DataFrame df) { /* implementation needed */ }
    }

    @Test
    @DisplayName("Constructor with null pattern uses default uuuu-M-d and adds LocalDate for valid input")
    public void test_TC10() {
        // B1 path: newPattern == null -> default pattern uuuu-M-d
        DateSchemaColumn col = new DateSchemaColumn(null, "d", null);
        TestDfDateColumn dfColumn = new TestDfDateColumn();
        // aString matches default pattern exactly
        String aString = "2021-1-1";
        col.parseAndAddToColumn(aString, dfColumn);
        // Expect one element equal to LocalDate.of(2021,1,1)
        assertEquals(1, dfColumn.size());
        assertEquals(LocalDate.of(2021, 1, 1), dfColumn.getObjectList().get(0));
    }

    @Test
    @DisplayName("Custom pattern dd/MM/uuuu parses and adds LocalDate for matching input using real column")
    public void test_TC11() {
        // B1 path: customPattern provided -> use it
        DateSchemaColumn col = new DateSchemaColumn(null, "d", "dd/MM/uuuu");
        TestDfDateColumn dfColumn = new TestDfDateColumn();
        // aString matches dd/MM/uuuu exactly
        String aString = "05/10/2022";
        col.parseAndAddToColumn(aString, dfColumn);
        // Expect one element equal to LocalDate.of(2022,10,5)
        assertEquals(1, dfColumn.size());
        assertEquals(LocalDate.of(2022, 10, 5), dfColumn.getObjectList().get(0));
    }

    @Test
    @DisplayName("Strict default pattern rejects non-leap February 29 and leaves column unchanged")
    public void test_TC12() {
        // B1 path: default pattern uuuu-M-d in strict mode
        DateSchemaColumn col = new DateSchemaColumn(null, "d", "uuuu-M-d");
        TestDfDateColumn dfColumn = new TestDfDateColumn();
        String aString = "2019-2-29";
        // Expect parsing to throw DateTimeParseException and no addition
        assertThrows(DateTimeParseException.class, () -> col.parseAndAddToColumn(aString, dfColumn));
        assertEquals(0, dfColumn.size());
    }

    @Test
    @DisplayName("Strict two-digit-month pattern MM-dd-uuuu rejects single-digit month and throws exception")
    public void test_TC13() {
        // B1 path: customPattern MM-dd-uuuu in strict mode
        DateSchemaColumn col = new DateSchemaColumn(null, "d", "MM-dd-uuuu");
        TestDfDateColumn dfColumn = new TestDfDateColumn();
        // aString has single-digit month "7", violates two-digit requirement
        String aString = "7-04-2023";
        assertThrows(DateTimeParseException.class, () -> col.parseAndAddToColumn(aString, dfColumn));
        assertEquals(0, dfColumn.size());
    }

    @Test
    @DisplayName("Custom pattern with whitespace trimmed and adds correct LocalDate using real column")
    public void test_TC14() {
        // B1 path: customPattern provided, B3 trim path
        DateSchemaColumn col = new DateSchemaColumn(null, "d", "MM-dd-uuuu");
        TestDfDateColumn dfColumn = new TestDfDateColumn();
        // aString contains leading/trailing spaces, should be trimmed
        String aString = " 07-04-2023 ";
        col.parseAndAddToColumn(aString, dfColumn);
        assertEquals(1, dfColumn.size());
        assertEquals(LocalDate.of(2023, 7, 4), dfColumn.getObjectList().get(0));
    }
}