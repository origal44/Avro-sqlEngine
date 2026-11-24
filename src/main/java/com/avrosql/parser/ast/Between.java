package com.avrosql.parser.ast;

import java.util.Objects;

public record Between(Expr value, Expr low, Expr high, Position position) implements Expr {

    public Between {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(low, "low");
        Objects.requireNonNull(high, "high");
    }

    public Between(Expr value, Expr low, Expr high) {
        this(value, low, high, Position.UNKNOWN);
    }
}
