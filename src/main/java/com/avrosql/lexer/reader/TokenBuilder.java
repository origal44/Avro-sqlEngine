package com.avrosql.lexer.reader;

import com.avrosql.lexer.Cursor;
import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.lexer.exception.LexicalRuntimeException;
import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

public final class TokenBuilder {

    @FunctionalInterface
    public interface CharTest {
        boolean test(char c);
    }

    private final Cursor cursor;
    private final int line;
    private final int column;
    private final StringBuilder text = new StringBuilder();

    TokenBuilder(Cursor cursor) {
        this.cursor = cursor;
        this.line = cursor.line();
        this.column = cursor.column();
    }

    public char peek() {
        return cursor.peek();
    }

    public char peekNext() {
        return cursor.peekNext();
    }

    public boolean isAtEnd() {
        return cursor.isAtEnd();
    }

    public char take() {
        char c = cursor.advance();
        text.append(c);
        return c;
    }

    public void takeWhile(CharTest test) {
        while (test.test(cursor.peek())) {
            take();
        }
    }

    public String text() {
        return text.toString();
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    public LexicalRuntimeException error(char character) {
        return new LexicalRuntimeException(new LexicalException(character, line, column));
    }

    public LexicalRuntimeException error(String message, char character) {
        return new LexicalRuntimeException(new LexicalException(message, character, line, column));
    }

    public LexicalRuntimeException errorHere(char character) {
        return new LexicalRuntimeException(new LexicalException(character, cursor.line(), cursor.column()));
    }

    Token build(TokenType type) {
        return new Token(type, text.toString(), line, column);
    }
}
