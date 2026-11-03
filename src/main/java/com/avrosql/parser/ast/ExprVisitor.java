package com.avrosql.parser.ast;

public interface ExprVisitor<R> {

    R visitColumnRef(ColumnRef columnRef);

    R visitLiteral(Literal literal);

    R visitBinary(Binary binary);

    R visitUnary(Unary unary);

    R visitIsNull(IsNull isNull);

    R visitBetween(Between between);

    R visitIn(In in);

    R visitAggregate(Aggregate aggregate);
}
