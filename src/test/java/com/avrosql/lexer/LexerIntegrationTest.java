package com.avrosql.lexer;

import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.avrosql.lexer.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

/** All readers working together on whole queries. */
class LexerIntegrationTest {

    private static List<Token> lex(String source) throws LexicalException {
        return new Lexer(source).tokenize();
    }

    private static List<TokenType> types(String source) throws LexicalException {
        return lex(source).stream().map(Token::type).toList();
    }

    private static Token t(TokenType type, String text, int line, int column) {
        return new Token(type, text, line, column);
    }

    @Test
    void fullMultiLineQuery() throws LexicalException {
        String sql = """
                SELECT DISTINCT name, COUNT(*) AS total
                FROM users
                WHERE age >= 18 AND city <> 'Tel Aviv; Haifa'
                  AND email IS NOT NULL
                GROUP BY name
                HAVING COUNT(*) > 1
                ORDER BY total DESC
                LIMIT 10 OFFSET 5;""";

        assertEquals(List.of(
                // line 1
                t(SELECT, "SELECT", 1, 1),
                t(DISTINCT, "DISTINCT", 1, 8),
                t(IDENTIFIER, "name", 1, 17),
                t(COMMA, ",", 1, 21),
                t(IDENTIFIER, "COUNT", 1, 23),
                t(LEFT_PAREN, "(", 1, 28),
                t(STAR, "*", 1, 29),
                t(RIGHT_PAREN, ")", 1, 30),
                t(AS, "AS", 1, 32),
                t(IDENTIFIER, "total", 1, 35),
                // line 2
                t(FROM, "FROM", 2, 1),
                t(IDENTIFIER, "users", 2, 6),
                // line 3
                t(WHERE, "WHERE", 3, 1),
                t(IDENTIFIER, "age", 3, 7),
                t(GREATER_EQUAL, ">=", 3, 11),
                t(NUMBER, "18", 3, 14),
                t(AND, "AND", 3, 17),
                t(IDENTIFIER, "city", 3, 21),
                t(NOT_EQUAL, "<>", 3, 26),
                t(STRING, "'Tel Aviv; Haifa'", 3, 29),
                // line 4 (indented)
                t(AND, "AND", 4, 3),
                t(IDENTIFIER, "email", 4, 7),
                t(IS, "IS", 4, 13),
                t(NOT, "NOT", 4, 16),
                t(NULL, "NULL", 4, 20),
                // line 5
                t(GROUP, "GROUP", 5, 1),
                t(BY, "BY", 5, 7),
                t(IDENTIFIER, "name", 5, 10),
                // line 6
                t(HAVING, "HAVING", 6, 1),
                t(IDENTIFIER, "COUNT", 6, 8),
                t(LEFT_PAREN, "(", 6, 13),
                t(STAR, "*", 6, 14),
                t(RIGHT_PAREN, ")", 6, 15),
                t(GREATER, ">", 6, 17),
                t(NUMBER, "1", 6, 19),
                // line 7
                t(ORDER, "ORDER", 7, 1),
                t(BY, "BY", 7, 7),
                t(IDENTIFIER, "total", 7, 10),
                t(DESC, "DESC", 7, 16),
                // line 8
                t(LIMIT, "LIMIT", 8, 1),
                t(NUMBER, "10", 8, 7),
                t(OFFSET, "OFFSET", 8, 10),
                t(NUMBER, "5", 8, 17),
                t(SEMICOLON, ";", 8, 18),
                t(EOF, "", 8, 19)), lex(sql));
    }

    // --- ';' is an ordinary token, not a stop condition ---

    @Test
    void queryWithoutSemicolon() throws LexicalException {
        assertEquals(List.of(
                t(SELECT, "SELECT", 1, 1),
                t(STAR, "*", 1, 8),
                t(FROM, "FROM", 1, 10),
                t(IDENTIFIER, "t", 1, 15),
                t(EOF, "", 1, 16)), lex("SELECT * FROM t"));
    }

