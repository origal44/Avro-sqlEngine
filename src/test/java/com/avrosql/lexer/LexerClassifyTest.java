package com.avrosql.lexer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerClassifyTest {

    private static void lex(String source) {
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
            long claimed = readers.stream().filter(r -> r.canStart(ch)).count();
            assertTrue(claimed <= 1, "character " + (int) c + " claimed by " + claimed + " readers");
        }
    }

    @Test
    void newTokenKindNeedsOnlyANewReader() {
        // Open/Closed: '#' becomes valid by plugging in a reader, without editing the Lexer.
        TokenReader hashReader = new TokenReader() {
            @Override
            public boolean canStart(char c) {
                return c == '#';
            }

            @Override
            public Token read(Cursor cursor) {
                int line = cursor.line();
                int column = cursor.column();
                cursor.advance();
                return new Token(TokenType.IDENTIFIER, "#", line, column);
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
