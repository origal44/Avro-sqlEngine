package com.avrosql;

import com.avrosql.cli.AstPrinter;
import com.avrosql.cli.Terminal;
import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.parser.Parser;
import com.avrosql.parser.exception.SyntaxException;

public final class ParserDemo {

    private static final String BANNER = "=== Avro SQL Parser ===";
    private static final String INTRO = "Type a query on one line, ending with ';', and press Enter.\n"
            + "Enter an empty line to exit.";
    private static final String PROMPT = "sql> ";
    private static final String INDENT = "  ";
    private static final String ERROR_MARKER = "^";
    private static final String ERROR_LABEL = "Error: ";
    private static final String GOODBYE = "Goodbye.";

    public static void main(String[] args) {
        run(new Terminal(System.in, System.out));
    }

    static void run(Terminal terminal) {
        terminal.println(BANNER);
        terminal.println(INTRO);
        while (true) {
            String sql = terminal.readLine(PROMPT);
            if (sql == null || sql.isBlank()) {
                terminal.println(GOODBYE);
                return;
            }
            terminal.println(describe(sql));
        }
    }

    // the tree, or the query with a ^ under the bad spot
    static String describe(String sql) {
        try {
            return AstPrinter.print(new Parser(new Lexer(sql).tokenize()).parse());
        } catch (LexicalException e) {
            return pointAt(sql, e.column(), e.getMessage());
        } catch (SyntaxException e) {
            return pointAt(sql, e.column(), e.getMessage());
        }
    }

    private static String pointAt(String sql, int column, String message) {
        return INDENT + sql + System.lineSeparator()
                + INDENT + " ".repeat(column - 1) + ERROR_MARKER + System.lineSeparator()
                + INDENT + ERROR_LABEL + message;
    }
}
