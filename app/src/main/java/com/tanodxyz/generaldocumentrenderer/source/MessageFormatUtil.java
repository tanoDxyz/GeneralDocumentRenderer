package com.tanodxyz.generaldocumentrenderer.source;


import java.text.MessageFormat;
import java.util.Locale;

public final class MessageFormatUtil {

    private MessageFormatUtil() {
        // Empty constructor.
    }

    public static String format(String pattern, Object... arguments) {
        return new MessageFormat(pattern, Locale.ROOT).format(arguments);
    }
}