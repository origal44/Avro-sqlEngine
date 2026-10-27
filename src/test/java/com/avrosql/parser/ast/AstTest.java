package com.avrosql.parser.ast;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The minimal AST: node rules, visitor dispatch, and structural equality. */
class AstTest {

    /** Says which visit method ran, to prove the double dispatch picks the right one. */
    private static final ExprVisitor<String> DESCRIBE = new ExprVisitor<>() {
        @Override
        public String visitColumnRef(ColumnRef c) {
            return "column " + c.name();
        }

        @Override
        public String visitLiteral(Literal l) {
            return "literal " + l.value();
        }
    };

    // ---- visitor dispatch ----

    @Test
    void visitorDispatchesOnRuntimeType() {
        Expr column = new ColumnRef("age");
        Expr literal = new Literal(30L);

        assertEquals("column age", column.accept(DESCRIBE));
        assertEquals("literal 30", literal.accept(DESCRIBE));
    }

    // ---- ColumnRef ----

    @Test
    void columnRefKeepsCase() {
        assertNotEquals(new ColumnRef("Name"), new ColumnRef("name"));
    }

    @Test
    void columnRefRejectsBlankOrNullName() {
        assertThrows(IllegalArgumentException.class, () -> new ColumnRef(" "));
        assertThrows(NullPointerException.class, () -> new ColumnRef(null));
    }

    // ---- Literal ----

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

    // ---- SelectStatement ----

    @Test
    void selectAllHasNoColumns() {
        SelectStatement s = SelectStatement.all("users");
        assertTrue(s.selectAll());
        assertTrue(s.columns().isEmpty());
        assertEquals("users", s.table());
    }

    @Test
    void cannotSelectAllAndListColumns() {
        // the shape of "SELECT name, * FROM users"
        assertThrows(IllegalArgumentException.class,
                () -> new SelectStatement(true, List.of(new ColumnRef("name")), "users"));
    }

    @Test
    void selectListMustNotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> SelectStatement.of(List.of(), "users"));
    }

    @Test
    void columnsAreCopiedAndImmutable() {
        List<Expr> columns = new ArrayList<>(List.of(new ColumnRef("name")));
        SelectStatement s = SelectStatement.of(columns, "users");

        columns.add(new ColumnRef("age"));

        assertEquals(1, s.columns().size());
        assertThrows(UnsupportedOperationException.class, () -> s.columns().add(new ColumnRef("x")));
    }

    @Test
    void treesWithSameShapeAreEqual() {
        // this is what lets parser tests compare a whole expected tree with one assertEquals
        SelectStatement a = SelectStatement.of(List.of(new ColumnRef("name"), new Literal(1L)), "users");
        SelectStatement b = SelectStatement.of(List.of(new ColumnRef("name"), new Literal(1L)), "users");
        assertEquals(a, b);
        assertNotEquals(a, SelectStatement.of(List.of(new ColumnRef("name")), "users"));
    }
}
