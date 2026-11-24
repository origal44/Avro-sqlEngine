package com.avrosql.exception;

import java.util.Objects;

// Every error caused by the user's query; the REST layer maps all of them to HTTP 400.
public abstract class QueryException extends Exception {

    private final ErrorCode code;
    private final int line;
    private final int column;

    protected QueryException(ErrorCode code, String message, int line, int column) {
        super(message);
        this.code = Objects.requireNonNull(code, "code");
        this.line = line;
        this.column = column;
    }

    public ErrorCode code() {
        return code;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }
}
