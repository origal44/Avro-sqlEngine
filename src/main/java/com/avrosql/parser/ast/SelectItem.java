package com.avrosql.parser.ast;

import java.util.Objects;
import java.util.Optional;

// One entry of the select list: price * qty AS total
public record SelectItem(Expr expression, Optional<String> alias) {

    public SelectItem {
        Objects.requireNonNull(expression, "expression");
        Objects.requireNonNull(alias, "alias");
        if (alias.isPresent() && alias.get().isBlank()) {
            throw new IllegalArgumentException("alias must not be blank");
        }
    }

    public static SelectItem of(Expr expression) {
        return new SelectItem(expression, Optional.empty());
    }

    public static SelectItem as(Expr expression, String alias) {
        return new SelectItem(expression, Optional.of(alias));
    }
}
