package com.avrosql.parser.exception;

/**
 * The unchecked form of a {@link SyntaxException}, used inside the parser.
 * The parse methods throw it so they don't need {@code throws} clauses;
 * the Parser's public entry point catches it and rethrows the checked exception it wraps.
 */
public final class SyntaxRuntimeException extends RuntimeException {

    public SyntaxRuntimeException(SyntaxException cause) {
        super(cause.getMessage(), cause);
    }

    /** The checked exception to report to callers of the Parser. */
    @Override
    public synchronized SyntaxException getCause() {
        return (SyntaxException) super.getCause();
    }
}
