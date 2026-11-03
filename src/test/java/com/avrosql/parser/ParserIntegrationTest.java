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
import com.avrosql.parser.ast.OrderItem;
import com.avrosql.parser.ast.SelectItem;
import com.avrosql.parser.ast.SelectStatement;
import com.avrosql.parser.ast.Unary;
import com.avrosql.parser.exception.SyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.avrosql.lexer.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

// Lexer and Parser together, on whole queries.
class ParserIntegrationTest {

    private static final Expr COUNT_STAR = new Aggregate(Aggregate.Function.COUNT, Optional.empty());

    private static SelectStatement parse(String sql) throws LexicalException, SyntaxException {
        return new Parser(new Lexer(sql).tokenize()).parse();
    }

    private static Expr col(String name) {
        return new ColumnRef(name);
    }

    private static Expr lit(Object value) {
        return new Literal(value);
    }

    private static Expr bin(Expr left, TokenType op, Expr right) {
        return new Binary(left, op, right);
    }

    @Test
    void fullMultiLineQuery() throws Exception {
        String sql = """
                SELECT DISTINCT city, COUNT(*) AS total, AVG(age) AS average
                FROM users
                WHERE age BETWEEN 18 AND 65
                  AND (city IN ('Haifa', 'Tel Aviv') OR name LIKE 'A%')
                  AND email IS NOT NULL
                GROUP BY city
                HAVING COUNT(*) >= 10
                ORDER BY total DESC, city
                LIMIT 20 OFFSET 40;
                """;

        Expr where = bin(
                bin(
                        new Between(col("age"), lit(18L), lit(65L)),
                        AND,
                        bin(new In(col("city"), List.of(lit("Haifa"), lit("Tel Aviv"))),
                                OR,
                                bin(col("name"), LIKE, lit("A%")))),
                AND,
                new Unary(NOT, new IsNull(col("email"))));

        SelectStatement expected = SelectStatement.of(List.of(
                                SelectItem.of(col("city")),
                                SelectItem.as(COUNT_STAR, "total"),
                                SelectItem.as(new Aggregate(Aggregate.Function.AVG, Optional.of(col("age"))), "average")),
                        "users")
                .withDistinct()
                .withWhere(where)
                .withGroupBy(List.of(col("city")))
                .withHaving(bin(COUNT_STAR, GREATER_EQUAL, lit(10L)))
                .withOrderBy(List.of(OrderItem.desc(col("total")), OrderItem.asc(col("city"))))
                .withLimit(20)
                .withOffset(40);

        assertEquals(expected, parse(sql));
    }

    @Test
    void layoutDoesNotChangeTheTree() throws Exception {
        SelectStatement oneLine = parse("SELECT a, b FROM t WHERE a > 1 ORDER BY b DESC LIMIT 5;");

        assertEquals(oneLine, parse("SELECT a,b FROM t WHERE a>1 ORDER BY b DESC LIMIT 5;"));
        assertEquals(oneLine, parse("  SELECT   a ,  b\tFROM t   WHERE a >  1 ORDER BY b DESC LIMIT 5 ;  "));
        assertEquals(oneLine, parse("SELECT\r\n\ta,\r\n\tb\r\nFROM t\r\nWHERE a > 1\r\nORDER BY b DESC\r\nLIMIT 5;\r\n"));
    }

    @Test
    void keywordsAndFunctionsIgnoreCaseButNamesDoNot() throws Exception {
        assertEquals(parse("SELECT Name, COUNT(*) FROM Users GROUP BY Name HAVING SUM(Age) > 1 ORDER BY Name ASC;"),
                parse("select Name, count(*) from Users group by Name having sum(Age) > 1 order by Name asc;"));
        assertNotEquals(parse("SELECT name FROM users;"), parse("SELECT Name FROM users;"));
    }

    @Test
    void precedenceEndToEnd() throws Exception {
        Expr item = bin(
                bin(col("a"), PLUS, bin(col("b"), STAR, col("c"))),
                MINUS,
                bin(col("d"), SLASH, lit(2L)));
        Expr where = bin(
                new Unary(NOT, bin(col("a"), EQUAL, lit(1L))),
                OR,
                bin(bin(col("b"), LESS, lit(2L)), AND, bin(col("c"), GREATER_EQUAL, lit(3L))));

        assertEquals(SelectStatement.of(List.of(SelectItem.of(item)), "t").withWhere(where),
                parse("SELECT a + b * c - d / 2 FROM t WHERE NOT a = 1 OR b < 2 AND c >= 3;"));
    }

    @Test
    void literalsArriveConverted() throws Exception {
        Expr where = bin(
                bin(col("name"), EQUAL, lit("O'Brien")),
                AND,
                bin(col("price"), GREATER, new Unary(MINUS, lit(1.5))));

        assertEquals(SelectStatement.all("t").withWhere(where),
                parse("SELECT * FROM t WHERE name = 'O''Brien' AND price > -1.5;"));
    }

