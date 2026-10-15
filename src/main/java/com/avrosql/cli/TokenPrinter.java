package com.avrosql.cli;

import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.LexicalException;
import com.avrosql.lexer.token.Token;

import java.util.List;

/** Runs the Lexer on one line of SQL and prints either a token table or the error with a caret under it. */
final class TokenPrinter {

    static final String INDENT = "  ";
    private static final String ROW = INDENT + "%-8s %-14s %s%n";
    private static final String POSITION = "%d:%d";
    private static final String HEADER_POSITION = "LINE:COL";
    private static final String HEADER_TYPE = "TYPE";
    private static final String HEADER_TEXT = "TEXT";
    private static final String ERROR_MARKER = "^";
    private static final String ERROR_LABEL = "Error: ";

    private TokenPrinter() {
    }

    static void print(Terminal terminal, String sql) {
        try {
            List<Token> tokens = new Lexer(sql).tokenize();
            terminal.printf(ROW, HEADER_POSITION, HEADER_TYPE, HEADER_TEXT);
            for (Token token : tokens) {
                terminal.printf(ROW, POSITION.formatted(token.line(), token.column()), token.type(), token.text());
            }
        } catch (LexicalException e) {
            terminal.println(INDENT + sql);
            terminal.println(INDENT + " ".repeat(e.column() - 1) + ERROR_MARKER);
            terminal.println(INDENT + ERROR_LABEL + e.getMessage());
        }
    }
}
