package software.amazon.event.ruler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ComparableNumber_generate_0_Test {

    @Test
    @DisplayName("generate throws IllegalArgumentException when f < -5e9 (lower bound violation)")
    void test_TC01() throws Exception {
        double f = -5_000_000_000.1;
        Class<?> clazz = Class.forName("software.amazon.event.ruler.ComparableNumber");
        Method generate = clazz.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            try {
                generate.invoke(null, f);
            } catch (InvocationTargetException ite) {
                throw ite.getTargetException();
            }
        });
        assertEquals("Value must be between -5000000000.0 and 5000000000.0, inclusive", ex.getMessage());
    }

    @Test
    @DisplayName("generate throws IllegalArgumentException when f > 5e9 (upper bound violation)")
    void test_TC02() throws Exception {
        double f = 5_000_000_000.1;
        Class<?> clazz = Class.forName("software.amazon.event.ruler.ComparableNumber");
        Method generate = clazz.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            try {
                generate.invoke(null, f);
            } catch (InvocationTargetException ite) {
                throw ite.getTargetException();
            }
        });
        assertEquals("Value must be between -5000000000.0 and 5000000000.0, inclusive", ex.getMessage());
    }

    @Test
    @DisplayName("generate returns all-zero hex string when f = -5e9 (exact lower bound)")
    void test_TC03() throws Exception {
        double f = -5_000_000_000.0;
        Class<?> clazz = Class.forName("software.amazon.event.ruler.ComparableNumber");
        Method generate = clazz.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("00000000000000", result);
    }

    @Test
    @DisplayName("generate returns correct mid hex string when f = 0 (mid-range)")
    void test_TC04() throws Exception {
        double f = 0.0;
        Class<?> clazz = Class.forName("software.amazon.event.ruler.ComparableNumber");
        Method generate = clazz.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("11682157304000", result);
    }

    @Test
    @DisplayName("generate returns correct hex string when f = 5e9 (exact upper bound)")
    void test_TC05() throws Exception {
        double f = 5_000_000_000.0;
        Class<?> clazz = Class.forName("software.amazon.event.ruler.ComparableNumber");
        Method generate = clazz.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("C7230489E80000", result);
    }

    @Test
    @DisplayName("generate returns minimal nonzero hex when f = -4999999999.999999 (just above lower bound)")
    void test_TC06() throws Exception {
        double f = -4_999_999_999.999999;
        Class<?> clazz = Class.forName("software.amazon.event.ruler.ComparableNumber");
        Method generate = clazz.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("00000000000001", result);
    }

    @Test
    @DisplayName("generate produces a 14-character hex string of valid hex digits when f = 123456789.0")
    void test_TC07() throws Exception {
        double f = 123_456_789.0;
        Class<?> clazz = Class.forName("software.amazon.event.ruler.ComparableNumber");
        Method generate = clazz.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertAll(
            () -> assertEquals(14, result.length()),
            () -> assertTrue(result.matches("[0-9A-F]{14}"), "Result should be 14 hex digits")
        );
    }

    @Test
    @DisplayName("generate produces a 14-character hex string of valid hex digits when f = -123.456 (negative mid)")
    void test_TC08() throws Exception {
        double f = -123.456;
        Class<?> clazz = Class.forName("software.amazon.event.ruler.ComparableNumber");
        Method generate = clazz.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertAll(
            () -> assertEquals(14, result.length()),
            () -> assertTrue(result.matches("[0-9A-F]{14}"), "Result should be 14 hex digits")
        );
    }
}