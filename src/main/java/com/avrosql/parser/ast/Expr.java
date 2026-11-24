package com.avrosql.parser.ast;

public sealed interface Expr permits ColumnRef, Literal, Binary, Unary, IsNull, Between, In, Aggregate {

    // where an error about this node should point
    Position position();
}
