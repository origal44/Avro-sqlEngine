package com.avrosql.parser;

import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;
import com.avrosql.parser.exception.SyntaxException;
import com.avrosql.parser.exception.SyntaxRuntimeException;

import java.util.List;

/**
 * Turns the Lexer's tokens into an AST.
 * Every parse method reads tokens only through peek, advance, check, match and expect.
 */
public final class Parser {

    private final List<Token> tokens;
    private int current = 0;

    /** @param tokens the Lexer's output, which always ends with EOF */
    public Parser(List<Token> tokens) {
        if (tokens.isEmpty() || tokens.getLast().type() != TokenType.EOF) {
            throw new IllegalArgumentException("token list must end with EOF");
        }
        this.tokens = List.copyOf(tokens);
    }

    /** The next token, without consuming it. */
    Token peek() {
        return tokens.get(current);
    }

    /** Consumes and returns the next token. At EOF it stays in place, so peek() always has a token. */
    Token advance() {
        Token token = peek();
        if (token.type() != TokenType.EOF) {
            current++;
        }
        return token;
    }

    /** Is the next token of this type? Never moves and never throws. */
    boolean check(TokenType type) {
        return peek().type() == type;
    }

    /** Consumes the next token only if it is of this type. Never throws. */
    boolean match(TokenType type) {
        if (!check(type)) {
            return false;
        }
        advance();
        return true;
    }

    /** Consumes and returns the next token, which must be of this type. */
    Token expect(TokenType type) {
        if (!check(type)) {
            throw new SyntaxRuntimeException(new SyntaxException(type, peek()));
        }
        return advance();
    }
}
