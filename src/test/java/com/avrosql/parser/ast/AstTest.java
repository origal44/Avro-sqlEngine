package com.avrosql.parser.ast;

import com.avrosql.lexer.token.TokenType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AstTest {

    private static final ExprVisitor<String> DESCRIBE = new ExprVisitor<>() {
        @Override
        public String visitColumnRef(ColumnRef c) {
            return "column " + c.name();
        }

        @Override
        public String visitLiteral(Literal l) {
            return "literal " + l.value();
        }

        @Override
        public String visitBinary(Binary b) {
            return "(" + b.left().accept(this) + " " + b.op() + " " + b.right().accept(this) + ")";
        }

        @Override
        public String visitUnary(Unary u) {
            return "(" + u.op() + " " + u.operand().accept(this) + ")";
        }

        @Override
        public String visitIsNull(IsNull n) {
            return "(" + n.operand().accept(this) + " IS NULL)";
        }

        @Override
        public String visitBetween(Between b) {
            return "(" + b.value().accept(this) + " BETWEEN " + b.low().accept(this) + " AND " + b.high().accept(this) + ")";
        }

        @Override
        public String visitIn(In in) {
            List<String> values = in.values().stream().map(v -> v.accept(this)).toList();
            return "(" + in.value().accept(this) + " IN " + values + ")";
        }

        @Override
        public String visitAggregate(Aggregate a) {
            return a.function() + "(" + a.argument().map(arg -> arg.accept(this)).orElse("*") + ")";
        }
    };

    @Test
    void visitorDispatchesOnRuntimeType() {
        Expr column = new ColumnRef("age");
        Expr literal = new Literal(30L);

        assertEquals("column age", column.accept(DESCRIBE));
        assertEquals("literal 30", literal.accept(DESCRIBE));
    }

    @Test
    void visitorWalksIntoBinaryChildren() {
        Expr sum = new Binary(new ColumnRef("age"), TokenType.PLUS, new Literal(1L));
        assertEquals("(column age PLUS literal 1)", sum.accept(DESCRIBE));
    }

    @Test
    void visitorWalksIntoUnaryOperand() {
        Expr negated = new Unary(TokenType.NOT, new ColumnRef("active"));
        assertEquals("(NOT column active)", negated.accept(DESCRIBE));
    }

    @Test
    void visitorWalksIntoIsNullOperand() {
        Expr check = new IsNull(new ColumnRef("email"));
        assertEquals("(column email IS NULL)", check.accept(DESCRIBE));
        assertThrows(NullPointerException.class, () -> new IsNull(null));
    }

    @Test
    void visitorWalksIntoBetweenParts() {
        Expr range = new Between(new ColumnRef("age"), new Literal(18L), new Literal(30L));
        assertEquals("(column age BETWEEN literal 18 AND literal 30)", range.accept(DESCRIBE));
        assertThrows(NullPointerException.class, () -> new Between(null, new Literal(1L), new Literal(2L)));
    }

    @Test
    void visitorWalksIntoInValues() {
        Expr in = new In(new ColumnRef("id"), List.of(new Literal(1L), new Literal(2L)));
        assertEquals("(column id IN [literal 1, literal 2])", in.accept(DESCRIBE));
    }

    @Test
    void inListIsCopiedAndMustNotBeEmpty() {
        List<Expr> values = new ArrayList<>(List.of(new Literal(1L)));
        In in = new In(new ColumnRef("id"), values);
        values.add(new Literal(2L));

        assertEquals(1, in.values().size());
        assertThrows(IllegalArgumentException.class, () -> new In(new ColumnRef("id"), List.of()));
    }

    @Test
    void visitorWalksIntoAggregateArgument() {
        assertEquals("COUNT(*)", new Aggregate(Aggregate.Function.COUNT, Optional.empty()).accept(DESCRIBE));
        assertEquals("SUM(column price)",
                new Aggregate(Aggregate.Function.SUM, Optional.of(new ColumnRef("price"))).accept(DESCRIBE));
    }

    @Test
    void onlyCountCanTakeStar() {
        assertThrows(IllegalArgumentException.class, () -> new Aggregate(Aggregate.Function.SUM, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new Aggregate(null, Optional.empty()));
    }

    @Test
    void unaryRejectsMissingParts() {
        assertThrows(NullPointerException.class, () -> new Unary(null, new Literal(1L)));
        assertThrows(NullPointerException.class, () -> new Unary(TokenType.MINUS, null));
    }

    @Test
    void binaryRejectsMissingParts() {
        Expr one = new Literal(1L);
        assertThrows(NullPointerException.class, () -> new Binary(null, TokenType.PLUS, one));
        assertThrows(NullPointerException.class, () -> new Binary(one, null, one));
        assertThrows(NullPointerException.class, () -> new Binary(one, TokenType.PLUS, null));
    }

    @Test
    void columnRefKeepsCase() {
        assertNotEquals(new ColumnRef("Name"), new ColumnRef("name"));
    }

    @Test
    void columnRefRejectsBlankOrNullName() {
        assertThrows(IllegalArgumentException.class, () -> new ColumnRef(" "));
        assertThrows(NullPointerException.class, () -> new ColumnRef(null));
    }

    @Test
    void literalAcceptsSupportedTypes() {
        assertEquals(30L, new Literal(30L).value());
        assertEquals(3.5, new Literal(3.5).value());
        assertEquals("Haifa", new Literal("Haifa").value());
        assertNull(new Literal(null).value());
    }

    @Test
    void literalRejectsInteger() {
        // 30 is an Integer, and Literal(30) would never equal Literal(30L)
        assertThrows(IllegalArgumentException.class, () -> new Literal(30));
    }

    @Test
    void literalRejectsOtherTypes() {
        assertThrows(IllegalArgumentException.class, () -> new Literal(true));
        assertThrows(IllegalArgumentException.class, () -> new Literal(new Object()));
    }

    @Test
    void selectAllHasNoItems() {
        SelectStatement s = SelectStatement.all("users");
        assertTrue(s.selectAll());
        assertFalse(s.distinct());
        assertTrue(s.items().isEmpty());
        assertEquals("users", s.table());
    }

    @Test
    void cannotSelectAllAndListItems() {
        // the shape of "SELECT name, * FROM users"
        List<SelectItem> items = List.of(SelectItem.of(new ColumnRef("name")));
        assertThrows(IllegalArgumentException.class,
                () -> new SelectStatement(false, true, items, "users", Optional.empty(), List.of(), Optional.empty(), List.of(),
                        Optional.empty(), Optional.empty()));
    }

    @Test
    void withDistinctKeepsEverythingElse() {
        Expr condition = new Binary(new ColumnRef("age"), TokenType.GREATER, new Literal(30L));
        SelectStatement s = SelectStatement.all("users").withWhere(condition).withDistinct();
        assertTrue(s.distinct());
        assertTrue(s.selectAll());
        assertEquals(Optional.of(condition), s.where());
    }

    @Test
    void selectItemAlias() {
        assertEquals(Optional.empty(), SelectItem.of(new ColumnRef("a")).alias());
        assertEquals(Optional.of("total"), SelectItem.as(new ColumnRef("a"), "total").alias());
        assertThrows(IllegalArgumentException.class, () -> SelectItem.as(new ColumnRef("a"), " "));
        assertThrows(NullPointerException.class, () -> SelectItem.of(null));
    }

    @Test
    void whereIsEmptyUntilSet() {
        Expr condition = new Binary(new ColumnRef("age"), TokenType.GREATER, new Literal(30L));
        SelectStatement plain = SelectStatement.all("users");
        SelectStatement filtered = plain.withWhere(condition);

        assertTrue(plain.where().isEmpty());
        assertEquals(Optional.of(condition), filtered.where());
        assertTrue(filtered.selectAll());
        assertThrows(NullPointerException.class,
                () -> new SelectStatement(false, true, List.of(), "users", null, List.of(), Optional.empty(), List.of(),
                        Optional.empty(), Optional.empty()));
    }

    @Test
    void selectListMustNotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> SelectStatement.of(List.of(), "users"));
    }

    @Test
    void itemsAreCopiedAndImmutable() {
        List<SelectItem> items = new ArrayList<>(List.of(SelectItem.of(new ColumnRef("name"))));
        SelectStatement s = SelectStatement.of(items, "users");

        items.add(SelectItem.of(new ColumnRef("age")));

        assertEquals(1, s.items().size());
        assertThrows(UnsupportedOperationException.class, () -> s.items().add(SelectItem.of(new ColumnRef("x"))));
    }

    @Test
    void treesWithSameShapeAreEqual() {
        List<SelectItem> items = List.of(SelectItem.of(new ColumnRef("name")), SelectItem.as(new Literal(1L), "one"));
        SelectStatement a = SelectStatement.of(items, "users");
        SelectStatement b = SelectStatement.of(List.copyOf(items), "users");
        assertEquals(a, b);
        assertNotEquals(a, SelectStatement.of(List.of(SelectItem.of(new ColumnRef("name"))), "users"));
        assertNotEquals(a, a.withDistinct());
    }
    @Test
    void havingNeedsGroupBy() {
        Expr condition = new Binary(new ColumnRef("a"), TokenType.GREATER, new Literal(1L));
        assertThrows(IllegalArgumentException.class, () -> SelectStatement.all("t").withHaving(condition));
        SelectStatement grouped = SelectStatement.all("t").withGroupBy(List.of(new ColumnRef("a"))).withHaving(condition);
        assertEquals(Optional.of(condition), grouped.having());
    }

    @Test
    void groupByIsCopiedAndImmutable() {
        List<Expr> keys = new ArrayList<>(List.of(new ColumnRef("city")));
        SelectStatement s = SelectStatement.all("users").withGroupBy(keys);
        keys.add(new ColumnRef("age"));
        assertEquals(1, s.groupBy().size());
        assertThrows(UnsupportedOperationException.class, () -> s.groupBy().add(new ColumnRef("x")));
    }
    @Test
    void orderItemDirections() {
        assertEquals(OrderItem.Direction.ASC, OrderItem.asc(new ColumnRef("a")).direction());
        assertEquals(OrderItem.Direction.DESC, OrderItem.desc(new ColumnRef("a")).direction());
        assertThrows(NullPointerException.class, () -> new OrderItem(new ColumnRef("a"), null));
    }

    @Test
    void orderByIsCopiedAndImmutable() {
        List<OrderItem> order = new ArrayList<>(List.of(OrderItem.asc(new ColumnRef("a"))));
        SelectStatement s = SelectStatement.all("t").withOrderBy(order);
        order.add(OrderItem.desc(new ColumnRef("b")));
        assertEquals(1, s.orderBy().size());
        assertThrows(UnsupportedOperationException.class, () -> s.orderBy().add(OrderItem.asc(new ColumnRef("x"))));
    }
    @Test
    void offsetNeedsLimit() {
        assertThrows(IllegalArgumentException.class, () -> SelectStatement.all("t").withOffset(5));
        SelectStatement s = SelectStatement.all("t").withLimit(10).withOffset(5);
        assertEquals(Optional.of(10L), s.limit());
        assertEquals(Optional.of(5L), s.offset());
    }

    @Test
    void limitAndOffsetMustNotBeNegative() {
        assertThrows(IllegalArgumentException.class, () -> SelectStatement.all("t").withLimit(-1));
        assertThrows(IllegalArgumentException.class, () -> SelectStatement.all("t").withLimit(1).withOffset(-1));
    }
}