    @Test
    void syntaxErrorOnALaterLine() {
        SyntaxException e = assertThrows(SyntaxException.class, () -> parse("SELECT name\nFROM users\nWHERE age > ;"));
        assertEquals("Expected an expression but found ';' (SEMICOLON) at line 3, column 13", e.getMessage());
        assertEquals(3, e.line());
        assertEquals(13, e.column());
    }

    @Test
    void missingSemicolonPointsAtEndOfLastLine() {
        SyntaxException e = assertThrows(SyntaxException.class, () -> parse("SELECT name\nFROM users"));
        assertEquals("Expected SEMICOLON but found end of input (EOF) at line 2, column 11", e.getMessage());
        assertEquals(SEMICOLON, e.expected());
    }

    @Test
    void lexicalErrorsStopBeforeTheParser() {
        LexicalException e = assertThrows(LexicalException.class,
                () -> parse("SELECT name FROM users WHERE age > 18 # adults;"));
        assertEquals('#', e.character());
        assertEquals(39, e.column());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM t;",
            "SELECT DISTINCT * FROM t;",
            "SELECT a FROM t;",
            "SELECT a AS x, b AS y FROM t;",
            "SELECT 1, 'text', 2.5, -3 FROM t;",
            "SELECT COUNT(*), SUM(a), AVG(a), MIN(a), MAX(a) FROM t;",
            "SELECT * FROM t WHERE ((a));",
            "SELECT * FROM t WHERE a NOT BETWEEN 1 AND 2;",
            "SELECT * FROM t WHERE a NOT IN (1) AND b NOT LIKE 'x';",
            "SELECT * FROM t WHERE NOT NOT a;",
            "SELECT * FROM t WHERE a IS NULL OR b IS NOT NULL;",
            "SELECT a FROM t GROUP BY a, b, a + b;",
            "SELECT a FROM t ORDER BY a ASC, b DESC, c;",
            "SELECT a FROM t LIMIT 0 OFFSET 0;",
            "SELECT a FROM t WHERE a > 1 GROUP BY a HAVING COUNT(*) > 1 ORDER BY a LIMIT 1 OFFSET 1;",
    })
    void validQueriesParse(String sql) {
        assertDoesNotThrow(() -> parse(sql));
    }

    static Stream<Arguments> invalidQueries() {
        return Stream.of(
                Arguments.of("",
                        "Expected SELECT but found end of input (EOF) at line 1, column 1"),
                Arguments.of("FROM t SELECT a;",
                        "Expected SELECT but found 'FROM' (FROM) at line 1, column 1"),
                Arguments.of("SELECT * FROM users WHERE;",
                        "Expected an expression but found ';' (SEMICOLON) at line 1, column 26"),
                Arguments.of("SELECT * FROM users WHERE age > 1 < 2;",
                        "Comparisons can't be chained: found '<' (LESS) at line 1, column 35"),
                Arguments.of("SELECT * FROM users WHERE age BETWEEN 1 OR 5;",
                        "Expected AND but found 'OR' (OR) at line 1, column 41"),
                Arguments.of("SELECT * FROM users WHERE id IN 1, 2;",
                        "Expected LEFT_PAREN but found '1' (NUMBER) at line 1, column 33"),
                Arguments.of("SELECT * FROM users WHERE id IN ();",
                        "Expected an expression but found ')' (RIGHT_PAREN) at line 1, column 34"),
                Arguments.of("SELECT * FROM users WHERE name NOT 'A%';",
                        "Expected IN, LIKE or BETWEEN but found ''A%'' (STRING) at line 1, column 36"),
                Arguments.of("SELECT * FROM users WHERE email IS NULLX;",
                        "Expected NULL but found 'NULLX' (IDENTIFIER) at line 1, column 36"),
                Arguments.of("SELECT FOO(x) FROM t;",
                        "Unknown function 'FOO' at line 1, column 8"),
                Arguments.of("SELECT SUM(*) FROM t;",
                        "Expected an expression but found '*' (STAR) at line 1, column 12"),
                Arguments.of("SELECT COUNT(* FROM t;",
                        "Expected RIGHT_PAREN but found 'FROM' (FROM) at line 1, column 16"),
                Arguments.of("SELECT (a + b FROM t;",
                        "Expected RIGHT_PAREN but found 'FROM' (FROM) at line 1, column 15"),
                Arguments.of("SELECT a FROM t GROUP BY a HAVING ORDER BY a;",
                        "Expected an expression but found 'ORDER' (ORDER) at line 1, column 35"),
                Arguments.of("SELECT a FROM t LIMIT 5 OFFSET -1;",
                        "Expected NUMBER but found '-' (MINUS) at line 1, column 32"),
                Arguments.of("SELECT a FROM t WHERE a = 99999999999999999999;",
                        "Number too large: '99999999999999999999' at line 1, column 27"),
                Arguments.of("SELECT a FROM t; SELECT b FROM t;",
                        "Expected EOF but found 'SELECT' (SELECT) at line 1, column 18"));
    }

    @ParameterizedTest
    @MethodSource("invalidQueries")
    void invalidQueriesReportWhatAndWhere(String sql, String message) {
        SyntaxException e = assertThrows(SyntaxException.class, () -> parse(sql));
        assertEquals(message, e.getMessage());
    }
}
