package com.avrosql.parser.exception;

import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

public final class SyntaxException extends Exception {

    private static final String UNEXPECTED_TOKEN = "Expected %s but found %s";
    private static final String AT_POSITION = "%s at line %d, column %d";

    private final TokenType expected;
    private final Token found;

    public SyntaxException(TokenType expected, Token found) {
        this(UNEXPECTED_TOKEN.formatted(expected, describe(found)), expected, found);
    }

    // For errors that aren't about one expected token. expected() is null for these.
    public SyntaxException(String problem, Token found) {
        this(problem, null, found);
    }

    private SyntaxException(String problem, TokenType expected, Token found) {
        super(AT_POSITION.formatted(problem, found.line(), found.column()));
        this.expected = expected;
        this.found = found;
    }

    public static String describe(Token token) {
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
