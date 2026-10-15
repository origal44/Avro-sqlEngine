package com.avrosql.lexer.reader;

import com.avrosql.lexer.Cursor;
import com.avrosql.lexer.LexicalException;
import com.avrosql.lexer.LexicalRuntimeException;
import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

/**
 * The token currently being read: remembers where it started and collects its characters.
 * Readers consume input only through {@link #take()}, so the token text always matches the source.
 */
public final class TokenBuilder {

    /** A test on a single character, e.g. {@code Chars::isDigit}. */
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

    /** Consumes the current character and adds it to the token text. */
    public char take() {
        char c = cursor.advance();
        text.append(c);
        return c;
    }

    /** Takes characters for as long as they pass the test. */
    public void takeWhile(CharTest test) {
        while (test.test(cursor.peek())) {
            take();
        }
    }

    /** The text taken so far. */
    public String text() {
        return text.toString();
    }

    /** Line where the token starts. */
    public int line() {
        return line;
    }

    /** Column where the token starts. */
    public int column() {
        return column;
    }

    /** An error at the start of the token. */
    public LexicalRuntimeException error(char character) {
        return new LexicalRuntimeException(new LexicalException(character, line, column));
    }

    /** An error at the start of the token, with a custom message. */
    public LexicalRuntimeException error(String message, char character) {
        return new LexicalRuntimeException(new LexicalException(message, character, line, column));
    }

    /** An error at the cursor's current position (the next character to be read). */
    public LexicalRuntimeException errorHere(char character) {
        return new LexicalRuntimeException(new LexicalException(character, cursor.line(), cursor.column()));
    }

    Token build(TokenType type) {
        return new Token(type, text.toString(), line, column);
    }
}
