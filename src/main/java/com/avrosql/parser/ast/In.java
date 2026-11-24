package com.avrosql.parser.ast;

import java.util.List;
import java.util.Objects;

// NOT IN is Unary(NOT, In(...)).
public record In(Expr value, List<Expr> values, Position position) implements Expr {

    public In {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(value, "value");
        values = List.copyOf(values);
        if (values.isEmpty()) {
            throw new IllegalArgumentException("IN list must not be empty");
        }
    }

    public In(Expr value, List<Expr> values) {
        this(value, values, Position.UNKNOWN);
    }
}
