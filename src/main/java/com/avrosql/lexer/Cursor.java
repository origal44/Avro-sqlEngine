package com.avrosql.lexer;

/**
 * Reads the source one character at a time and tracks the current line and column.
 * This is the only class that touches the raw string index.
 */
final class Cursor {

    /** Returned by {@link #peek()} when there are no more characters. */
    static final char END = '\0';

    private final String source;
    private int current = 0;
    private int line = 1;
    private int column = 1;

    Cursor(String source) {
        this.source = source;
    }

    boolean isAtEnd() {
        return current >= source.length();
    }

    /** Returns the current character without consuming it, or END at the end of input. */
    char peek() {
        return isAtEnd() ? END : source.charAt(current);
    }

    /** Consumes and returns the current character, updating line and column. */
    char advance() {
        if (isAtEnd()) {
            throw new IllegalStateException("advance() called at end of input");
        }
        char c = source.charAt(current++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    int line() {
        return line;
    }

    int column() {
        return column;
    }
}
