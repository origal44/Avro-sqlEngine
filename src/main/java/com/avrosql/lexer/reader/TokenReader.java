package com.avrosql.lexer.reader;

import com.avrosql.lexer.Cursor;
import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.lexer.exception.LexicalRuntimeException;
import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;

public abstract class TokenReader {

    public abstract boolean isToken(char c);

    public final Token read(Cursor cursor) {
        TokenBuilder token = new TokenBuilder(cursor);
        TokenType type = scan(token);
        return token.build(type);
    }

    protected abstract TokenType scan(TokenBuilder token);
}
