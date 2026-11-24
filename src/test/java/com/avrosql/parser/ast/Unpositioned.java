package com.avrosql.parser.ast;

import java.util.Optional;

// Copies a tree with every position set to UNKNOWN, so tests compare structure only.
public final class Unpositioned {

    private Unpositioned() {
    }

    public static SelectStatement of(SelectStatement s) {
        return new SelectStatement(s.distinct(), s.selectAll(),
                s.items().stream().map(i -> new SelectItem(of(i.expression()), i.alias())).toList(),
                s.table(), s.where().map(Unpositioned::of), s.groupBy().stream().map(Unpositioned::of).toList(),
                s.having().map(Unpositioned::of),
                s.orderBy().stream().map(o -> new OrderItem(of(o.expression()), o.direction())).toList(),
                s.limit(), s.offset());
    }

    public static Expr of(Expr expr) {
        return switch (expr) {
            case ColumnRef c -> new ColumnRef(c.name());
            case Literal l -> new Literal(l.value());
            case Binary b -> new Binary(of(b.left()), b.op(), of(b.right()));
            case Unary u -> new Unary(u.op(), of(u.operand()));
            case IsNull n -> new IsNull(of(n.operand()));
            case Between b -> new Between(of(b.value()), of(b.low()), of(b.high()));
            case In in -> new In(of(in.value()), in.values().stream().map(Unpositioned::of).toList());
            case Aggregate a -> new Aggregate(a.function(), a.argument().map(Unpositioned::of));
        };
    }
}
