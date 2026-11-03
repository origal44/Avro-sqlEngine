package com.avrosql.parser.ast;

import java.util.Objects;

// One entry of ORDER BY: price DESC
public record OrderItem(Expr expression, Direction direction) {

    public enum Direction { ASC, DESC }

    public OrderItem {
        Objects.requireNonNull(expression, "expression");
        Objects.requireNonNull(direction, "direction");
    }

    public static OrderItem asc(Expr expression) {
        return new OrderItem(expression, Direction.ASC);
    }

    public static OrderItem desc(Expr expression) {
        return new OrderItem(expression, Direction.DESC);
    }
}
