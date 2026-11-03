package com.avrosql.lexer;

public final class Cursor {

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

    public char peek() {
        return isAtEnd() ? END : source.charAt(current);
    }

    public char peekNext() {
        return current + 1 >= source.length() ? END : source.charAt(current + 1);
    }

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
