package com.avrosql.lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits SQL text into tokens. Syntax checks are the Parser's job, not the Lexer's.
 * The Lexer only drives the loop; each kind of token is read by its own {@link TokenReader}.
 */
public final class Lexer {

    private final Cursor cursor;
    private final List<TokenReader> readers;

    public Lexer(String source) {
        this(source, defaultReaders());
    }

    /** Lets callers (mainly tests) supply their own readers. */
    Lexer(String source, List<TokenReader> readers) {
        this.cursor = new Cursor(source);
        this.readers = List.copyOf(readers);
    }

    private static List<TokenReader> defaultReaders() {
        return List.of(
                new OperatorReader(),
                new NumberReader(),
                new StringReader(),
                new IdentifierReader());
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        // The only stop condition is reaching the end of the input.
        while (!cursor.isAtEnd()) {
            char c = cursor.peek();
            if (Chars.isWhitespace(c)) {
                cursor.advance(); // must consume, or the loop never ends
            } else {
                tokens.add(scanToken(c));
            }
        }
        tokens.add(new Token(TokenType.EOF, "", cursor.line(), cursor.column()));
        return List.copyOf(tokens);
    }

    /** Hands the token to the first reader that can start with this character. */
    private Token scanToken(char c) {
        for (TokenReader reader : readers) {
            if (reader.canStart(c)) {
                return reader.read(cursor);
            }
        }
        throw new LexicalException(c, cursor.line(), cursor.column());
    }
}