    @Test
    void queryWithSemicolon() throws LexicalException {
        assertEquals(List.of(
                t(SELECT, "SELECT", 1, 1),
                t(STAR, "*", 1, 8),
                t(FROM, "FROM", 1, 10),
                t(IDENTIFIER, "t", 1, 15),
                t(SEMICOLON, ";", 1, 16),
                t(EOF, "", 1, 17)), lex("SELECT * FROM t;"));
    }

    @Test
    void textAfterSemicolonIsLexedNormally() throws LexicalException {
        // A truncated second query: the Lexer returns it, the Parser will reject it.
        assertEquals(List.of(SELECT, STAR, FROM, IDENTIFIER, SEMICOLON, SELECT, EOF),
                types("SELECT * FROM t; SELECT"));
    }

    @Test
    void unknownWordAfterSemicolonIsJustAnIdentifier() throws LexicalException {
        assertEquals(List.of(SELECT, STAR, FROM, IDENTIFIER, SEMICOLON, IDENTIFIER, IDENTIFIER, EOF),
                types("SELECT * FROM t; DROP users"));
    }

    // --- No syntax knowledge in the Lexer ---

    @Test
    void betweenAndIsAnOrdinaryAnd() throws LexicalException {
        assertEquals(List.of(WHERE, IDENTIFIER, NOT, BETWEEN, NUMBER, AND, NUMBER, EOF),
                types("WHERE age NOT BETWEEN 1 AND 5"));
    }

    @Test
    void notInIsTwoTokens() throws LexicalException {
        assertEquals(List.of(WHERE, IDENTIFIER, NOT, IN,
                        LEFT_PAREN, STRING, COMMA, STRING, RIGHT_PAREN, EOF),
                types("WHERE city NOT IN ('Haifa', 'Eilat')"));
    }

    @Test
    void likePatternIsAString() throws LexicalException {
        assertEquals(List.of(WHERE, IDENTIFIER, LIKE, STRING, EOF),
                types("WHERE name LIKE 'A%'"));
    }

    @Test
    void arithmeticWithNegativeDecimal() throws LexicalException {
        assertEquals(List.of(SELECT, IDENTIFIER, STAR, MINUS, NUMBER, PERCENT, NUMBER, EOF),
                types("SELECT price*-1.5%3"));
    }

    @Test
    void wrongWordOrderIsNotTheLexersProblem() throws LexicalException {
        assertEquals(List.of(FROM, SELECT, WHERE, EOF), types("FROM SELECT WHERE"));
    }

    // --- Whitespace and positions ---

    @Test
    void windowsLineEndingsAndTabs() throws LexicalException {
        assertEquals(List.of(
                t(SELECT, "SELECT", 1, 1),
                t(IDENTIFIER, "name", 2, 2),
                t(FROM, "FROM", 3, 1),
                t(IDENTIFIER, "t", 3, 6),
                t(EOF, "", 3, 7)), lex("SELECT\r\n\tname\r\nFROM t"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "SELECT", "SELECT * FROM t;", "a;b;c", "'x' ; 'y'", "1 + 2\n"})
    void eofAppearsExactlyOnceAndLast(String sql) throws LexicalException {
        List<TokenType> types = types(sql);

        assertEquals(EOF, types.get(types.size() - 1));
        assertEquals(1, types.stream().filter(type -> type == EOF).count());
    }

    // --- Errors inside a full query ---

    @Test
    void unknownCharacterInQueryReportsItsPosition() {
        LexicalException e = assertThrows(LexicalException.class,
                () -> lex("SELECT name\nFROM users\nWHERE age > 18 # adults"));

        assertEquals('#', e.character());
        assertEquals(3, e.line());
        assertEquals(16, e.column());
    }

    @Test
    void unterminatedStringInQueryReportsOpeningQuote() {
        LexicalException e = assertThrows(LexicalException.class,
                () -> lex("SELECT name FROM users WHERE city = 'Haifa;"));

        assertEquals(1, e.line());
        assertEquals(37, e.column());
    }

    @Test
    void resultListCannotBeModified() throws LexicalException {
        List<Token> tokens = lex("SELECT 1");

        assertThrows(UnsupportedOperationException.class, () -> tokens.add(t(EOF, "", 1, 1)));
    }
}
