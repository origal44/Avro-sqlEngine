package com.avrosql.lexer.reader;

import com.avrosql.lexer.token.TokenType;

public final class StringReader extends TokenReader {

    private static final char QUOTE = '\'';
    private static final String UNTERMINATED = "Unterminated string starting at line %d, column %d";

    @Override
    public boolean isToken(char c) {
        return c == QUOTE;
    }

    @Override
    protected TokenType scan(TokenBuilder token) {
        token.take(); // opening quote

        while (true) {
            if (token.isAtEnd()) {
                throw token.error(UNTERMINATED.formatted(token.line(), token.column()), QUOTE);
            }
            if (token.take() == QUOTE) {
                if (token.peek() != QUOTE) {
                    break; // closing quote
                }
                token.take(); // '' is an escaped quote, keep reading
            }
        }
        return TokenType.STRING;
    }
}
