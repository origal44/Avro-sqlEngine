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

public final class Lexer {

    private static final String EOF_TEXT = "";

    private final Cursor cursor;
    private final List<TokenReader> readers;

    public Lexer(String source) {
        this(source, defaultReaders());
    }

    // package-private so tests can plug in their own readers
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

    public List<Token> tokenize() throws LexicalException {
        List<Token> tokens = new ArrayList<>();
        try {
            while (!cursor.isAtEnd()) {
                char c = cursor.peek();
                if (Chars.isWhitespace(c)) {
                    cursor.advance(); // must consume, or the loop never ends
                } else {
                    tokens.add(scanToken(c));
                }
            }
        } catch (LexicalRuntimeException e) {
            // readers throw the unchecked one, callers get the checked one
            throw e.getCause();
        }
        tokens.add(new Token(TokenType.EOF, EOF_TEXT, cursor.line(), cursor.column()));
        return List.copyOf(tokens);
    }

    private Token scanToken(char c) throws LexicalException {
        for (TokenReader reader : readers) {
            if (reader.isToken(c)) {
                return reader.read(cursor);
            }
        }
        throw new LexicalException(c, cursor.line(), cursor.column());
    }
}
