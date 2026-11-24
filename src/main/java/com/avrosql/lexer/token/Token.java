package com.avrosql.lexer.token;

import com.avrosql.lexer.Lexer;

import java.util.Objects;

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
