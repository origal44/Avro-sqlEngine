package com.avrosql;

import com.avrosql.lexer.LexicalException;
import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.Token;

import java.util.Scanner;

/**
 * Interactive demo: reads one SQL line at a time and prints the tokens the Lexer produces.
 * Type "exit" (or an empty line) to quit.
 */
public final class LexerDemo {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter SQL (empty line or 'exit' to quit):");

        while (true) {
            System.out.print("sql> ");
            if (!in.hasNextLine()) {
                break;
            }
            String sql = in.nextLine();
            if (sql.isBlank() || sql.trim().equalsIgnoreCase("exit")) {
                break;
            }

            try {
                for (Token token : new Lexer(sql).tokenize()) {
                    System.out.printf("  %d:%-4d %-14s %s%n",
                            token.line(), token.column(), token.type(), token.text());
                }
            } catch (LexicalException e) {
                System.out.println("  " + sql);
                System.out.println("  " + " ".repeat(e.column() - 1) + "^");
                System.out.println("  Error: " + e.getMessage());
            }
        }
    }
}
