package com.avrosql.parser.ast;

// Where a node starts in the query, 1-based. UNKNOWN is for nodes built in code, not parsed.
public record Position(int line, int column) {

    public static final Position UNKNOWN = new Position(0, 0);

    public Position {
        if (line < 0 || column < 0) {
            throw new IllegalArgumentException("line and column must not be negative");
        }
    }
}
