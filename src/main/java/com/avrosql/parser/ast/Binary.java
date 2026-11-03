package com.avrosql.parser.ast;

import com.avrosql.lexer.token.TokenType;

import java.util.Objects;

public record Binary(Expr left, TokenType op, Expr right) implements Expr {

    public Binary {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(op, "op");
        Objects.requireNonNull(right, "right");
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitBinary(this);
    }
}
