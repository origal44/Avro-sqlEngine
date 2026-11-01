package com.avrosql.parser.ast;

import java.util.Objects;
import java.util.Optional;

// COUNT(*) has no argument: Aggregate(COUNT, Optional.empty()).
public record Aggregate(Function function, Optional<Expr> argument) implements Expr {

    public enum Function { COUNT, SUM, AVG, MIN, MAX }

    public Aggregate {
        Objects.requireNonNull(function, "function");
        Objects.requireNonNull(argument, "argument");
        if (argument.isEmpty() && function != Function.COUNT) {
            throw new IllegalArgumentException("only COUNT can take *");
        }
    }

    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitAggregate(this);
    }
}
