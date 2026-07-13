package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_1_Test {

    @Test
    @DisplayName("Invoke private static tagSet() via reflection and assert it returns a non-empty Set<String>")
    public void test_TC01() throws Exception {
        // Arrange: load Parser and get private static tagSet method
        Class<?> cls = org.jsoup.parser.Parser.class;
        Method tagSetMethod = cls.getDeclaredMethod("tagSet");
        tagSetMethod.setAccessible(true);

        // Act: invoke the private static method
        Object result = tagSetMethod.invoke(null);

        // Assert: result is a Set and is non-empty, contains a known tag name
        assertAll("Verify tagSet returns a non-empty set of tag names",
            () -> assertTrue(result instanceof Set, "Expected a Set but got " + result.getClass()),
            () -> {
                Set<?> tags = (Set<?>) result;
                assertFalse(tags.isEmpty(), "Expected at least one tag in the set");
                // 'html' is a known HTML tag and should be in the default tag set
                assertTrue(tags.contains("html"), "Expected the set to contain the 'html' tag");
            }
        );
    }

    @Test
    @DisplayName("Attempt to invoke non-existent tagSetFake() via reflection and expect NoSuchMethodException")
    public void test_TC02() {
        // Arrange: reference the Parser class
        Class<?> cls = org.jsoup.parser.Parser.class;

        // Act & Assert: looking for a non-existent method should throw NoSuchMethodException
        assertThrows(NoSuchMethodException.class,
            () -> cls.getDeclaredMethod("tagSetFake"),
            "Expected NoSuchMethodException when accessing tagSetFake"
        );
    }
}