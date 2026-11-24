package com.avrosql.exception;

// Tests assert on these, never on message text.
public enum ErrorCode {
    // lexer
    UNEXPECTED_CHARACTER,
    UNTERMINATED_STRING,

    // parser
    UNEXPECTED_TOKEN,
    EXPECTED_EXPRESSION,
    EXPECTED_INTEGER,
    NUMBER_TOO_LARGE,
    CHAINED_COMPARISON,
    EXPECTED_AFTER_NOT,
    UNKNOWN_FUNCTION
}
