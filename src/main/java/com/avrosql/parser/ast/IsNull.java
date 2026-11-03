package com.avrosql.parser.ast;

import java.util.Objects;

// IS NOT NULL is Unary(NOT, IsNull(...)): same meaning, since IS NULL is never NULL itself.
public record IsNull(Expr operand) implements Expr {

    public IsNull {
        Objects.requireNonNull(operand, "operand");
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitIsNull(this);
    }
}
