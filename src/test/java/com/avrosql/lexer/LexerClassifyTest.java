package com.avrosql.lexer;

import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.lexer.reader.IdentifierReader;
import com.avrosql.lexer.reader.NumberReader;
import com.avrosql.lexer.reader.OperatorReader;
import com.avrosql.lexer.reader.StringReader;
import com.avrosql.lexer.reader.TokenBuilder;
import com.avrosql.lexer.reader.TokenReader;
import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerClassifyTest {

    private static void lex(String source) throws LexicalException {
        new Lexer(source).tokenize();
    }

    @ParameterizedTest
    @ValueSource(strings = {"#", "@", "$", "\"", "`", "?", ".", "א", "é"})
    void unknownCharactersThrowLexicalException(String source) {
        LexicalException e = assertThrows(LexicalException.class, () -> lex(source));
        assertEquals(source.charAt(0), e.character());
    }

    @Test
    void noCharacterIsClaimedByTwoReaders() {
        // Readers must not overlap, so their order in the list can never change the result.
        List<TokenReader> readers = List.of(
                new OperatorReader(), new NumberReader(), new StringReader(), new IdentifierReader());
        for (char c = 0; c < 128; c++) {
            final char ch = c;
            long claimed = readers.stream().filter(r -> r.isToken(ch)).count();
            assertTrue(claimed <= 1, "character " + (int) c + " claimed by " + claimed + " readers");
        }
    }

    @Test
    void newTokenKindNeedsOnlyANewReader() throws LexicalException {
        // Open/Closed: '#' becomes valid by plugging in a reader, without editing the Lexer.
        TokenReader hashReader = new TokenReader() {
            @Override
            public boolean isToken(char c) {
                return c == '#';
            }

            @Override
            protected TokenType scan(TokenBuilder token) {
                token.take();
                return TokenType.IDENTIFIER;
            }
        };

        List<Token> tokens = new Lexer(" #", List.of(hashReader)).tokenize();

        assertEquals(List.of(
                new Token(TokenType.IDENTIFIER, "#", 1, 2),
                new Token(TokenType.EOF, "", 1, 3)), tokens);
    }

    @Test
    void lexicalExceptionReportsPositionAfterWhitespace() {
        LexicalException e = assertThrows(LexicalException.class, () -> lex("  \n   #"));

        assertEquals('#', e.character());
        assertEquals(2, e.line());
        assertEquals(4, e.column());
        assertEquals("Unexpected character '#' at line 2, column 4", e.getMessage());
    }
}
