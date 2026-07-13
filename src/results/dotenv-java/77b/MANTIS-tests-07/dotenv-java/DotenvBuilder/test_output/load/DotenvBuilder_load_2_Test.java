package io.github.cdimascio.dotenv;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.github.cdimascio.dotenv.DotenvException;
import io.github.cdimascio.dotenv.DotenvEntry;
import io.github.cdimascio.dotenv.internal.DotenvParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class DotenvBuilder_load_2_Test {

    @Test
    @DisplayName("TC10: load() without systemProperties and parser returns multiple entries exercises B0→B2 with loop>0")
    public void test_TC10() throws Exception {
        // Arrange: stub DotenvParser.parse() to return two entries
        List<DotenvEntry> stubEntries = List.of(
                new DotenvEntry("K1", "V1"),
                new DotenvEntry("K2", "V2")
        );
        try (MockedConstruction<DotenvParser> mc = mockConstruction(DotenvParser.class,
                (mock, ctx) -> when(mock.parse()).thenReturn(stubEntries))) {
            DotenvBuilder builder = new DotenvBuilder();
            // We do NOT call systemProperties() so systemProperties==false -> B0→B2 path
            // Act
            Dotenv dotenv = builder.load();
            // Assert: entries include our stub entries
            Set<DotenvEntry> entries = dotenv.entries();
            assertTrue(entries.containsAll(stubEntries),
                    "Parsed entries should be present in the loaded Dotenv entries");
            // And get() returns correct values for those keys
            assertEquals("V1", dotenv.get("K1"));
            assertEquals("V2", dotenv.get("K2"));
        }
    }

    @Test
    @DisplayName("TC11: load() with ignoreIfMissing and non-existent directory does not throw and yields only system env entries")
    public void test_TC11() throws Exception {
        // Arrange: use a directory that does not exist and ignoreIfMissing()
        String badDir = "./no_such_dir_xyz";
        DotenvBuilder builder = new DotenvBuilder()
                .ignoreIfMissing()
                .directory(badDir);
        // Act
        Dotenv dotenv = builder.load();  // should not throw
        // Build expected: all System.getenv() entries only
        Set<DotenvEntry> expected = System.getenv()
                .entrySet()
                .stream()
                .map(e -> new DotenvEntry(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
        // Assert
        assertEquals(expected, dotenv.entries(),
                "When ignoring missing file, entries() must equal system environment entries only");
    }

    @Test
    @DisplayName("TC12: load() throws DotenvException when throwIfMalformed=true and parser.parse() signals malformed data")
    public void test_TC12() throws Exception {
        // Arrange: stub DotenvParser.parse() to throw malformed exception
        try (MockedConstruction<DotenvParser> mc = mockConstruction(DotenvParser.class,
                (mock, ctx) -> when(mock.parse())
                        .thenThrow(new DotenvException("malformed"))) {
            DotenvBuilder builder = new DotenvBuilder();
            // Act & Assert: exception is propagated
            DotenvException ex = assertThrows(DotenvException.class, builder::load,
                    "Malformed data should cause DotenvException to be thrown");
            assertEquals("malformed", ex.getMessage(),
                    "Exception message must match the one thrown by parser");
        }
    }
}