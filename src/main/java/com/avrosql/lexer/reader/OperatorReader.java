package com.avrosql.lexer.reader;

import com.avrosql.lexer.token.TokenType;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/** Reads operators and punctuation. '-' always lands here; the Parser decides whether it means negation. */
public final class OperatorReader extends TokenReader {

    /** Every symbol spelling mapped to its type, built from {@link TokenType#symbols()}. */
    private static final Map<String, TokenType> SYMBOLS = Arrays.stream(TokenType.values())
            .flatMap(type -> type.symbols().stream().map(symbol -> Map.entry(symbol, type)))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    /** The first character of every symbol. Includes '!', which only appears as part of "!=". */
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

        // Lookahead: only take the second character if it completes a two-character symbol.
        TokenType twoCharType = SYMBOLS.get("" + first + token.peek());
        if (twoCharType != null) {
            token.take();
            return twoCharType;
        }
        // A start character that is not a symbol by itself, e.g. '!' without '='.
        TokenType oneCharType = SYMBOLS.get(String.valueOf(first));
        if (oneCharType == null) {
            throw token.error(first);
        }
        return oneCharType;
    }
}
