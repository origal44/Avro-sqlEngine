package com.avrosql.parser.ast;

/**
 * A constant value, already converted by the Parser so a bad number fails before any data is read.
 * The value is a Long, a Double, a String, or null for SQL NULL. Integer is not allowed:
 * {@code new Literal(30)} and {@code new Literal(30L)} would not be equal.
 */
public record Literal(Object value) implements Expr {

    public Literal {
        if (value != null && !(value instanceof Long || value instanceof Double || value instanceof String)) {
            throw new IllegalArgumentException("unsupported literal type: " + value.getClass().getSimpleName());
        }
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitLiteral(this);
    }
}
