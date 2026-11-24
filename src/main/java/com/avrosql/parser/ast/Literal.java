package com.avrosql.parser.ast;

import java.util.Objects;

// Long, Double, String, Boolean or null. Not Integer: Literal(30) wouldn't equal Literal(30L).
public record Literal(Object value, Position position) implements Expr {

    public Literal {
        Objects.requireNonNull(position, "position");
        if (value != null && !(value instanceof Long || value instanceof Double || value instanceof String
                || value instanceof Boolean)) {
            throw new IllegalArgumentException("unsupported literal type: " + value.getClass().getSimpleName());
        }
    }

    public Literal(Object value) {
        this(value, Position.UNKNOWN);
    }
}
