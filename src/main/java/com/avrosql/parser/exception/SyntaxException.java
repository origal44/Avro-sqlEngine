package com.avrosql.parser.exception;

import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

/**
 * Valid tokens in the wrong place. Characters that aren't part of the language
 * are a LexicalException, thrown earlier by the Lexer.
 * Checked, like LexicalException, so every caller of the Parser has to handle bad queries.
 * The position comes from the token that was found.
 */
public final class SyntaxException extends Exception {

    private static final String UNEXPECTED_TOKEN = "Expected %s but found %s at line %d, column %d";

    private final TokenType expected;
    private final Token found;

    public SyntaxException(TokenType expected, Token found) {
        super(UNEXPECTED_TOKEN.formatted(expected, describe(found), found.line(), found.column()));
        this.expected = expected;
        this.found = found;
    }

    /** EOF has no text, so it gets a readable name instead of empty quotes. */
    private static String describe(Token token) {
        if (token.type() == TokenType.EOF) {
            return "end of input (EOF)";
        }
        return "'%s' (%s)".formatted(token.text(), token.type());
    }

    public TokenType expected() {
        return expected;
    }

    public Token found() {
        return found;
    }

    public int line() {
        return found.line();
    }

    public int column() {
        return found.column();
    }
}
