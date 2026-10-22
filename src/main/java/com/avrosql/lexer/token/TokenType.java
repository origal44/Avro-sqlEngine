package com.avrosql.lexer.token;

import java.util.List;

/**
 * All the token types the lexer can produce.
 * The readers build their lookup tables from this enum, so adding a keyword
 * or a symbol only needs a change here.
 *
 * COUNT, SUM, AVG, MIN and MAX aren't keywords. They come out as IDENTIFIER,
 * and the parser treats an identifier followed by '(' as a function call.
 */
public enum TokenType {
    // Keywords. The constant's name is how it's spelled (case-insensitive)
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

    // End of input. The lexer always adds this one last
    EOF(Category.END);

    /** Decides which reader handles the token. */
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

    /** How a SYMBOL is written ("<>" and "!=" for NOT_EQUAL). Empty for every other category. */
    public List<String> symbols() {
        return symbols;
    }
}