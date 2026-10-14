package com.avrosql.lexer;

/**
 * Reads numeric literals: digits, optionally followed by '.' and more digits.
 * Integers and decimals share the NUMBER type; later stages convert using the text.
 */
final class NumberReader implements TokenReader {

    @Override
    public boolean canStart(char c) {
        return Chars.isDigit(c);
    }

    @Override
    public Token read(Cursor cursor) {
        int line = cursor.line();
        int column = cursor.column();
        StringBuilder text = new StringBuilder();

        readDigits(cursor, text);
        if (cursor.peek() == '.') {
            int dotLine = cursor.line();
            int dotColumn = cursor.column();
            text.append(cursor.advance());
            // A dot must be followed by at least one digit: "3." is not a number.
            if (!Chars.isDigit(cursor.peek())) {
                throw new LexicalException('.', dotLine, dotColumn);
            }
            readDigits(cursor, text);
        }
        return new Token(TokenType.NUMBER, text.toString(), line, column);
    }

    private static void readDigits(Cursor cursor, StringBuilder text) {
        while (Chars.isDigit(cursor.peek())) {
            text.append(cursor.advance());
        }
    }
}
