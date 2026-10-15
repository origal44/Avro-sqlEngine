package com.avrosql.lexer.token;

import com.avrosql.lexer.Lexer;

import java.util.Objects;

/**
 * One unit of the Lexer's output.
 *
 * @param type   the token's category
 * @param text   the exact characters from the source (empty for EOF)
 * @param line   1-based line where the token starts
 * @param column 1-based column where the token starts
 */
public record Token(TokenType type, String text, int line, int column) {

    public Token {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(text, "text");
        if (line < 1) {
            throw new IllegalArgumentException("line must be >= 1, got " + line);
        }
        if (column < 1) {
            throw new IllegalArgumentException("column must be >= 1, got " + column);
        }
    }
}
