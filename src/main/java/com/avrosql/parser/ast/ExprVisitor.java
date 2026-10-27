package com.avrosql.parser.ast;

/**
 * One operation over expressions, with one method per node type.
 * Adding a node adds a method here, so every visitor stops compiling until it handles the new node.
 *
 * @param <R> what the operation returns for each node (e.g. a value, or a type)
 */
public interface ExprVisitor<R> {

    R visitColumnRef(ColumnRef columnRef);

    R visitLiteral(Literal literal);
}
