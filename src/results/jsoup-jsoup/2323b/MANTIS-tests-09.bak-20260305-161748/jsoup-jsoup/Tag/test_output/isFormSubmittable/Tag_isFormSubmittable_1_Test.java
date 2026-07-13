package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

// Test class targeting the isFormSubmittable method
public class Tag_isFormSubmittable_1_Test {

    @Test
    @DisplayName("valueOf(String,String,ParseSettings) throws NullPointerException when tagName is null")
    void test_TC04() throws Exception {
        Method m = Tag.class.getDeclaredMethod(
            "valueOf", String.class, String.class, String.class, ParseSettings.class);
        m.setAccessible(true);
        String tagName = null;
        String normalName = "irrelevant";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        Executable call = () -> {
            try {
                m.invoke(null, tagName, normalName, namespace, settings);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) throw cause;
                throw e;
            }
        };
        assertThrows(NullPointerException.class, call);
    }

    @Test
    @DisplayName("valueOf(String,String,ParseSettings) throws IllegalArgumentException when tagName is empty after trim")
    void test_TC05() throws Exception {
        Method m = Tag.class.getDeclaredMethod(
            "valueOf", String.class, String.class, String.class, ParseSettings.class);
        m.setAccessible(true);
        String tagName = "   ";
        String normalName = "";
        String namespace = Parser.NamespaceHtml;
        ParseSettings settings = ParseSettings.preserveCase;
        Executable call = () -> {
            try {
                m.invoke(null, tagName, normalName, namespace, settings);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) throw cause;
                throw e;
            }
        };
        assertThrows(IllegalArgumentException.class, call);
    }

    @Test
    @DisplayName("valueOf(String,String,ParseSettings) throws NullPointerException when namespace is null")
    void test_TC06() throws Exception {
        Method m = Tag.class.getDeclaredMethod(
            "valueOf", String.class, String.class, String.class, ParseSettings.class);
        m.setAccessible(true);
        String tagName = "foo";
        String normalName = "foo";
        String namespace = null;
        ParseSettings settings = ParseSettings.preserveCase;
        Executable call = () -> {
            try {
                m.invoke(null, tagName, normalName, namespace, settings);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) throw cause;
                throw e;
            }
        };
        assertThrows(NullPointerException.class, call);
    }

    @Test
    @DisplayName("valueOf(String,ParseSettings) with uppercase known tag and preserveCase=true returns cloned Tag and retains submittable flag")
    void test_TC07() {
        String tagName = "INPUT";
        ParseSettings settings = ParseSettings.preserveCase;
        Tag tag = Tag.valueOf(tagName, settings);
        assertEquals("INPUT", tag.getName(), "Tag name should preserve uppercase when preserveCase=true");
        assertTrue(tag.isFormSubmittable(), "Known 'input' tag should be submittable in forms");
    }
}