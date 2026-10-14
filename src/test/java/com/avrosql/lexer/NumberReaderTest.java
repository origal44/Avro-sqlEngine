package com.avrosql.lexer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.avrosql.lexer.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

class NumberReaderTest {

    private final NumberReader reader = new NumberReader();

    private static List<Token> lex(String source) {
        return new Lexer(source).tokenize();
    }

    private static Token token(TokenType type, String text, int column) {
        return new Token(type, text, 1, column);
    }

    // --- The reader on its own ---

    @ParameterizedTest
    @ValueSource(strings = {"0", "7", "42", "007", "1234567890"})
    void readsIntegers(String text) {
        Cursor cursor = new Cursor(text);

        assertEquals(token(NUMBER, text, 1), reader.read(cursor));
        assertTrue(cursor.isAtEnd());
    }

    @ParameterizedTest
    @ValueSource(strings = {"3.14", "0.5", "10.0", "2.000"})
    void readsDecimals(String text) {
        Cursor cursor = new Cursor(text);

        assertEquals(token(NUMBER, text, 1), reader.read(cursor));
        assertTrue(cursor.isAtEnd());
    }

    @Test
    void stopsAtFirstNonDigitWithoutConsumingIt() {
        // 42abc: the number ends at 'a', which is left for the next token.
        Cursor cursor = new Cursor("42abc");

        assertEquals(token(NUMBER, "42", 1), reader.read(cursor));
        assertEquals('a', cursor.peek());
    }

    // --- Through the Lexer ---

    @Test
    void numberOperatorNumber() {
        assertEquals(List.of(
                token(NUMBER, "42", 1),
                token(PLUS, "+", 3),
                token(NUMBER, "1", 4),
                token(EOF, "", 5)), lex("42+1"));
    }

    @Test
    void negativeNumberIsMinusThenNumber() {
        assertEquals(List.of(
                token(MINUS, "-", 1),
                token(NUMBER, "5.5", 2),
                token(EOF, "", 5)), lex("-5.5"));
    }

    @Test
    void numbersInsideParenthesesAndAfterSpaces() {
        assertEquals(List.of(
                token(LEFT_PAREN, "(", 1),
                token(NUMBER, "10", 2),
                token(PERCENT, "%", 5),
                token(NUMBER, "3", 7),
                token(RIGHT_PAREN, ")", 8),
                token(EOF, "", 9)), lex("(10 % 3)"));
    }

    @Test
    void trailingDotIsALexicalError() {
        LexicalException e = assertThrows(LexicalException.class, () -> lex("3."));

        assertEquals('.', e.character());
        assertEquals(2, e.column());
    }

    @Test
    void dotFollowedByNonDigitIsALexicalError() {
        LexicalException e = assertThrows(LexicalException.class, () -> lex("3.x"));

        assertEquals('.', e.character());
        assertEquals(2, e.column());
    }

    @Test
    void leadingDotIsALexicalError() {
        // No reader starts with '.', so the Lexer rejects it.
        LexicalException e = assertThrows(LexicalException.class, () -> lex(".5"));

        assertEquals('.', e.character());
        assertEquals(1, e.column());
    }

    @Test
    void secondDotIsALexicalError() {
        // 1.2.3: the number is 1.2, then the second '.' starts nothing.
        LexicalException e = assertThrows(LexicalException.class, () -> lex("1.2.3"));

        assertEquals('.', e.character());
        assertEquals(4, e.column());
    }
}
