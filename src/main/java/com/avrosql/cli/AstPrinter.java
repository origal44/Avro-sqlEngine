package com.avrosql.cli;

import com.avrosql.parser.ast.Aggregate;
import com.avrosql.parser.ast.Between;
import com.avrosql.parser.ast.Binary;
import com.avrosql.parser.ast.ColumnRef;
import com.avrosql.parser.ast.Expr;
import com.avrosql.parser.ast.ExprVisitor;
import com.avrosql.parser.ast.In;
import com.avrosql.parser.ast.IsNull;
import com.avrosql.parser.ast.Literal;
import com.avrosql.parser.ast.OrderItem;
import com.avrosql.parser.ast.SelectItem;
import com.avrosql.parser.ast.SelectStatement;
import com.avrosql.parser.ast.Unary;

// Prints the tree with one node per line; children are indented under their parent.
public final class AstPrinter implements ExprVisitor<Void> {

    private static final String INDENT = "  ";

    private final StringBuilder out = new StringBuilder();
    private int depth = 0;

    private AstPrinter() {
    }

    public static String print(SelectStatement statement) {
        AstPrinter printer = new AstPrinter();
        printer.statement(statement);
        return printer.out.toString();
    }

    private void statement(SelectStatement s) {
        line("SelectStatement");
        depth++;
        if (s.distinct()) {
            line("DISTINCT");
        }
        if (s.selectAll()) {
            line("SELECT *");
        } else {
            line("SELECT");
            nested(() -> s.items().forEach(this::item));
        }
        line("FROM " + s.table());
        s.where().ifPresent(condition -> clause("WHERE", condition));
        if (!s.groupBy().isEmpty()) {
            line("GROUP BY");
            nested(() -> s.groupBy().forEach(this::expr));
        }
        s.having().ifPresent(condition -> clause("HAVING", condition));
        if (!s.orderBy().isEmpty()) {
            line("ORDER BY");
            nested(() -> s.orderBy().forEach(this::order));
        }
        s.limit().ifPresent(rows -> line("LIMIT " + rows));
        s.offset().ifPresent(rows -> line("OFFSET " + rows));
        depth--;
    }

    private void item(SelectItem item) {
        if (item.alias().isEmpty()) {
            expr(item.expression());
            return;
        }
        line("AS " + item.alias().get());
        nested(() -> expr(item.expression()));
    }

    private void order(OrderItem item) {
        line(item.direction().name());
        nested(() -> expr(item.expression()));
    }

    private void clause(String name, Expr condition) {
        line(name);
        nested(() -> expr(condition));
    }

    private void expr(Expr expression) {
        expression.accept(this);
    }

    private void nested(Runnable children) {
        depth++;
        children.run();
        depth--;
    }

    private void line(String text) {
        out.append(INDENT.repeat(depth)).append(text).append(System.lineSeparator());
    }

    @Override
    public Void visitColumnRef(ColumnRef c) {
        line("Column " + c.name());
        return null;
    }

    @Override
    public Void visitLiteral(Literal l) {
        Object value = l.value();
        line("Literal " + (value instanceof String text ? "'" + text + "'" : String.valueOf(value).toUpperCase()));
        return null;
    }

    @Override
    public Void visitBinary(Binary b) {
        line("Binary " + b.op());
        nested(() -> {
            expr(b.left());
            expr(b.right());
        });
        return null;
    }

    @Override
    public Void visitUnary(Unary u) {
        line("Unary " + u.op());
        nested(() -> expr(u.operand()));
        return null;
    }

    @Override
    public Void visitIsNull(IsNull n) {
        line("IsNull");
        nested(() -> expr(n.operand()));
        return null;
    }

    @Override
    public Void visitBetween(Between b) {
        line("Between");
        nested(() -> {
            expr(b.value());
            expr(b.low());
            expr(b.high());
        });
        return null;
    }

    @Override
    public Void visitIn(In in) {
        line("In");
        nested(() -> {
            expr(in.value());
            in.values().forEach(this::expr);
        });
        return null;
    }

    @Override
    public Void visitAggregate(Aggregate a) {
        if (a.argument().isEmpty()) {
            line("Aggregate " + a.function() + "(*)");
            return null;
        }
        line("Aggregate " + a.function());
        nested(() -> expr(a.argument().get()));
        return null;
    }
}
