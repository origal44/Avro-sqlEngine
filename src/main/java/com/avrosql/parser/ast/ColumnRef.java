package com.avrosql.parser.ast;

import java.util.Objects;

public record ColumnRef(String name, Position position) implements Expr {

    public ColumnRef {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("column name must not be blank");
        }
    }

    public ColumnRef(String name) {
        this(name, Position.UNKNOWN);
    }
}
