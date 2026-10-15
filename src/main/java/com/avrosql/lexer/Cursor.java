package com.avrosql.lexer;

/**
 * Reads the source one character at a time and tracks the current line and column.
 * This is the only class that touches the raw string index.
 */
public final class Cursor {

    /** Returned by {@link #peek()} when there are no more characters. */
    public static final char END = '\0';

    private final String source;
    private int current = 0;
    private int line = 1;
    private int column = 1;

    public Cursor(String source) {
        this.source = source;
    }

    public boolean isAtEnd() {
        return current >= source.length();
    }

    /** Returns the current character without consuming it, or END at the end of input. */
    public char peek() {
        return isAtEnd() ? END : source.charAt(current);
    }

    /** Returns the character after the current one without consuming anything, or END if there is none. */
    public char peekNext() {
        return current + 1 >= source.length() ? END : source.charAt(current + 1);
    }

    /** Consumes and returns the current character, updating line and column. */
    public char advance() {
        if (isAtEnd()) {
            throw new IllegalStateException("advance() called at end of input");
        }
        char c = source.charAt(current++);
        if (c == Chars.NEWLINE) {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }
}
