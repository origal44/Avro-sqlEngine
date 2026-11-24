package com.avrosql.parser.ast;

import com.avrosql.lexer.token.TokenType;

import java.util.Objects;

public record Binary(Expr left, TokenType op, Expr right, Position position) implements Expr {

    public Binary {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(op, "op");
        Objects.requireNonNull(right, "right");
    }

    public Binary(Expr left, TokenType op, Expr right) {
        this(left, op, right, Position.UNKNOWN);
    }
}
