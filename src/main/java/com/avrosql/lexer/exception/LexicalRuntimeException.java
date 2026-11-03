package com.avrosql.lexer.exception;

import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.LexicalError;

// Thrown inside the lexer; Lexer.tokenize() unwraps it.
public final class LexicalRuntimeException extends RuntimeException implements LexicalError {

    public LexicalRuntimeException(LexicalException cause) {
        super(cause.getMessage(), cause);
    }

    @Override
    public synchronized LexicalException getCause() {
        return (LexicalException) super.getCause();
    }

    @Override
    public char character() {
        return getCause().character();
    }

    @Override
    public int line() {
        return getCause().line();
    }

    @Override
    public int column() {
        return getCause().column();
    }
}
