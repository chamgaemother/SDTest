package org.jsoup.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_1_Test {

    @Test
    @DisplayName("Invoking private tagSet(\"div\") returns true for known HTML tag")
    public void test_TC01() throws Exception {
        // GIVEN a new HTML Parser
        Parser parser = Parser.htmlParser();
        // Use reflection to access the private tagSet method
        Method tagSet = Parser.class.getDeclaredMethod("tagSet", String.class);
        tagSet.setAccessible(true);
        String tag = "div"; // known HTML tag, should be recognized
        
        // WHEN invoking tagSet on a known tag
        boolean result = (boolean) tagSet.invoke(parser, tag);
        
        // THEN expect true since "div" is a standard HTML tag (path B0→B1→B3→B5)
        assertEquals(true, result);
    }

    @Test
    @DisplayName("Invoking private tagSet(\"unknownTag\") returns false for unrecognized tag")
    public void test_TC02() throws Exception {
        // GIVEN a new HTML Parser
        Parser parser = Parser.htmlParser();
        // Use reflection to access the private tagSet method
        Method tagSet = Parser.class.getDeclaredMethod("tagSet", String.class);
        tagSet.setAccessible(true);
        String tag = "unknownTag"; // not in HTML spec, should not be recognized
        
        // WHEN invoking tagSet on an unrecognized tag
        boolean result = (boolean) tagSet.invoke(parser, tag);
        
        // THEN expect false since "unknownTag" is not in the known tag set (path B0→B1→B4→B6)
        assertEquals(false, result);
    }

    @Test
    @DisplayName("Invoking private tagSet(null) throws NullPointerException")
    public void test_TC03() throws Exception {
        // GIVEN a new XML Parser
        Parser parser = Parser.xmlParser();
        // Use reflection to access the private tagSet method
        Method tagSet = Parser.class.getDeclaredMethod("tagSet", String.class);
        tagSet.setAccessible(true);
        
        // WHEN invoking tagSet with null input, should throw InvocationTargetException wrapping NullPointerException
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> {
            tagSet.invoke(parser, (Object) null);
        });
        
        // THEN the cause should be a NullPointerException (path B0→B1→B2→B7)
        assertNotNull(thrown.getCause());
        assertTrue(thrown.getCause() instanceof NullPointerException, 
                   "Expected cause to be NullPointerException");
    }
}