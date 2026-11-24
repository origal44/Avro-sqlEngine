package com.avrosql.parser.ast;

import java.util.Objects;

public record Between(Expr value, Expr low, Expr high) implements Expr {

    public Between {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(low, "low");
        Objects.requireNonNull(high, "high");
    }
}
