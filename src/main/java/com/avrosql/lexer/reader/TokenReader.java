package com.avrosql.lexer.reader;

import com.avrosql.lexer.Cursor;
import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

/**
 * Reads one kind of token. Each subclass owns the rules for its token kind,
 * so adding a new kind means adding a reader, not editing the Lexer.
 *
 * <p>{@link #read(Cursor)} holds the steps every reader shares (remember the start position,
 * collect the text, build the Token); subclasses only fill in {@link #scan(TokenBuilder)}.
 */
public abstract class TokenReader {

    /** Whether a token of this kind can start with the given character. */
    public abstract boolean isToken(char c);

    /**
     * Reads one token starting at the cursor's current position.
     * Called only when {@link #isToken(char)} returned true for {@code cursor.peek()}.
     * Throws {@link com.avrosql.lexer.LexicalRuntimeException} for bad input; the Lexer turns it into the checked
     * {@link com.avrosql.lexer.LexicalException}.
     */
    public final Token read(Cursor cursor) {
        TokenBuilder token = new TokenBuilder(cursor);
        TokenType type = scan(token);
        return token.build(type);
    }

    /** Takes this token's characters and returns its type. */
    protected abstract TokenType scan(TokenBuilder token);
}
