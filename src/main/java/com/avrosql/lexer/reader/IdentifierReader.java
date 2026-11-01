package com.avrosql.lexer.reader;

import com.avrosql.lexer.Chars;
import com.avrosql.lexer.token.TokenType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class IdentifierReader extends TokenReader {

    // COUNT/SUM/AVG/MIN/MAX aren't keywords, they stay IDENTIFIER
    private static final Map<String, TokenType> KEYWORDS = Arrays.stream(TokenType.values())
            .filter(type -> type.category() == TokenType.Category.KEYWORD)
            .collect(Collectors.toUnmodifiableMap(TokenType::name, Function.identity()));

    @Override
    public boolean isToken(char c) {
        return Chars.isLetter(c);
    }

    @Override
    protected TokenType scan(TokenBuilder token) {
        // whole word first, so "orders" isn't split into ORDER + "s"
        token.takeWhile(c -> Chars.isLetter(c) || Chars.isDigit(c));

        // Locale.ROOT: in a Turkish locale "limit" upper-cases to "LİMİT"
        return KEYWORDS.getOrDefault(token.text().toUpperCase(Locale.ROOT), TokenType.IDENTIFIER);
    }
}
