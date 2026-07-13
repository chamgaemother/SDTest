package software.amazon.event.ruler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComparableNumber_generate_0_Test {

    @Test
    @DisplayName("f < -5e9 throws IllegalArgumentException for too small input (branch f < -Constants.FIVE_BILLION)")
    void test_TC01() throws Exception {
        double f = -5_000_000_000.1; // below lower bound to trigger f < -FIVE_BILLION branch
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        Executable call = () -> {
            try {
                generate.invoke(null, f);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, call);
        assertTrue(ex.getMessage().startsWith("Value must be between"),
                "Expected message to start with 'Value must be between'");
    }

    @Test
    @DisplayName("f > 5e9 throws IllegalArgumentException for too large input (branch f > Constants.FIVE_BILLION)")
    void test_TC02() throws Exception {
        double f = 5_000_000_000.1; // above upper bound to trigger f > FIVE_BILLION branch
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        Executable call = () -> {
            try {
                generate.invoke(null, f);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, call);
        assertTrue(ex.getMessage().startsWith("Value must be between"),
                "Expected message to start with 'Value must be between'");
    }

    @Test
    @DisplayName("f == -5e9 returns all-zero hex string (boundary at lower limit)")
    void test_TC03() throws Exception {
        double f = -5_000_000_000.0; // exactly lower bound, should not throw and go through full conversion
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("00000000000000", result,
                "Expected all-zero hex string at lower boundary");
    }

    @Test
    @DisplayName("f == 5e9 returns hex string for 1e10*1e6 (boundary at upper limit)")
    void test_TC04() throws Exception {
        double f = 5_000_000_000.0; // exactly upper bound, should go through conversion without exception
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("2386F26FC10000", result,
                "Expected hex string for max value (01 2386F26FC1 0000 skipping first byte)");
    }

    @Test
    @DisplayName("f == 0 returns midpoint hex string for 5e9*1e6")
    void test_TC05() throws Exception {
        double f = 0.0; // midpoint input, adding FIVE_BILLION yields 5e9, times 1e6 yields midpoint long
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("11456BDCC80000", result,
                "Expected midpoint hex representation for zero input");
    }

    @Test
    @DisplayName("f == 0.5 returns hex string after fractional adjustment (branch through conversion)")
    void test_TC06() throws Exception {
        double f = 0.5; // small positive fraction to test fractional part handling
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("1456BDCCF7A120", result,
                "Expected correct hex for fractional input 0.5");
    }

    @Test
    @DisplayName("f == -123.456 returns hex string for negative fractional input")
    void test_TC07() throws Exception {
        double f = -123.456; // negative fractional to test offset and fraction combined
        Method generate = ComparableNumber.class.getDeclaredMethod("generate", double.class);
        generate.setAccessible(true);
        String result = (String) generate.invoke(null, f);
        assertEquals("11456BDC83E6E0", result,
                "Expected correct hex for negative fractional input -123.456");
    }
}