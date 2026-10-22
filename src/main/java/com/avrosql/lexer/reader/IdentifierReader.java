package com.avrosql.lexer.reader;

import com.avrosql.lexer.Chars;
import com.avrosql.lexer.token.TokenType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reads a whole word ([A-Za-z_][A-Za-z0-9_]*) and then decides whether it is a keyword.
 * Keyword lookup is case-insensitive; the token text always keeps the original case,
 * since Avro field names are case-sensitive.
 */
public final class IdentifierReader extends TokenReader {

    // Built from every KEYWORD in TokenType; each is spelled like its constant's name.
    // COUNT/SUM/AVG/MIN/MAX are deliberately not keywords: they stay IDENTIFIER.
    private static final Map<String, TokenType> KEYWORDS = Arrays.stream(TokenType.values())
            .filter(type -> type.category() == TokenType.Category.KEYWORD)
            .collect(Collectors.toUnmodifiableMap(TokenType::name, Function.identity()));

    @Override
    public boolean isToken(char c) {
        return Chars.isLetter(c);
    }

    @Override
    protected TokenType scan(TokenBuilder token) {
        // Read the whole word first, so "orders" is never cut into ORDER + "s".
        token.takeWhile(c -> Chars.isLetter(c) || Chars.isDigit(c));

        // Locale.ROOT: in a Turkish locale "limit".toUpperCase() would be "LİMİT" and miss the table.
        return KEYWORDS.getOrDefault(token.text().toUpperCase(Locale.ROOT), TokenType.IDENTIFIER);
    }
}
