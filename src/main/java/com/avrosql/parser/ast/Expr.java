package com.avrosql.parser.ast;

/**
 * Any expression in a query: a column, a literal, and later operators and function calls.
 * Nodes hold data only; every operation on them (validation, evaluation) is an {@link ExprVisitor}.
 * Sealed, so no node can exist that the visitors don't know about.
 */
public sealed interface Expr permits ColumnRef, Literal {

    /** Calls the visitor method that matches this node's type. */
    <R> R accept(ExprVisitor<R> visitor);
}
