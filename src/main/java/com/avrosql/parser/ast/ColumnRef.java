package com.avrosql.parser.ast;

import java.util.Objects;

public record ColumnRef(String name) implements Expr {

    public ColumnRef {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("column name must not be blank");
        }
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitColumnRef(this);
    }
}
