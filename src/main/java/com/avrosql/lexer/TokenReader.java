package com.avrosql.lexer;

/**
 * Reads one kind of token. Each implementation owns the rules for its token kind,
 * so adding a new kind means adding a reader, not editing the Lexer.
 */
interface TokenReader {

    /** Whether a token of this kind can start with the given character. */
    boolean canStart(char c);

    /**
     * Reads one token starting at the cursor's current position.
     * Called only when {@link #canStart(char)} returned true for {@code cursor.peek()}.
     */
    Token read(Cursor cursor);
}
