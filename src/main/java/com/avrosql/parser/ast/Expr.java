package com.avrosql.parser.ast;

public sealed interface Expr permits ColumnRef, Literal, Binary, Unary, IsNull, Between, In, Aggregate {
}
