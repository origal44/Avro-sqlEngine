package com.avrosql.lexer.exception;

import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.LexicalError;

/**
 * Thrown by {@link Lexer#tokenize()} when the input cannot be turned into tokens.
 * Checked, so every caller of the Lexer has to decide what to do with bad input.
 * Carries the character at the reported position and its line and column.
 */
public final class LexicalException extends Exception implements LexicalError {

    private static final String UNEXPECTED_CHARACTER = "Unexpected character '%c' at line %d, column %d";

    private final char character;
    private final int line;
    private final int column;

    /** A character that cannot start any token. */
    public LexicalException(char character, int line, int column) {
        this(UNEXPECTED_CHARACTER.formatted(character, line, column), character, line, column);
    }

    /** Any other lexical error, with a custom message (e.g. an unterminated string). */
    public LexicalException(String message, char character, int line, int column) {
        super(message);
        this.character = character;
        this.line = line;
        this.column = column;
    }

    @Override
    public char character() {
        return character;
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public int column() {
        return column;
    }
}
