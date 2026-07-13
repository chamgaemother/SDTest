package software.amazon.event.ruler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ComparableNumber_generate_0_Test {

    @Test
    @DisplayName("generate(f < -5e9) throws IllegalArgumentException for value below minimum")
    public void test_TC01() throws Exception {
        double f = -5_000_000_000.1;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            try {
                generate.invoke(null, f);
            } catch (InvocationTargetException ite) {
                throw ite.getCause();
            }
        });
        assertEquals("Value must be between -5000000000.0 and 5000000000.0, inclusive", ex.getMessage());
    }

    @Test
    @DisplayName("generate(f > 5e9) throws IllegalArgumentException for value above maximum")
    public void test_TC02() throws Exception {
        double f = 5_000_000_000.1;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            try {
                generate.invoke(null, f);
            } catch (InvocationTargetException ite) {
                throw ite.getCause();
            }
        });
        assertEquals("Value must be between -5000000000.0 and 5000000000.0, inclusive", ex.getMessage());
    }

    @Test
    @DisplayName("generate(f = -5e9) returns all-zero hex string at lower boundary")
    public void test_TC03() throws Exception {
        double f = -5_000_000_000.0;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("00000000000000", result);
    }

    @Test
    @DisplayName("generate(f = 5e9) returns hex string for max value at upper boundary")
    public void test_TC04() throws Exception {
        double f = 5_000_000_000.0;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("2386F26FC10000", result);
    }

    @Test
    @DisplayName("generate(f = 0.0) returns mid-range hex string")
    public void test_TC05() throws Exception {
        double f = 0.0;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("1192D02C680000", result);
    }

    @Test
    @DisplayName("generate(f = -5e9 + 1e-6) returns smallest non-zero hex string")
    public void test_TC06() throws Exception {
        double f = -5_000_000_000.0 + 0.000001;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("00000000000001", result);
    }
}