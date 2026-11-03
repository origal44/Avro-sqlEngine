package com.avrosql.parser;

import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.lexer.token.TokenType;
import com.avrosql.parser.ast.Aggregate;
import com.avrosql.parser.ast.Between;
import com.avrosql.parser.ast.Binary;
import com.avrosql.parser.ast.ColumnRef;
import com.avrosql.parser.ast.Expr;
import com.avrosql.parser.ast.In;
import com.avrosql.parser.ast.IsNull;
import com.avrosql.parser.ast.Literal;
import com.avrosql.parser.ast.SelectItem;
import com.avrosql.parser.ast.SelectStatement;
import com.avrosql.parser.ast.Unary;
import com.avrosql.parser.exception.SyntaxException;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.avrosql.lexer.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

class ParseSelectTest {

    private static SelectStatement parse(String source) throws LexicalException, SyntaxException {
        return new Parser(new Lexer(source).tokenize()).parse();
    }

    private static SyntaxException assertSyntaxError(String source, TokenType expected, String found,
                                                     int line, int column) {
        SyntaxException e = assertThrows(SyntaxException.class, () -> parse(source));
        assertEquals(expected, e.expected());
        assertEquals(line, e.line());
        assertEquals(column, e.column());
        assertTrue(e.getMessage().contains("Expected " + expected), e.getMessage());
        assertTrue(e.getMessage().contains("found " + found), e.getMessage());
        assertTrue(e.getMessage().contains("at line " + line + ", column " + column), e.getMessage());
        return e;
    }

    private static void assertError(String source, String message) {
        SyntaxException e = assertThrows(SyntaxException.class, () -> parse(source));
        assertEquals(message, e.getMessage());
    }

    private static Expr col(String name) {
        return new ColumnRef(name);
    }

    private static Expr num(long value) {
        return new Literal(value);
    }

    private static List<SelectItem> items(Expr... expressions) {
        return Arrays.stream(expressions).map(SelectItem::of).toList();
    }

    @Test
    void singleColumn() throws Exception {
        assertEquals(SelectStatement.of(items(col("name")), "users"),
                parse("SELECT name FROM users;"));
    }

    @Test
    void severalColumnsKeepOrderAndCase() throws Exception {
        assertEquals(SelectStatement.of(items(col("Name"), col("age"), col("city")), "users"),
                parse("select Name, age ,city from users;"));
    }

    @Test
    void selectAll() throws Exception {
        assertEquals(SelectStatement.all("users"), parse("SELECT * FROM users;"));
    }

    @Test
    void spreadOverLines() throws Exception {
        assertEquals(SelectStatement.all("users"), parse("SELECT *\nFROM users\n;\n"));
    }

    @Test
    void missingSemicolon() {
        assertSyntaxError("SELECT name FROM users", SEMICOLON, "end of input (EOF)", 1, 23);
    }

    @Test
    void whereBeforeFromFailsAtExpectFrom() {
        assertSyntaxError("SELECT name WHERE age > 30 FROM users;", FROM, "'WHERE' (WHERE)", 1, 13);
    }

    @Test
    void textAfterSemicolonFailsAtExpectEof() {
        assertSyntaxError("SELECT * FROM users; blabla", EOF, "'blabla' (IDENTIFIER)", 1, 22);
    }

    @Test
    void doubleSemicolonFailsAtExpectEof() {
        assertSyntaxError("SELECT name FROM users;;", EOF, "';' (SEMICOLON)", 1, 24);
    }

    @Test
    void starThenColumnFailsAtExpectFrom() {
        assertSyntaxError("SELECT *, name FROM users;", FROM, "',' (COMMA)", 1, 9);
    }

    @Test
    void trailingCommaNeedsAColumn() {
        assertError("SELECT name, FROM users;", "Expected an expression but found 'FROM' (FROM) at line 1, column 14");
    }

    @Test
    void emptySelectList() {
        assertError("SELECT FROM users;", "Expected an expression but found 'FROM' (FROM) at line 1, column 8");
    }

    @Test
    void missingCommaBetweenColumns() {
        // after "name" the list ends, so "age" is where FROM should be
        assertSyntaxError("SELECT name age FROM users;", FROM, "'age' (IDENTIFIER)", 1, 13);
    }

    @Test
    void mustStartWithSelect() {
        assertSyntaxError("FROM users;", SELECT, "'FROM' (FROM)", 1, 1);
    }

    @Test
    void emptyInput() {
        assertSyntaxError("", SELECT, "end of input (EOF)", 1, 1);
    }

    @Test
    void missingTableName() {
        assertSyntaxError("SELECT name FROM ;", IDENTIFIER, "';' (SEMICOLON)", 1, 18);
    }

