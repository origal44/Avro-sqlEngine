package com.avrosql.lexer;

import java.util.Locale;
import java.util.Map;

import static java.util.Map.entry;

/**
 * Reads a whole word ([A-Za-z_][A-Za-z0-9_]*) and then decides whether it is a keyword.
 * Keyword lookup is case-insensitive; the token text always keeps the original case,
 * since Avro field names are case-sensitive.
 */
final class IdentifierReader implements TokenReader {

    // Only words with a single meaning everywhere in the language are keywords.
    // COUNT/SUM/AVG/MIN/MAX are deliberately absent: they stay IDENTIFIER.
    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            entry("SELECT", TokenType.SELECT),
            entry("FROM", TokenType.FROM),
            entry("WHERE", TokenType.WHERE),
            entry("AND", TokenType.AND),
            entry("OR", TokenType.OR),
            entry("NOT", TokenType.NOT),
            entry("IS", TokenType.IS),
            entry("NULL", TokenType.NULL),
            entry("BETWEEN", TokenType.BETWEEN),
            entry("IN", TokenType.IN),
            entry("LIKE", TokenType.LIKE),
            entry("AS", TokenType.AS),
            entry("DISTINCT", TokenType.DISTINCT),
            entry("ORDER", TokenType.ORDER),
            entry("BY", TokenType.BY),
            entry("ASC", TokenType.ASC),
            entry("DESC", TokenType.DESC),
            entry("LIMIT", TokenType.LIMIT),
            entry("OFFSET", TokenType.OFFSET),
            entry("GROUP", TokenType.GROUP),
            entry("HAVING", TokenType.HAVING));

    @Override
    public boolean canStart(char c) {
        return Chars.isLetter(c);
    }

    @Override
    public Token read(Cursor cursor) {
        int line = cursor.line();
        int column = cursor.column();
        StringBuilder text = new StringBuilder();

        // Read the whole word first, so "orders" is never cut into ORDER + "s".
        while (Chars.isLetter(cursor.peek()) || Chars.isDigit(cursor.peek())) {
            text.append(cursor.advance());
        }
        String word = text.toString();

        // Locale.ROOT: in a Turkish locale "limit".toUpperCase() would be "LİMİT" and miss the table.
        TokenType type = KEYWORDS.getOrDefault(word.toUpperCase(Locale.ROOT), TokenType.IDENTIFIER);
        return new Token(type, word, line, column);
    }
}
