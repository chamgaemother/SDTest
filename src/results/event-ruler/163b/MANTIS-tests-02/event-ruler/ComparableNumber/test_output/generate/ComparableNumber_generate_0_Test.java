package software.amazon.event.ruler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ComparableNumber_generate_0_Test {

    @Test
    @DisplayName("f < -5e9 triggers lower-bound IllegalArgumentException branch")
    void test_TC01() throws Exception {
        double f = -5_000_000_000.1;
        Method method = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        method.setAccessible(true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            try {
                method.invoke(null, f);
            } catch (ReflectiveOperationException roe) {
                Throwable cause = roe.getCause();
                if (cause instanceof RuntimeException) {
                    throw cause;
                } else {
                    throw new RuntimeException(cause);
                }
            }
        });
        assertEquals("Value must be between -5000000000.0 and 5000000000.0, inclusive", ex.getMessage());
    }

    @Test
    @DisplayName("f > 5e9 triggers upper-bound IllegalArgumentException branch")
    void test_TC02() throws Exception {
        double f = 5_000_000_000.1;
        Method method = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        method.setAccessible(true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            try {
                method.invoke(null, f);
            } catch (ReflectiveOperationException roe) {
                Throwable cause = roe.getCause();
                if (cause instanceof RuntimeException) {
                    throw cause;
                } else {
                    throw new RuntimeException(cause);
                }
            }
        });
        assertEquals("Value must be between -5000000000.0 and 5000000000.0, inclusive", ex.getMessage());
    }

    @Test
    @DisplayName("f == -5e9 produces minimal value hex string \"00000000000000\"")
    void test_TC03() throws Exception {
        double f = -5_000_000_000.0;
        Method method = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, f);
        assertEquals("00000000000000", result);
    }

    @Test
    @DisplayName("f == 5e9 produces maximal value hex string \"2386F26FC10000\"")
    void test_TC04() throws Exception {
        double f = 5_000_000_000.0;
        Method method = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, f);
        assertEquals("2386F26FC10000", result);
    }

    @Test
    @DisplayName("f = 0.0 produces middle-range hex string of length 14 with valid hex digits")
    void test_TC05() throws Exception {
        double f = 0.0;
        Method method = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, f);
        assertAll(
            () -> assertEquals(14, result.length()),
            () -> assertTrue(result.matches("[0-9A-F]{14}"), "Result should be 14 uppercase hex digits")
        );
    }

    @Test
    @DisplayName("f = 1.234567 produces hex string of length 14 with fractional-precision applied")
    void test_TC06() throws Exception {
        double f = 1.234567;
        Method method = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, f);
        assertAll(
            () -> assertEquals(14, result.length()),
            () -> assertTrue(result.matches("[0-9A-F]{14}"), "Result should be 14 uppercase hex digits")
        );
    }

    @Test
    @DisplayName("f = -1.234567 produces hex string of length 14 with fractional-precision applied")
    void test_TC07() throws Exception {
        double f = -1.234567;
        Method method = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, f);
        assertAll(
            () -> assertEquals(14, result.length()),
            () -> assertTrue(result.matches("[0-9A-F]{14}"), "Result should be 14 uppercase hex digits")
        );
    }
}