package com.avrosql.lexer;

/**
 * Reads single-quoted string literals. Inside a string nothing is classified:
 * only a closing quote ends it, and '' stands for one quote character.
 * The token text is the raw source, quotes and escapes included (e.g. 'it''s').
 */
final class StringReader implements TokenReader {

    @Override
    public boolean canStart(char c) {
        return c == '\'';
    }

    @Override
    public Token read(Cursor cursor) {
        int line = cursor.line();
        int column = cursor.column();
        StringBuilder text = new StringBuilder();
        text.append(cursor.advance()); // opening quote

        while (true) {
            if (cursor.isAtEnd()) {
                throw new LexicalException(
                        "Unterminated string starting at line " + line + ", column " + column,
                        '\'', line, column);
            }
            char c = cursor.advance();
            text.append(c);
            if (c == '\'') {
                if (cursor.peek() != '\'') {
                    break; // closing quote
                }
                text.append(cursor.advance()); // '' is an escaped quote, keep reading
            }
        }
        return new Token(TokenType.STRING, text.toString(), line, column);
    }
}
