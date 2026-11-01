package com.avrosql.parser.ast;

import com.avrosql.lexer.token.TokenType;

import java.util.Objects;

public record Unary(TokenType op, Expr operand) implements Expr {

    public Unary {
        Objects.requireNonNull(op, "op");
        Objects.requireNonNull(operand, "operand");
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitUnary(this);
    }
}
