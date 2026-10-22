package com.avrosql.lexer.reader;

import com.avrosql.lexer.Cursor;
import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Locale;

import static com.avrosql.lexer.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

class IdentifierReaderTest {

    private final IdentifierReader reader = new IdentifierReader();

    private static List<Token> lex(String source) throws LexicalException {
        return new Lexer(source).tokenize();
    }

    private static Token token(TokenType type, String text, int column) {
        return new Token(type, text, 1, column);
    }

    // --- The reader on its own ---

    @ParameterizedTest
    @ValueSource(strings = {"age", "_name", "order_id", "col1", "A_1_b", "x"})
    void readsIdentifiers(String text) {
        Cursor cursor = new Cursor(text);

        assertEquals(token(IDENTIFIER, text, 1), reader.read(cursor));
        assertTrue(cursor.isAtEnd());
    }

    @Test
    void stopsAtFirstNonWordCharacterWithoutConsumingIt() {
        Cursor cursor = new Cursor("age>5");

        assertEquals(token(IDENTIFIER, "age", 1), reader.read(cursor));
        assertEquals('>', cursor.peek());
    }

    @ParameterizedTest
    @EnumSource(value = TokenType.class, names = {
            "SELECT", "FROM", "WHERE", "AND", "OR", "NOT", "IS", "NULL", "BETWEEN", "IN", "LIKE",
            "AS", "DISTINCT", "ORDER", "BY", "ASC", "DESC", "LIMIT", "OFFSET", "GROUP", "HAVING"})
    void everyKeywordIsRecognizedInAnyCase(TokenType keyword) {
        String upper = keyword.name();
        String lower = upper.toLowerCase(Locale.ROOT);
        String mixed = upper.charAt(0) + lower.substring(1);

        for (String text : List.of(upper, lower, mixed)) {
            // The type is the keyword; the text keeps exactly what was written.
            assertEquals(token(keyword, text, 1), reader.read(new Cursor(text)));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"orders", "order_id", "selection", "fromage", "notes", "isbn", "in1", "_select"})
    void wordsThatStartWithAKeywordAreIdentifiers(String text) {
        // The whole word is read first, so a keyword prefix never splits it.
        assertEquals(token(IDENTIFIER, text, 1), reader.read(new Cursor(text)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"COUNT", "sum", "Avg", "MIN", "max"})
    void aggregateFunctionsAreIdentifiers(String text) {
        assertEquals(token(IDENTIFIER, text, 1), reader.read(new Cursor(text)));
    }

    @Test
    void identifierKeepsOriginalCase() {
        // Avro names are case-sensitive; matching policy is decided later, not here.
        assertEquals("Age", reader.read(new Cursor("Age")).text());
        assertEquals("age", reader.read(new Cursor("age")).text());
    }

    @Test
    void keywordLookupDoesNotDependOnDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"));
            assertEquals(LIMIT, reader.read(new Cursor("limit")).type());
            assertEquals(DISTINCT, reader.read(new Cursor("distinct")).type());
        } finally {
            Locale.setDefault(original);
        }
    }

    // --- Through the Lexer ---

    @Test
    void orderByIsTwoKeywords() throws LexicalException {
        assertEquals(List.of(
                token(ORDER, "ORDER", 1),
                token(BY, "BY", 7),
                token(IDENTIFIER, "age", 10),
                token(DESC, "desc", 14),
                token(EOF, "", 18)), lex("ORDER BY age desc"));
    }

    @Test
    void functionCallIsIdentifierThenParenthesis() throws LexicalException {
        assertEquals(List.of(
                token(IDENTIFIER, "COUNT", 1),
                token(LEFT_PAREN, "(", 6),
                token(STAR, "*", 7),
                token(RIGHT_PAREN, ")", 8),
                token(EOF, "", 9)), lex("COUNT(*)"));
    }

    @Test
    void numberFollowedByLettersIsTwoTokens() throws LexicalException {
        // From Step 5: 42abc is NUMBER then IDENTIFIER, and the Parser rejects it.
        assertEquals(List.of(
                token(NUMBER, "42", 1),
                token(IDENTIFIER, "abc", 3),
                token(EOF, "", 6)), lex("42abc"));
    }

    @Test
    void nonAsciiLetterAfterIdentifierIsALexicalError() {
        LexicalException e = assertThrows(LexicalException.class, () -> lex("abcא"));

        assertEquals('א', e.character());
        assertEquals(4, e.column());
    }
}
