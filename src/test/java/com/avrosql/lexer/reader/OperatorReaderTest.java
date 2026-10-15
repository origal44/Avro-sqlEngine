package com.avrosql.lexer.reader;

import com.avrosql.lexer.Cursor;
import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.LexicalException;
import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static com.avrosql.lexer.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

class OperatorReaderTest {

    private final OperatorReader reader = new OperatorReader();

    private static List<Token> lex(String source) throws LexicalException {
        return new Lexer(source).tokenize();
    }

    private static Token token(TokenType type, String text, int column) {
        return new Token(type, text, 1, column);
    }

    // --- The reader on its own ---

    @ParameterizedTest
    @CsvSource(delimiter = ' ', value = {
            "= EQUAL", "< LESS", "> GREATER",
            "+ PLUS", "- MINUS", "* STAR", "/ SLASH", "% PERCENT",
            "( LEFT_PAREN", ") RIGHT_PAREN", ", COMMA", "; SEMICOLON"})
    void readsSingleCharacterOperators(String text, TokenType type) {
        assertEquals(token(type, text, 1), reader.read(new Cursor(text)));
    }

    @ParameterizedTest
    @CsvSource(delimiter = ' ', value = {
            "<= LESS_EQUAL", ">= GREATER_EQUAL", "<> NOT_EQUAL", "!= NOT_EQUAL"})
    void readsTwoCharacterOperators(String text, TokenType type) {
        Cursor cursor = new Cursor(text);

        assertEquals(token(type, text, 1), reader.read(cursor));
        assertTrue(cursor.isAtEnd());
    }

    @Test
    void doesNotConsumeCharacterThatIsNotPartOfOperator() {
        // '<' followed by '-': the '-' must be left for the next token.
        Cursor cursor = new Cursor("<-");

        assertEquals(token(LESS, "<", 1), reader.read(cursor));
        assertEquals('-', cursor.peek());
    }

    @Test
    void minusBeforeDigitIsJustMinus() {
        // Negative numbers are the Parser's job.
        Cursor cursor = new Cursor("-5");

        assertEquals(token(MINUS, "-", 1), reader.read(cursor));
        assertEquals('5', cursor.peek());
    }

    // --- Through the Lexer ---

    @Test
    void lessThenMinusAreTwoTokens() throws LexicalException {
        assertEquals(List.of(token(LESS, "<", 1), token(MINUS, "-", 2), token(EOF, "", 3)), lex("<-"));
    }

    @Test
    void spaceSplitsTwoCharacterOperator() throws LexicalException {
        // The Lexer does not guess: '< =' is LESS then EQUAL, and the Parser reports the error.
        assertEquals(List.of(token(LESS, "<", 1), token(EQUAL, "=", 3), token(EOF, "", 4)), lex("< ="));
    }

    @Test
    void longestMatchThenContinue() throws LexicalException {
        // '<>=' is NOT_EQUAL then EQUAL, never LESS then GREATER_EQUAL.
        assertEquals(List.of(token(NOT_EQUAL, "<>", 1), token(EQUAL, "=", 3), token(EOF, "", 4)), lex("<>="));
    }

    @Test
    void doubleSpacesInTheMiddleAreSkipped() throws LexicalException {
        // Required by Step 2: doubled whitespace between tokens is skipped and positions stay correct.
        assertEquals(List.of(
                token(LEFT_PAREN, "(", 1),
                token(STAR, "*", 4),
                token(RIGHT_PAREN, ")", 7),
                token(EOF, "", 10)), lex("(  *\t\t)  "));
    }

    @Test
    void semicolonIsAnOrdinaryTokenNotAStop() throws LexicalException {
        assertEquals(List.of(
                token(SEMICOLON, ";", 1),
                token(PLUS, "+", 2),
                token(EOF, "", 3)), lex(";+"));
    }

    @Test
    void loneBangIsALexicalError() {
        LexicalException e = assertThrows(LexicalException.class, () -> lex("  !"));

        assertEquals('!', e.character());
        assertEquals(1, e.line());
        assertEquals(3, e.column());
    }

    @Test
    void bangSpaceEqualsIsALexicalError() {
        // '! =' is not '!='; the '!' alone is invalid.
        assertThrows(LexicalException.class, () -> lex("! ="));
    }

    @Test
    void bangAtEndOfInputIsALexicalError() {
        // peek() returns '\0' at the end, which never completes '!='.
        assertThrows(LexicalException.class, () -> lex("!"));
    }
}
