package com.avrosql.lexer;

/**
 * Character classes shared by the Lexer and the readers.
 * ASCII only, matching Avro's name rule [A-Za-z_][A-Za-z0-9_]*.
 */
public final class Chars {

    public static final char NEWLINE = '\n';

    private Chars() {
    }

    public static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == NEWLINE;
    }

    public static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
