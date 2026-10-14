package com.avrosql.lexer;

/**
 * Every category of token the Lexer can produce.
 * COUNT/SUM/AVG/MIN/MAX are intentionally absent: they are lexed as IDENTIFIER
 * and recognized by the Parser as function calls (IDENTIFIER followed by '(').
 */
public enum TokenType {
    // Keywords
    SELECT, FROM, WHERE,
    AND, OR, NOT,
    IS, NULL, BETWEEN, IN, LIKE,
    AS, DISTINCT,
    ORDER, BY, ASC, DESC,
    LIMIT, OFFSET,
    GROUP, HAVING,

    // Literals and names
    IDENTIFIER, NUMBER, STRING,

    // Comparison operators
    EQUAL,          // =
    NOT_EQUAL,      // <> or !=
    LESS,           // <
    LESS_EQUAL,     // <=
    GREATER,        // >
    GREATER_EQUAL,  // >=

    // Arithmetic operators
    PLUS, MINUS, STAR, SLASH, PERCENT,

    // Punctuation
    LEFT_PAREN, RIGHT_PAREN, COMMA, SEMICOLON,

    // Sentinel: always the last token in the list
    EOF
}
