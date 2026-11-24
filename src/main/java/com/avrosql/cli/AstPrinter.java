package com.avrosql.cli;

import com.avrosql.parser.ast.Aggregate;
import com.avrosql.parser.ast.Between;
import com.avrosql.parser.ast.Binary;
import com.avrosql.parser.ast.ColumnRef;
import com.avrosql.parser.ast.Expr;
import com.avrosql.parser.ast.In;
import com.avrosql.parser.ast.IsNull;
import com.avrosql.parser.ast.Literal;
import com.avrosql.parser.ast.OrderItem;
import com.avrosql.parser.ast.SelectItem;
import com.avrosql.parser.ast.SelectStatement;
import com.avrosql.parser.ast.Unary;

// Prints the tree with one node per line; children are indented under their parent.
public final class AstPrinter {

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

    // no default branch: a new Expr type must fail to compile here until it is handled
    private void expr(Expr expression) {
        switch (expression) {
            case ColumnRef c -> line("Column " + c.name());
            case Literal l -> line("Literal " + literal(l.value()));
            case Binary b -> node("Binary " + b.op(), b.left(), b.right());
            case Unary u -> node("Unary " + u.op(), u.operand());
            case IsNull n -> node("IsNull", n.operand());
            case Between b -> node("Between", b.value(), b.low(), b.high());
            case In in -> {
                line("In");
                nested(() -> {
                    expr(in.value());
                    in.values().forEach(this::expr);
                });
            }
            case Aggregate a -> {
                if (a.argument().isEmpty()) {
                    line("Aggregate " + a.function() + "(*)");
                } else {
                    node("Aggregate " + a.function(), a.argument().get());
                }
            }
        }
    }

    private void node(String label, Expr... children) {
        line(label);
        nested(() -> {
            for (Expr child : children) {
                expr(child);
            }
        });
    }

    private static String literal(Object value) {
        return value instanceof String text ? "'" + text + "'" : String.valueOf(value).toUpperCase();
    }

    private void nested(Runnable children) {
        depth++;
        children.run();
        depth--;
    }

    private void line(String text) {
        out.append(INDENT.repeat(depth)).append(text).append(System.lineSeparator());
    }
}
