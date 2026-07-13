package software.amazon.event.ruler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class ComparableNumber_generate_0_Test {

    @Test
    @DisplayName("TC01: generate throws IllegalArgumentException when f < -5e9 (below lower bound)")
    void test_TC01() throws Exception {
        // f < -5e9 triggers the lower bound check and throws IllegalArgumentException
        double f = -5_000_000_000.000001;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        Executable exec = () -> {
            try {
                generate.invoke(null, f);
            } catch (InvocationTargetException e) {
                // unwrap to throw the underlying exception for assertion
                throw e.getCause();
            }
        };
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, exec);
        assertEquals(
            "Value must be between -5000000000.0 and 5000000000.0, inclusive", 
            ex.getMessage(),
            "Expected exception message for value below lower bound"
        );
    }

    @Test
    @DisplayName("TC02: generate throws IllegalArgumentException when f > 5e9 (above upper bound)")
    void test_TC02() throws Exception {
        // f > 5e9 triggers the upper bound check and throws IllegalArgumentException
        double f = 5_000_000_000.0001;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        Executable exec = () -> {
            try {
                generate.invoke(null, f);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, exec);
        assertEquals(
            "Value must be between -5000000000.0 and 5000000000.0, inclusive",
            ex.getMessage(),
            "Expected exception message for value above upper bound"
        );
    }

    @Test
    @DisplayName("TC03: generate returns fourteen-char hex string for f = 0 (mid-range zero)")
    void test_TC03() throws Exception {
        // f=0 is within bounds, should produce the midpoint hex string
        double f = 0.0;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals(
            "116BDE03C00000",
            result,
            "Expected hex string representing zero offset from midpoint"
        );
    }

    @Test
    @DisplayName("TC04: generate returns fourteen-char hex string for f = -5000000000.0 (exact lower bound)")
    void test_TC04() throws Exception {
        // f at exact lower bound maps to zero long, producing all zeros
        double f = -5_000_000_000.0;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals(
            "00000000000000",
            result,
            "Expected hex string of all zeros at lower bound"
        );
    }

    @Test
    @DisplayName("TC05: generate returns fourteen-char hex string for f = 5000000000.0 (exact upper bound)")
    void test_TC05() throws Exception {
        // f at exact upper bound maps to max long, yielding highest hex value
        double f = 5_000_000_000.0;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals(
            "2386F26FC10000",
            result,
            "Expected hex string at upper bound"
        );
    }

    @Test
    @DisplayName("TC06: generate returns expected hex for a small negative integer f = -2.0")
    void test_TC06() throws Exception {
        // small negative integer offset below midpoint branch, should format correctly
        double f = -2.0;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals(
            "116BDE01A7B880",
            result,
            "Expected hex string for f = -2.0"
        );
    }

    @Test
    @DisplayName("TC07: generate returns expected hex for a small positive integer f = 2.0")
    void test_TC07() throws Exception {
        // small positive integer offset above midpoint branch, should format correctly
        double f = 2.0;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals(
            "116BDE05A84880",
            result,
            "Expected hex string for f = 2.0"
        );
    }

    @Test
    @DisplayName("TC08: generate throws IllegalArgumentException for f = NaN (undefined number)")
    void test_TC08() throws Exception {
        // NaN comparison always false for bounds, should treat as out-of-range and throw
        double f = Double.NaN;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        Executable exec = () -> {
            try {
                generate.invoke(null, f);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, exec);
        assertEquals(
            "Value must be between -5000000000.0 and 5000000000.0, inclusive",
            ex.getMessage(),
            "Expected exception message for NaN input"
        );
    }

    @Test
    @DisplayName("TC09: generate throws IllegalArgumentException for f = +Infinity")
    void test_TC09() throws Exception {
        // positive infinity exceeds upper bound, should throw IllegalArgumentException
        double f = Double.POSITIVE_INFINITY;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        Executable exec = () -> {
            try {
                generate.invoke(null, f);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, exec);
        assertEquals(
            "Value must be between -5000000000.0 and 5000000000.0, inclusive",
            ex.getMessage(),
            "Expected exception message for positive infinity"
        );
    }

    @Test
    @DisplayName("TC10: generate throws IllegalArgumentException for f = -Infinity")
    void test_TC10() throws Exception {
        // negative infinity below lower bound, should throw IllegalArgumentException
        double f = Double.NEGATIVE_INFINITY;
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        Executable exec = () -> {
            try {
                generate.invoke(null, f);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, exec);
        assertEquals(
            "Value must be between -5000000000.0 and 5000000000.0, inclusive",
            ex.getMessage(),
            "Expected exception message for negative infinity"
        );
    }
}