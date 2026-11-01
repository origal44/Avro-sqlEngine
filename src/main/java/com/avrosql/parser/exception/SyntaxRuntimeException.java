package com.avrosql.parser.exception;

// Thrown inside the parser; Parser.parse() unwraps it.
public final class SyntaxRuntimeException extends RuntimeException {

    public SyntaxRuntimeException(SyntaxException cause) {
        super(cause.getMessage(), cause);
    }

    @Override
    public synchronized SyntaxException getCause() {
        return (SyntaxException) super.getCause();
    }
}
