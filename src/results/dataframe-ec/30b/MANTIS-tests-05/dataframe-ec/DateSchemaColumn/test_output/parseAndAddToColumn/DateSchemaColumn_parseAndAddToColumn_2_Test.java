package io.github.vmzakharov.ecdataframe.dataset;

import io.github.vmzakharov.ecdataframe.dataset.DateSchemaColumn;
import io.github.vmzakharov.ecdataframe.dsl.value.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateSchemaColumn_parseAndAddToColumn_2_Test {

    @Test
    @DisplayName("parseAndAddToColumn parses and adds LocalDate for default pattern with zero-padded month and day")
    public void test_TC10() {
        String aString = "2021-01-02";
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", null);
        io.github.vmzakharov.ecdataframe.dataframe.DfColumn stubColumn = Mockito.mock(io.github.vmzakharov.ecdataframe.dataframe.DfColumn.class);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        schema.parseAndAddToColumn(aString, stubColumn);

        Mockito.verify(stubColumn, Mockito.times(1)).addObject(captor.capture());
        Object added = captor.getValue();
        assertEquals(LocalDate.of(2021, 1, 2), added);
    }

    @Test
    @DisplayName("parseAndAddToColumn parses and adds LocalDate for default pattern valid leap-day")
    public void test_TC11() {
        String aString = "2020-02-29";
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", null);
        io.github.vmzakharov.ecdataframe.dataframe.DfColumn stubColumn = Mockito.mock(io.github.vmzakharov.ecdataframe.dataframe.DfColumn.class);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        schema.parseAndAddToColumn(aString, stubColumn);

        Mockito.verify(stubColumn, Mockito.times(1)).addObject(captor.capture());
        Object added = captor.getValue();
        assertEquals(LocalDate.of(2020, 2, 29), added);
    }

    @Test
    @DisplayName("parseAndAddToColumn trims and adds LocalDate for custom pattern dd/MM/uuuu")
    public void test_TC12() {
        String aString = " 31/12/2021 ";
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "dd/MM/uuuu");
        io.github.vmzakharov.ecdataframe.dataframe.DfColumn stubColumn = Mockito.mock(io.github.vmzakharov.ecdataframe.dataframe.DfColumn.class);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        schema.parseAndAddToColumn(aString, stubColumn);

        Mockito.verify(stubColumn, Mockito.times(1)).addObject(captor.capture());
        Object added = captor.getValue();
        assertEquals(LocalDate.of(2021, 12, 31), added);
    }

    @Test
    @DisplayName("parseAndAddToColumn parses and adds LocalDate for custom pattern dd.MM.uuuu without trimming")
    public void test_TC13() {
        String aString = "06.05.2022";
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "dd.MM.uuuu");
        io.github.vmzakharov.ecdataframe.dataframe.DfColumn stubColumn = Mockito.mock(io.github.vmzakharov.ecdataframe.dataframe.DfColumn.class);
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        schema.parseAndAddToColumn(aString, stubColumn);

        Mockito.verify(stubColumn, Mockito.times(1)).addObject(captor.capture());
        Object added = captor.getValue();
        assertEquals(LocalDate.of(2022, 5, 6), added);
    }

    @Test
    @DisplayName("parseAndAddToColumn throws DateTimeParseException for custom pattern dd.MM.uuuu invalid date")
    public void test_TC14() {
        String aString = "29.02.2021";
        DateSchemaColumn schema = new DateSchemaColumn(null, "col", "dd.MM.uuuu");
        io.github.vmzakharov.ecdataframe.dataframe.DfColumn stubColumn = Mockito.mock(io.github.vmzakharov.ecdataframe.dataframe.DfColumn.class);

        assertThrows(DateTimeParseException.class, () -> schema.parseAndAddToColumn(aString, stubColumn));
        Mockito.verify(stubColumn, Mockito.never()).addObject(Mockito.any());
    }
}