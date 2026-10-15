package com.avrosql.lexer.reader;

import com.avrosql.lexer.Cursor;
import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.LexicalException;
import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.avrosql.lexer.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

class StringReaderTest {

    private final StringReader reader = new StringReader();

    private static List<Token> lex(String source) throws LexicalException {
        return new Lexer(source).tokenize();
    }

    private static Token token(TokenType type, String text, int column) {
        return new Token(type, text, 1, column);
    }

    // --- The reader on its own ---

    @ParameterizedTest
    @ValueSource(strings = {"'abc'", "''", "'it''s'", "''''", "'a b  c'"})
    void readsWholeStringAsRawText(String text) {
        Cursor cursor = new Cursor(text);

        assertEquals(token(STRING, text, 1), reader.read(cursor));
        assertTrue(cursor.isAtEnd());
    }

    @Test
    void stopsRightAfterClosingQuote() {
        Cursor cursor = new Cursor("'abc')");

        assertEquals(token(STRING, "'abc'", 1), reader.read(cursor));
        assertEquals(')', cursor.peek());
    }

    // --- Through the Lexer ---

    @Test
    void semicolonInsideStringDoesNotStopReading() throws LexicalException {
        assertEquals(List.of(
                token(STRING, "'Tel Aviv; Haifa'", 1),
                token(EOF, "", 18)), lex("'Tel Aviv; Haifa'"));
    }

    @Test
    void sqlTextInsideStringIsNotTokenized() throws LexicalException {
        // Keywords, operators and unknown characters are just content inside quotes.
        assertEquals(List.of(
                token(STRING, "'SELECT * FROM t; -- #@!'", 1),
                token(EOF, "", 26)), lex("'SELECT * FROM t; -- #@!'"));
    }

    @Test
    void spaceSeparatedQuotesAreTwoStrings() throws LexicalException {
        assertEquals(List.of(
                token(STRING, "'abc'", 1),
                token(STRING, "'def'", 7),
                token(EOF, "", 12)), lex("'abc' 'def'"));
    }

    @Test
    void adjacentQuotesAreOneStringWithEscape() throws LexicalException {
        assertEquals(List.of(
                token(STRING, "'abc''def'", 1),
                token(EOF, "", 11)), lex("'abc''def'"));
    }

    @Test
    void stringBetweenOperators() throws LexicalException {
        assertEquals(List.of(
                token(EQUAL, "=", 1),
                token(STRING, "'x'", 3),
                token(SEMICOLON, ";", 6),
                token(EOF, "", 7)), lex("= 'x';"));
    }

    @Test
    void newlineInsideStringKeepsPositionsCorrect() throws LexicalException {
        assertEquals(List.of(
                new Token(STRING, "'a\nb'", 1, 1),
                new Token(SEMICOLON, ";", 2, 4),
                new Token(EOF, "", 2, 5)), lex("'a\nb' ;"));
    }

    @Test
    void unterminatedStringPointsAtOpeningQuote() {
        LexicalException e = assertThrows(LexicalException.class, () -> lex("= 'abc"));

        assertEquals('\'', e.character());
        assertEquals(1, e.line());
        assertEquals(3, e.column());
        assertEquals("Unterminated string starting at line 1, column 3", e.getMessage());
    }

    @Test
    void escapedQuoteAtEndIsStillUnterminated() {
        // 'it'' : the last '' is an escape, so the string never closes.
        LexicalException e = assertThrows(LexicalException.class, () -> lex("'it''"));

        assertEquals(1, e.column());
    }

    @Test
    void loneQuoteIsUnterminated() {
        assertThrows(LexicalException.class, () -> lex("'"));
    }
}