    @Test
    void errorOnLaterLineReportsThatLine() {
        assertSyntaxError("SELECT name\nFROM users\nblabla;", SEMICOLON, "'blabla' (IDENTIFIER)", 3, 1);
    }

    @Test
    void noWhereMeansEmpty() throws Exception {
        assertTrue(parse("SELECT * FROM users;").where().isEmpty());
    }

    @Test
    void whereWithComparison() throws Exception {
        assertEquals(SelectStatement.all("users").withWhere(new Binary(col("age"), GREATER, num(30))),
                parse("SELECT * FROM users WHERE age > 30;"));
    }

    @Test
    void whereWithBetweenAndLogicalAnd() throws Exception {
        Expr condition = new Binary(new Between(col("a"), num(1), num(5)), AND, new Binary(col("b"), EQUAL, num(2)));
        assertEquals(SelectStatement.of(items(col("name"), col("age")), "users").withWhere(condition),
                parse("SELECT name, age FROM users WHERE a BETWEEN 1 AND 5 AND b = 2;"));
    }

    @Test
    void whereWithEveryKindOfOperator() throws Exception {
        Expr inCity = new In(col("city"), List.of(new Literal("Haifa")));
        Expr hasEmail = new Unary(NOT, new IsNull(col("email")));
        Expr startsWithA = new Binary(col("name"), LIKE, new Literal("A%"));
        Expr condition = new Binary(new Binary(inCity, AND, hasEmail), OR, startsWithA);

        assertEquals(SelectStatement.of(items(col("name")), "users").withWhere(condition),
                parse("SELECT name FROM users WHERE city IN ('Haifa') AND email IS NOT NULL OR name LIKE 'A%';"));
    }

    @Test
    void whereConditionEndsAtTheFirstNonOperator() {
        assertSyntaxError("SELECT * FROM users WHERE age > 30 name = 'x';", SEMICOLON, "'name' (IDENTIFIER)", 1, 36);
    }

    @Test
    void whereWithoutCondition() {
        assertError("SELECT * FROM users WHERE;",
                "Expected an expression but found ';' (SEMICOLON) at line 1, column 26");
        assertError("SELECT * FROM users WHERE WHERE a;",
                "Expected an expression but found 'WHERE' (WHERE) at line 1, column 27");
    }

    @Test
    void whereMissingSemicolon() {
        assertSyntaxError("SELECT * FROM users WHERE age > 30", SEMICOLON, "end of input (EOF)", 1, 35);
    }

    @Test
    void whereFollowedByDoubleSemicolon() {
        assertSyntaxError("SELECT * FROM users WHERE age > 30;;", EOF, "';' (SEMICOLON)", 1, 36);
    }

    @Test
    void whereUnclosedParenthesis() {
        assertSyntaxError("SELECT * FROM users WHERE (age > 30;", RIGHT_PAREN, "';' (SEMICOLON)", 1, 36);
    }

    @Test
    void aggregateInSelectList() throws Exception {
        assertEquals(SelectStatement.of(items(new Aggregate(Aggregate.Function.COUNT, Optional.empty())), "users"),
                parse("SELECT COUNT(*) FROM users;"));
    }

    @Test
    void expressionsInSelectList() throws Exception {
        assertEquals(SelectStatement.of(items(col("name"), new Binary(col("price"), STAR, col("qty"))), "orders"),
                parse("SELECT name, price * qty FROM orders;"));
    }

    @Test
    void aliases() throws Exception {
        Expr total = new Binary(col("price"), STAR, col("qty"));
        assertEquals(SelectStatement.of(List.of(SelectItem.as(total, "Total")), "orders"),
                parse("SELECT price * qty AS Total FROM orders;"));

        Expr count = new Aggregate(Aggregate.Function.COUNT, Optional.empty());
        assertEquals(SelectStatement.of(List.of(SelectItem.as(col("name"), "n"), SelectItem.as(count, "c")), "users"),
                parse("SELECT name AS n, COUNT(*) AS c FROM users;"));
    }

    @Test
    void someItemsWithAliasSomeWithout() throws Exception {
        assertEquals(SelectStatement.of(List.of(SelectItem.of(col("a")), SelectItem.as(col("b"), "x")), "t"),
                parse("SELECT a, b AS x FROM t;"));
    }

    @Test
    void distinct() throws Exception {
        assertEquals(SelectStatement.of(items(col("city")), "users").withDistinct(),
                parse("SELECT DISTINCT city FROM users;"));
        assertEquals(SelectStatement.all("users").withDistinct(), parse("SELECT DISTINCT * FROM users;"));
    }

    @Test
    void distinctWithWhere() throws Exception {
        assertEquals(SelectStatement.of(items(col("city")), "users").withDistinct()
                        .withWhere(new Binary(col("age"), GREATER, num(30))),
                parse("select distinct city from users where age > 30;"));
    }

