package com.avrosql.cli;

import com.avrosql.lexer.token.TokenType;
import com.avrosql.lexer.token.TokenType.Category;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Lists every token type, grouped by category. Keywords and symbols are read from {@link TokenType},
 * so a type added to the enum shows up here without editing this screen.
 */
final class TokenReferenceScreen implements Screen {

    private static final String TITLE = "Token reference";
    private static final String HEADING = "TOKEN REFERENCE";
    private static final String KEYWORDS_HEADING = "Keywords (case-insensitive)";
    private static final String SYMBOLS_HEADING = "Symbols";
    private static final String OTHERS_HEADING = "Names, values and end of input";
    private static final String ROW = TokenPrinter.INDENT + "%-14s %s%n";
    private static final String SPELLING_SEPARATOR = "  or  ";
    private static final String KEYWORD_SEPARATOR = ", ";
    private static final int LINE_WIDTH = 72;

    /** Types with no fixed spelling need a hand-written description. */
    private static final Map<TokenType, String> DESCRIPTIONS = new EnumMap<>(Map.of(
            TokenType.IDENTIFIER, "a name, e.g. age, _id, order_total (also COUNT, SUM, ...)",
            TokenType.NUMBER, "a whole or decimal number, e.g. 42 or 3.14",
            TokenType.STRING, "text in single quotes, e.g. 'Ori' or 'it''s'",
            TokenType.EOF, "added once, after the last real token"));

    @Override
    public String title() {
        return TITLE;
    }

    @Override
    public void show(Terminal terminal) {
        terminal.println(HEADING);

        terminal.println();
        terminal.println(KEYWORDS_HEADING);
        for (String line : wrap(namesOf(Category.KEYWORD))) {
            terminal.println(TokenPrinter.INDENT + line);
        }

        terminal.println();
        terminal.println(SYMBOLS_HEADING);
        for (TokenType type : typesOf(Category.SYMBOL)) {
            terminal.printf(ROW, type, String.join(SPELLING_SEPARATOR, type.symbols()));
        }

        terminal.println();
        terminal.println(OTHERS_HEADING);
        List<TokenType> others = new ArrayList<>(typesOf(Category.LITERAL));
        others.addAll(typesOf(Category.END));
        for (TokenType type : others) {
            terminal.printf(ROW, type, DESCRIPTIONS.getOrDefault(type, ""));
        }

        terminal.waitForEnter();
    }

    private static List<TokenType> typesOf(Category category) {
        List<TokenType> types = new ArrayList<>();
        for (TokenType type : TokenType.values()) {
            if (type.category() == category) {
                types.add(type);
            }
        }
        return types;
    }

    private static List<String> namesOf(Category category) {
        return typesOf(category).stream().map(TokenType::name).toList();
    }

    /** Joins the words with commas, starting a new line before one would pass LINE_WIDTH. */
    private static List<String> wrap(List<String> words) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (!line.isEmpty() && line.length() + KEYWORD_SEPARATOR.length() + word.length() > LINE_WIDTH) {
                lines.add(line + KEYWORD_SEPARATOR.strip());
                line.setLength(0);
            }
            if (!line.isEmpty()) {
                line.append(KEYWORD_SEPARATOR);
            }
            line.append(word);
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }
}
