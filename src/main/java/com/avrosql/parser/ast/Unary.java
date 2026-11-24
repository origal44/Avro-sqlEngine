package com.avrosql.parser.ast;

import com.avrosql.lexer.token.TokenType;

import java.util.Objects;

public record Unary(TokenType op, Expr operand, Position position) implements Expr {

    public Unary {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(op, "op");
        Objects.requireNonNull(operand, "operand");
    }

    public Unary(TokenType op, Expr operand) {
        this(op, operand, Position.UNKNOWN);
    }
}
