package com.avrosql.lexer.token;

import java.util.List;

/**
 * Every category of token the Lexer can produce, and how each one is spelled in SQL.
 * This enum is the single source of truth: the readers build their lookup tables from it.
 * COUNT/SUM/AVG/MIN/MAX are intentionally absent: they are lexed as IDENTIFIER
 * and recognized by the Parser as function calls (IDENTIFIER followed by '(').
 */
public enum TokenType {
    // Keywords: spelled exactly like the constant's name (matched case-insensitively)
    SELECT(Category.KEYWORD), FROM(Category.KEYWORD), WHERE(Category.KEYWORD),
    AND(Category.KEYWORD), OR(Category.KEYWORD), NOT(Category.KEYWORD),
    IS(Category.KEYWORD), NULL(Category.KEYWORD), BETWEEN(Category.KEYWORD),
    IN(Category.KEYWORD), LIKE(Category.KEYWORD),
    AS(Category.KEYWORD), DISTINCT(Category.KEYWORD),
    ORDER(Category.KEYWORD), BY(Category.KEYWORD), ASC(Category.KEYWORD), DESC(Category.KEYWORD),
    LIMIT(Category.KEYWORD), OFFSET(Category.KEYWORD),
    GROUP(Category.KEYWORD), HAVING(Category.KEYWORD),

    // Literals and names: no fixed spelling
    IDENTIFIER(Category.LITERAL), NUMBER(Category.LITERAL), STRING(Category.LITERAL),

    // Comparison operators
    EQUAL(Category.SYMBOL, "="),
    NOT_EQUAL(Category.SYMBOL, "<>", "!="),
    LESS(Category.SYMBOL, "<"),
    LESS_EQUAL(Category.SYMBOL, "<="),
    GREATER(Category.SYMBOL, ">"),
    GREATER_EQUAL(Category.SYMBOL, ">="),

    // Arithmetic operators
    PLUS(Category.SYMBOL, "+"),
    MINUS(Category.SYMBOL, "-"),
    STAR(Category.SYMBOL, "*"),
    SLASH(Category.SYMBOL, "/"),
    PERCENT(Category.SYMBOL, "%"),

    // Punctuation
    LEFT_PAREN(Category.SYMBOL, "("),
    RIGHT_PAREN(Category.SYMBOL, ")"),
    COMMA(Category.SYMBOL, ","),
    SEMICOLON(Category.SYMBOL, ";"),

    // Sentinel: always the last token in the list
    EOF(Category.END);

    /** What kind of token a type is, which decides which reader produces it. */
    public enum Category { KEYWORD, LITERAL, SYMBOL, END }

    private final Category category;
    private final List<String> symbols;

    TokenType(Category category, String... symbols) {
        this.category = category;
        this.symbols = List.of(symbols);
    }

    public Category category() {
        return category;
    }

    /** The exact spellings of a SYMBOL type (e.g. "<>" and "!=" for NOT_EQUAL); empty for every other category. */
    public List<String> symbols() {
        return symbols;
    }
}
