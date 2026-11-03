package com.avrosql.lexer.token;

import java.util.List;

// COUNT, SUM, AVG, MIN, MAX are IDENTIFIER; the parser sees name + '(' as a call.
public enum TokenType {
    // Keywords, spelled like the constant name (case-insensitive)
    SELECT(Category.KEYWORD), FROM(Category.KEYWORD), WHERE(Category.KEYWORD),
    AND(Category.KEYWORD), OR(Category.KEYWORD), NOT(Category.KEYWORD),
    IS(Category.KEYWORD), NULL(Category.KEYWORD), BETWEEN(Category.KEYWORD),
    IN(Category.KEYWORD), LIKE(Category.KEYWORD),
    AS(Category.KEYWORD), DISTINCT(Category.KEYWORD),
    ORDER(Category.KEYWORD), BY(Category.KEYWORD), ASC(Category.KEYWORD), DESC(Category.KEYWORD),
    LIMIT(Category.KEYWORD), OFFSET(Category.KEYWORD),
    GROUP(Category.KEYWORD), HAVING(Category.KEYWORD),

    // Names and literal values
    IDENTIFIER(Category.LITERAL), NUMBER(Category.LITERAL), STRING(Category.LITERAL),

    // Comparison
    EQUAL(Category.SYMBOL, "="),
    NOT_EQUAL(Category.SYMBOL, "<>", "!="),
    LESS(Category.SYMBOL, "<"),
    LESS_EQUAL(Category.SYMBOL, "<="),
    GREATER(Category.SYMBOL, ">"),
    GREATER_EQUAL(Category.SYMBOL, ">="),

    // Arithmetic
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

    // always the last token
    EOF(Category.END);

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

    public List<String> symbols() {
        return symbols;
    }
}