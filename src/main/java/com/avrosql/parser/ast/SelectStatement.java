package com.avrosql.parser.ast;

import java.util.List;
import java.util.Objects;

/**
 * The root of the AST.
 * {@code *} is not an expression, so it is a flag here and not an item in {@code columns}.
 * A statement either selects all columns or lists at least one, never both,
 * so "SELECT name, *" cannot be represented.
 *
 * @param selectAll true for {@code SELECT *}
 * @param columns   the select list; empty exactly when selectAll is true
 * @param table     the table in the FROM clause
 */
public record SelectStatement(boolean selectAll, List<Expr> columns, String table) {

    public SelectStatement {
        columns = List.copyOf(columns); // also rejects null items
        Objects.requireNonNull(table, "table");
        if (selectAll && !columns.isEmpty()) {
            throw new IllegalArgumentException("SELECT * cannot also list columns");
        }
        if (!selectAll && columns.isEmpty()) {
            throw new IllegalArgumentException("select list must not be empty");
        }
    }

    /** SELECT * FROM table */
    public static SelectStatement all(String table) {
        return new SelectStatement(true, List.of(), table);
    }

    /** SELECT col1, col2, ... FROM table */
    public static SelectStatement of(List<Expr> columns, String table) {
        return new SelectStatement(false, columns, table);
    }
}
