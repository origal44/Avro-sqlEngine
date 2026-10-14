package com.avrosql.lexer;

/**
 * Thrown when the Lexer cannot turn the input into tokens.
 * Carries the character at the reported position and its line and column.
 */
public final class LexicalException extends RuntimeException {

    private final char character;
    private final int line;
    private final int column;

    /** A character that cannot start any token. */
    public LexicalException(char character, int line, int column) {
        this("Unexpected character '" + character + "' at line " + line + ", column " + column,
                character, line, column);
    }

    /** Any other lexical error, with a custom message (e.g. an unterminated string). */
    public LexicalException(String message, char character, int line, int column) {
        super(message);
        this.character = character;
        this.line = line;
        this.column = column;
    }

    public char character() {
        return character;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }
}
