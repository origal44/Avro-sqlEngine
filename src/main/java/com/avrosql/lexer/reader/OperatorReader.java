package com.avrosql.lexer.reader;

import com.avrosql.lexer.token.TokenType;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public final class OperatorReader extends TokenReader {

    private static final Map<String, TokenType> SYMBOLS = Arrays.stream(TokenType.values())
            .flatMap(type -> type.symbols().stream().map(symbol -> Map.entry(symbol, type)))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final String START_CHARACTERS = SYMBOLS.keySet().stream()
            .map(symbol -> symbol.substring(0, 1))
            .distinct()
            .collect(Collectors.joining());

    @Override
    public boolean isToken(char c) {
        return START_CHARACTERS.indexOf(c) >= 0;
    }

    @Override
    protected TokenType scan(TokenBuilder token) {
        char first = token.take();

        TokenType twoCharType = SYMBOLS.get("" + first + token.peek());
        if (twoCharType != null) {
            token.take();
            return twoCharType;
        }
        TokenType oneCharType = SYMBOLS.get(String.valueOf(first));
        // e.g. '!' without '='
        if (oneCharType == null) {
            throw token.error(first);
        }
        return oneCharType;
    }
}
