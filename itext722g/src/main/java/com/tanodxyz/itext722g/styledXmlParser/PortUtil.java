package com.tanodxyz.itext722g.styledXmlParser;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.regex.Pattern;

/**
 * This file is a helper class for internal usage only.
 * Be aware that its API and functionality may be changed in future.
 */
public class PortUtil {

    /**
     * Instantiates a {@link PortUtil} instance.
     */
    private PortUtil() {
    }

    /**
     * Wraps a {@link Reader} instance in a {@link BufferedReader}.
     *
     * @param inputStreamReader the original reader
     * @return the buffered reader
     */
    public static Reader wrapInBufferedReader(Reader inputStreamReader) {
        return new BufferedReader(inputStreamReader);
    }

    /**
     * By default "." symbol in regular expressions does not match line terminators.
     * The issue is more complicated by the fact that "." does not match only "\n" in C#, while it does not
     * match several other characters as well in Java.
     * This utility method creates a pattern in which dots match any character, including line terminators
     * @param regex regular expression string
     * @return pattern in which dot characters match any Unicode char, including line terminators
     */
    public static Pattern createRegexPatternWithDotMatchingNewlines(String regex) {
        return Pattern.compile(regex, Pattern.DOTALL);
    }

}