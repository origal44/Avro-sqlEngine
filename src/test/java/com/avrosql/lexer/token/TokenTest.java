package com.avrosql.lexer.token;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenTest {

    @Test
    void storesTypeTextAndPosition() {
        Token token = new Token(TokenType.IDENTIFIER, "age", 2, 8);

        assertEquals(TokenType.IDENTIFIER, token.type());
        assertEquals("age", token.text());
        assertEquals(2, token.line());
        assertEquals(8, token.column());
    }

    @Test
    void tokensWithSameFieldsAreEqual() {
        // Later tests compare whole token lists, so value equality matters.
        assertEquals(new Token(TokenType.NUMBER, "42", 1, 5),
                     new Token(TokenType.NUMBER, "42", 1, 5));
    }

    @Test
    void tokensWithDifferentTextAreNotEqual() {
        // Same type, different value: text is what tells them apart.
        assertNotEquals(new Token(TokenType.IDENTIFIER, "age", 1, 1),
                        new Token(TokenType.IDENTIFIER, "name", 1, 1));
    }

    @Test
    void eofTokenHasEmptyText() {
        Token eof = new Token(TokenType.EOF, "", 1, 1);

        assertEquals(TokenType.EOF, eof.type());
        assertEquals("", eof.text());
    }

    @Test
    void rejectsNullTypeOrText() {
        assertThrows(NullPointerException.class, () -> new Token(null, "x", 1, 1));
        assertThrows(NullPointerException.class, () -> new Token(TokenType.IDENTIFIER, null, 1, 1));
    }

    @Test
    void rejectsPositionsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new Token(TokenType.EOF, "", 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Token(TokenType.EOF, "", 1, 0));
    }

    @Test
    void tokenTypeIncludesSentinelAndSemicolon() {
        assertNotNull(TokenType.valueOf("EOF"));
        assertNotNull(TokenType.valueOf("SEMICOLON"));
    }

    @Test
    void aggregateFunctionsAreNotKeywords() {
        // Decision: COUNT/SUM/AVG/MIN/MAX are lexed as IDENTIFIER.
        for (String name : new String[] {"COUNT", "SUM", "AVG", "MIN", "MAX"}) {
            assertThrows(IllegalArgumentException.class, () -> TokenType.valueOf(name));
        }
    }
}
