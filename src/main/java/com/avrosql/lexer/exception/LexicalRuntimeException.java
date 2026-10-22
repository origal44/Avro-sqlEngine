package com.avrosql.lexer.exception;

import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.LexicalError;

/**
 * The unchecked form of a {@link LexicalException}, used inside the lexer.
 * Readers throw it so their methods don't need {@code throws} clauses;
 * {@link Lexer#tokenize()} catches it and rethrows the checked exception it wraps.
 */
public final class LexicalRuntimeException extends RuntimeException implements LexicalError {

    public LexicalRuntimeException(LexicalException cause) {
        super(cause.getMessage(), cause);
    }

    /** The checked exception to report to callers of the Lexer. */
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
