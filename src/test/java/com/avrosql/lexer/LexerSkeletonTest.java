package com.avrosql.lexer;

import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerSkeletonTest {

    private static List<Token> lex(String source) throws LexicalException {
        return new Lexer(source).tokenize();
    }

    private static Token eof(int line, int column) {
        return new Token(TokenType.EOF, "", line, column);
    }

    @Test
    void emptyInputGivesOnlyEof() throws LexicalException {
        assertEquals(List.of(eof(1, 1)), lex(""));
    }

    @Test
    void onlySpacesGivesOnlyEof() throws LexicalException {
        assertEquals(List.of(eof(1, 4)), lex("   "));
    }

    @Test
    void singleNewlineMovesEofToNextLine() throws LexicalException {
        assertEquals(List.of(eof(2, 1)), lex("\n"));
    }

    @Test
    void allWhitespaceKindsAreSkipped() throws LexicalException {
        // tab, Windows newline, two spaces: no exception, only EOF.
        assertEquals(List.of(eof(2, 3)), lex("\t\r\n  "));
    }

    @Test
    void consecutiveSpacesAndNewlinesDoNotStopTheLoop() throws LexicalException {
        // Doubled whitespace is not an end marker; the loop runs to the real end.
        assertEquals(List.of(eof(3, 3)), lex("  \n\n  "));
    }

    @Test
    void eofIsAlwaysTheLastToken() throws LexicalException {
        List<Token> tokens = lex("  \t ");
        assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).type());
    }
}
