package org.jsoup.parser;

import org.jsoup.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;
public class Parser_tagSet_2_Test {

    @Test
    @DisplayName("TC03: Invoking tagSet twice replaces the parser.settings and updates treeBuilder.parser reference each time")
    public void test_TC03() throws Exception {
        // GIVEN a fresh HTML parser and two distinct ParseSettings instances
        Parser parser = Parser.htmlParser();
        Method tagSet = Parser.class.getDeclaredMethod("tagSet", ParseSettings.class);
        tagSet.setAccessible(true);
        ParseSettings s1 = new ParseSettings(false, true);
        ParseSettings s2 = new ParseSettings(true, false);

        // WHEN first invocation: exercise B0→B1→B2→B4, replacing settings once
        Parser ret1 = (Parser) tagSet.invoke(parser, s1);
        // THEN returned parser is same instance and settings updated to s1
        assertSame(parser, ret1, "tagSet should return the same parser instance on first call");
        assertSame(s1, parser.settings(), "Parser.settings() should reference the first passed settings");

        // WHEN second invocation: again B0→B1→B2→B4, replacing settings a second time
        Parser ret2 = (Parser) tagSet.invoke(parser, s2);
        // THEN returned parser is same instance and settings updated to s2
        assertSame(parser, ret2, "tagSet should return the same parser instance on second call");
        assertSame(s2, parser.settings(), "Parser.settings() should reference the second passed settings");
    }

    @Test
    @DisplayName("TC04: Calling public settings(ParseSettings) delegates to tagSet and returns this parser instance")
    public void test_TC04() {
        // GIVEN a fresh HTML parser and a new ParseSettings for chaining
        Parser parser = Parser.htmlParser();
        ParseSettings newSettings = new ParseSettings(true, true);

        // WHEN invoking the public settings() setter: B0→B1→B2→B4 via delegate
        Parser returned = parser.settings(newSettings);
        // THEN the same parser instance is returned and settings refer to the provided one
        assertSame(parser, returned, "settings(ParseSettings) should return this parser instance");
        assertSame(newSettings, parser.settings(), "Parser.settings() should reference the provided settings");
    }

    @Test
    @DisplayName("TC05: Reflection invocation of tagSet with unsupported subclass of ParseSettings still assigns provided instance")
    public void test_TC05() throws Exception {
        // GIVEN a subclass of ParseSettings and a fresh HTML parser
        class MySettings extends ParseSettings {
            MySettings() { super(true, false); }
        }
        ParseSettings subSettings = new MySettings();
        Parser parser = Parser.htmlParser();

        // Use reflection to access private tagSet to cover B0→B1→B2→B4
        Method tagSet = Parser.class.getDeclaredMethod("tagSet", ParseSettings.class);
        tagSet.setAccessible(true);

        // WHEN invoking tagSet with the subclass instance
        Parser result = (Parser) tagSet.invoke(parser, subSettings);
        // THEN parser instance is returned and settings updated to the subclass instance
        assertSame(parser, result, "Reflection tagSet should return the same parser instance");
        assertSame(subSettings, parser.settings(), "Parser.settings() should reference the provided subclass instance");
    }
}