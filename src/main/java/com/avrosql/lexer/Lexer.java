package com.avrosql.lexer;

import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.lexer.exception.LexicalRuntimeException;
import com.avrosql.lexer.reader.IdentifierReader;
import com.avrosql.lexer.reader.NumberReader;
import com.avrosql.lexer.reader.OperatorReader;
import com.avrosql.lexer.reader.StringReader;
import com.avrosql.lexer.reader.TokenReader;
import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits SQL text into tokens. Syntax checks are the Parser's job, not the Lexer's.
 * The Lexer only drives the loop; each kind of token is read by its own {@link TokenReader}.
 */
public final class Lexer {

    /** EOF has no characters in the source. */
    private static final String EOF_TEXT = "";

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

    /**
     * Splits the whole input into tokens, ending with EOF.
     *
     * @throws LexicalException if a character cannot be part of any token
     */
    public List<Token> tokenize() throws LexicalException {
        List<Token> tokens = new ArrayList<>();
        try {
            // The only stop condition is reaching the end of the input.
            while (!cursor.isAtEnd()) {
                char c = cursor.peek();
                if (Chars.isWhitespace(c)) {
                    cursor.advance(); // must consume, or the loop never ends
                } else {
                    tokens.add(scanToken(c));
                }
            }
        } catch (LexicalRuntimeException e) {
            // Readers throw the unchecked form; callers of the Lexer get the checked one.
            throw e.getCause();
        }
        tokens.add(new Token(TokenType.EOF, EOF_TEXT, cursor.line(), cursor.column()));
        return List.copyOf(tokens);
    }

    /** Hands the token to the first reader that can start with this character. */
    private Token scanToken(char c) throws LexicalException {
        for (TokenReader reader : readers) {
            if (reader.isToken(c)) {
                return reader.read(cursor);
            }
        }
        throw new LexicalException(c, cursor.line(), cursor.column());
    }
}
