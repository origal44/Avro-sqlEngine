package com.avrosql.lexer.reader;

import com.avrosql.lexer.Chars;
import com.avrosql.lexer.token.TokenType;

/**
 * Reads numeric literals: digits, optionally followed by '.' and more digits.
 * Integers and decimals share the NUMBER type; later stages convert using the text.
 */
public final class NumberReader extends TokenReader {

    private static final char DECIMAL_POINT = '.';

    @Override
    public boolean isToken(char c) {
        return Chars.isDigit(c);
    }

    @Override
    protected TokenType scan(TokenBuilder token) {
        token.takeWhile(Chars::isDigit);
        if (token.peek() == DECIMAL_POINT) {
            // A dot must be followed by at least one digit: "3." is not a number.
            if (!Chars.isDigit(token.peekNext())) {
                throw token.errorHere(DECIMAL_POINT);
            }
            token.take();
            token.takeWhile(Chars::isDigit);
        }
        return TokenType.NUMBER;
    }
}
