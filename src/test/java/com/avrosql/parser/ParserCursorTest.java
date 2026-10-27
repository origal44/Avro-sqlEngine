package com.avrosql.parser;

import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.lexer.token.Token;
import com.avrosql.parser.exception.SyntaxException;
import com.avrosql.parser.exception.SyntaxRuntimeException;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.avrosql.lexer.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

/** The five token operations every parse method is built on. */
class ParserCursorTest {

    private static Parser parser(String source) throws LexicalException {
        return new Parser(new Lexer(source).tokenize());
    }

    // ---- construction ----

    @Test
    void rejectsEmptyTokenList() {
        assertThrows(IllegalArgumentException.class, () -> new Parser(List.of()));
    }

    @Test
    void rejectsTokenListWithoutEof() {
        List<Token> tokens = List.of(new Token(SELECT, "SELECT", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Parser(tokens));
    }

    // ---- peek / advance ----

    @Test
    void peekDoesNotMove() throws LexicalException {
        Parser p = parser("SELECT name");
        assertEquals(SELECT, p.peek().type());
        assertEquals(SELECT, p.peek().type());
    }

    @Test
    void advanceReturnsCurrentTokenAndMoves() throws LexicalException {
        Parser p = parser("SELECT name");
        assertEquals(SELECT, p.advance().type());
        assertEquals("name", p.advance().text());
        assertEquals(EOF, p.peek().type());
    }

    @Test
    void advanceStaysOnEof() throws LexicalException {
        Parser p = parser("");
        assertEquals(EOF, p.advance().type());
        assertEquals(EOF, p.advance().type());
        assertEquals(EOF, p.peek().type());
    }

    // ---- check ----

    @Test
    void checkReportsTypeWithoutMoving() throws LexicalException {
        Parser p = parser("WHERE");
        assertFalse(p.check(GROUP));
        assertTrue(p.check(WHERE));
        assertEquals(WHERE, p.peek().type());
    }

    // ---- match ----

    @Test
    void matchConsumesOnSuccess() throws LexicalException {
        Parser p = parser("DESC name");
        assertTrue(p.match(DESC));
        assertEquals(IDENTIFIER, p.peek().type());
    }

    @Test
    void matchLeavesTokenOnFailure() throws LexicalException {
        Parser p = parser("name");
        assertFalse(p.match(DESC));
        assertEquals(IDENTIFIER, p.peek().type());
    }

    // ---- expect ----

    @Test
    void expectReturnsConsumedToken() throws LexicalException {
        Parser p = parser("users;");
        Token table = p.expect(IDENTIFIER);
        assertEquals("users", table.text());
        assertEquals(SEMICOLON, p.peek().type());
    }

    @Test
    void expectEofAtEndSucceeds() throws LexicalException {
        Parser p = parser(";");
        p.expect(SEMICOLON);
        assertEquals(EOF, p.expect(EOF).type());
    }

    @Test
    void expectFailureAtEofReportsExpectedFoundAndPosition() throws LexicalException {
        Parser p = parser("SELECT name FROM users");
        p.advance(); p.advance(); p.advance(); p.advance();

        SyntaxException e = assertThrows(SyntaxRuntimeException.class, () -> p.expect(SEMICOLON)).getCause();

        assertEquals(SEMICOLON, e.expected());
        assertEquals(EOF, e.found().type());
        assertEquals(1, e.line());
        assertEquals(23, e.column());
        assertEquals("Expected SEMICOLON but found end of input (EOF) at line 1, column 23", e.getMessage());
    }

    @Test
    void expectFailureOnIdentifierShowsItsText() throws LexicalException {
        Parser p = parser("; blabla");
        p.expect(SEMICOLON);

        SyntaxException e = assertThrows(SyntaxRuntimeException.class, () -> p.expect(EOF)).getCause();

        assertEquals("Expected EOF but found 'blabla' (IDENTIFIER) at line 1, column 3", e.getMessage());
    }

    @Test
    void expectFailureDoesNotMove() throws LexicalException {
        Parser p = parser("WHERE");
        assertThrows(SyntaxRuntimeException.class, () -> p.expect(FROM));
        assertEquals(WHERE, p.peek().type());
    }

    @Test
    void positionComesFromFoundTokenOnLaterLine() throws LexicalException {
        Parser p = parser("SELECT name\n  WHERE");
        p.advance(); p.advance();

        SyntaxException e = assertThrows(SyntaxRuntimeException.class, () -> p.expect(FROM)).getCause();

        assertEquals(2, e.line());
        assertEquals(3, e.column());
        assertTrue(e.getMessage().contains("Expected FROM"));
        assertTrue(e.getMessage().contains("'WHERE' (WHERE)"));
    }
}