    @Test
    void aliasWithoutAsIsAnError() {
        // AS is required, so a forgotten comma isn't silently read as an alias
        assertSyntaxError("SELECT price total FROM orders;", FROM, "'total' (IDENTIFIER)", 1, 14);
    }

    @Test
    void asNeedsAName() {
        assertSyntaxError("SELECT name AS FROM users;", IDENTIFIER, "'FROM' (FROM)", 1, 16);
        assertSyntaxError("SELECT name AS 5 FROM users;", IDENTIFIER, "'5' (NUMBER)", 1, 16);
        assertSyntaxError("SELECT name AS 'n' FROM users;", IDENTIFIER, "''n'' (STRING)", 1, 16);
    }

    @Test
    void oneAliasPerItem() {
        assertSyntaxError("SELECT name AS n AS m FROM users;", FROM, "'AS' (AS)", 1, 18);
    }

    @Test
    void starCannotHaveAnAlias() {
        assertSyntaxError("SELECT * AS x FROM users;", FROM, "'AS' (AS)", 1, 10);
    }

    @Test
    void distinctNeedsSomethingToSelect() {
        assertError("SELECT DISTINCT FROM users;", "Expected an expression but found 'FROM' (FROM) at line 1, column 17");
        assertError("SELECT DISTINCT DISTINCT a FROM t;",
                "Expected an expression but found 'DISTINCT' (DISTINCT) at line 1, column 17");
    }

    @Test
    void distinctOnlyRightAfterSelect() {
        assertSyntaxError("SELECT name DISTINCT FROM users;", FROM, "'DISTINCT' (DISTINCT)", 1, 13);
    }
    private static final Expr COUNT_STAR = new Aggregate(Aggregate.Function.COUNT, Optional.empty());

    @Test
    void groupBySingleKey() throws Exception {
        assertEquals(SelectStatement.of(items(col("city"), COUNT_STAR), "users").withGroupBy(List.of(col("city"))),
                parse("SELECT city, COUNT(*) FROM users GROUP BY city;"));
    }

    @Test
    void groupBySeveralKeysAndExpressions() throws Exception {
        Expr decade = new Binary(col("age"), SLASH, num(10));
        assertEquals(SelectStatement.of(items(col("city")), "users").withGroupBy(List.of(col("city"), decade)),
                parse("select city from users group by city, age / 10;"));
    }

    @Test
    void whereGroupByHaving() throws Exception {
        Expr where = new Binary(col("age"), GREATER, num(18));
        Expr having = new Binary(
                new Binary(COUNT_STAR, GREATER, num(5)),
                AND,
                new Binary(new Aggregate(Aggregate.Function.AVG, Optional.of(col("age"))), LESS, num(40)));
        assertEquals(SelectStatement.of(items(col("city")), "users")
                        .withWhere(where).withGroupBy(List.of(col("city"))).withHaving(having),
                parse("SELECT city FROM users WHERE age > 18 GROUP BY city HAVING COUNT(*) > 5 AND AVG(age) < 40;"));
    }

    @Test
    void groupByWithoutHavingLeavesItEmpty() throws Exception {
        SelectStatement s = parse("SELECT city FROM users GROUP BY city;");
        assertTrue(s.having().isEmpty());
        assertTrue(parse("SELECT city FROM users;").groupBy().isEmpty());
    }

    @Test
    void groupNeedsBy() {
        assertSyntaxError("SELECT city FROM users GROUP city;", BY, "'city' (IDENTIFIER)", 1, 30);
    }

    @Test
    void groupByNeedsAKey() {
        assertError("SELECT city FROM users GROUP BY;",
                "Expected an expression but found ';' (SEMICOLON) at line 1, column 32");
        assertError("SELECT city FROM users GROUP BY city, ;",
                "Expected an expression but found ';' (SEMICOLON) at line 1, column 39");
    }

    @Test
    void groupByKeysNeedCommas() {
        assertSyntaxError("SELECT city FROM users GROUP BY city age;", SEMICOLON, "'age' (IDENTIFIER)", 1, 38);
    }

    @Test
    void havingNeedsGroupBy() {
        assertSyntaxError("SELECT COUNT(*) FROM users HAVING COUNT(*) > 5;", SEMICOLON, "'HAVING' (HAVING)", 1, 28);
    }

    @Test
    void havingNeedsACondition() {
        assertError("SELECT city FROM users GROUP BY city HAVING;",
                "Expected an expression but found ';' (SEMICOLON) at line 1, column 44");
    }

    @Test
    void whereMustComeBeforeGroupBy() {
        assertSyntaxError("SELECT city FROM users GROUP BY city WHERE age > 1;", SEMICOLON, "'WHERE' (WHERE)", 1, 38);
    }
}
