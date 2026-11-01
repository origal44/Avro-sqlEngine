package com.avrosql.parser.ast;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

// '*' is the selectAll flag, not an item.
public record SelectStatement(boolean distinct, boolean selectAll, List<SelectItem> items, String table,
                              Optional<Expr> where) {

    public SelectStatement {
        items = List.copyOf(items); // also rejects null items
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(where, "where");
        if (selectAll && !items.isEmpty()) {
            throw new IllegalArgumentException("SELECT * cannot also list columns");
        }
        if (!selectAll && items.isEmpty()) {
            throw new IllegalArgumentException("select list must not be empty");
        }
    }

    public static SelectStatement all(String table) {
        return new SelectStatement(false, true, List.of(), table, Optional.empty());
    }

    public static SelectStatement of(List<SelectItem> items, String table) {
        return new SelectStatement(false, false, items, table, Optional.empty());
    }

    public SelectStatement withDistinct() {
        return new SelectStatement(true, selectAll, items, table, where);
    }

    public SelectStatement withWhere(Expr condition) {
        return new SelectStatement(distinct, selectAll, items, table, Optional.of(condition));
    }
}
