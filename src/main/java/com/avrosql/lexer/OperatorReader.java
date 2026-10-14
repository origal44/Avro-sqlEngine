package com.avrosql.lexer;

/** Reads operators and punctuation. '-' always lands here; the Parser decides whether it means negation. */
final class OperatorReader implements TokenReader {

    @Override
    public boolean canStart(char c) {
        return "=<>!+-*/%(),;".indexOf(c) >= 0;
    }

    @Override
    public Token read(Cursor cursor) {
        int line = cursor.line();
        int column = cursor.column();
        char first = cursor.advance();

        // Lookahead: only consume the second character if it completes a two-character operator.
        TokenType twoCharType = twoCharType(first, cursor.peek());
        if (twoCharType != null) {
            char second = cursor.advance();
            return new Token(twoCharType, "" + first + second, line, column);
        }
        if (first == '!') {
            // '!' has no meaning on its own; it only exists as part of '!='.
            throw new LexicalException(first, line, column);
        }
        return new Token(singleCharType(first), String.valueOf(first), line, column);
    }

    /** Returns the type of a two-character operator, or null if the pair is not one. */
    private static TokenType twoCharType(char first, char second) {
        return switch ("" + first + second) {
            case "<=" -> TokenType.LESS_EQUAL;
            case ">=" -> TokenType.GREATER_EQUAL;
            case "<>", "!=" -> TokenType.NOT_EQUAL;
            default -> null;
        };
    }

    private static TokenType singleCharType(char c) {
        return switch (c) {
            case '=' -> TokenType.EQUAL;
            case '<' -> TokenType.LESS;
            case '>' -> TokenType.GREATER;
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.MINUS;
            case '*' -> TokenType.STAR;
            case '/' -> TokenType.SLASH;
            case '%' -> TokenType.PERCENT;
            case '(' -> TokenType.LEFT_PAREN;
            case ')' -> TokenType.RIGHT_PAREN;
            case ',' -> TokenType.COMMA;
            case ';' -> TokenType.SEMICOLON;
            default -> throw new IllegalStateException("not an operator: '" + c + "'");
        };
    }
}
