package com.avrosql.parser.ast;

// Long, Double, String or null. Not Integer: Literal(30) wouldn't equal Literal(30L).
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
