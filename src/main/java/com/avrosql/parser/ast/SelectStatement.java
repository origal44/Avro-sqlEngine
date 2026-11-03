package com.avrosql.parser.ast;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

// '*' is the selectAll flag, not an item. An empty groupBy or orderBy means the clause is absent.
public record SelectStatement(boolean distinct, boolean selectAll, List<SelectItem> items, String table,
                              Optional<Expr> where, List<Expr> groupBy, Optional<Expr> having,
                              List<OrderItem> orderBy) {

    public SelectStatement {
        items = List.copyOf(items); // also rejects null items
        groupBy = List.copyOf(groupBy);
        orderBy = List.copyOf(orderBy);
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(where, "where");
        Objects.requireNonNull(having, "having");
        if (selectAll && !items.isEmpty()) {
            throw new IllegalArgumentException("SELECT * cannot also list columns");
        }
        if (!selectAll && items.isEmpty()) {
            throw new IllegalArgumentException("select list must not be empty");
        }
        if (having.isPresent() && groupBy.isEmpty()) {
            throw new IllegalArgumentException("HAVING needs GROUP BY");
        }
    }

    public static SelectStatement all(String table) {
        return new SelectStatement(false, true, List.of(), table, Optional.empty(), List.of(), Optional.empty(),
                List.of());
    }

    public static SelectStatement of(List<SelectItem> items, String table) {
        return new SelectStatement(false, false, items, table, Optional.empty(), List.of(), Optional.empty(),
                List.of());
    }

    public SelectStatement withDistinct() {
        return new SelectStatement(true, selectAll, items, table, where, groupBy, having, orderBy);
    }

    public SelectStatement withWhere(Expr condition) {
        return new SelectStatement(distinct, selectAll, items, table, Optional.of(condition), groupBy, having,
                orderBy);
    }

    public SelectStatement withGroupBy(List<Expr> keys) {
        return new SelectStatement(distinct, selectAll, items, table, where, keys, having, orderBy);
    }

    public SelectStatement withHaving(Expr condition) {
        return new SelectStatement(distinct, selectAll, items, table, where, groupBy, Optional.of(condition),
                orderBy);
    }

    public SelectStatement withOrderBy(List<OrderItem> order) {
        return new SelectStatement(distinct, selectAll, items, table, where, groupBy, having, order);
    }
}
