package com.avrosql.cli;

import java.util.List;

/**
 * Explains each lexical error. Every example is run through the real Lexer,
 * so the output shown is exactly what the user would see in "Tokenize SQL".
 */
final class ErrorGuideScreen implements Screen {

    /** One error the lexer can report, with a query that triggers it. */
    record ErrorExample(String name, String explanation, String fix, String sql) {
    }

    private static final String TITLE = "Error explanations";
    private static final String HEADING = "ERROR EXPLANATIONS";
    private static final String NAME_ROW = "%d. %s%n";
    private static final String FIX_LABEL = "How to fix: ";
    private static final String EXAMPLE_LABEL = "Example:";

    static final List<ErrorExample> EXAMPLES = List.of(
            new ErrorExample(
                    "Unexpected character",
                    "The character can't start any token, e.g. # @ $ ? or a double quote.",
                    "Remove it. Strings use single quotes ('Ori'), not double quotes.",
                    "SELECT name FROM users WHERE id # 5"),
            new ErrorExample(
                    "A '!' on its own",
                    "'!' only exists as part of '!=' (not equal). By itself it means nothing.",
                    "Write != or <> for \"not equal\", or the NOT keyword.",
                    "SELECT * FROM users WHERE age ! 18"),
            new ErrorExample(
                    "Unterminated string",
                    "A string was opened with ' but the line ended before the closing '.",
                    "Add the closing quote. For a quote inside the text, write it twice: 'it''s'.",
                    "SELECT * FROM users WHERE name = 'Ori"),
            new ErrorExample(
                    "Number ending in a dot",
                    "A decimal point must have at least one digit after it.",
                    "Write 3 or 3.0 instead of 3.",
                    "SELECT * FROM users LIMIT 3."));

    @Override
    public String title() {
        return TITLE;
    }

    @Override
    public void show(Terminal terminal) {
        terminal.println(HEADING);
        for (int i = 0; i < EXAMPLES.size(); i++) {
            ErrorExample example = EXAMPLES.get(i);
            terminal.println();
            terminal.printf(NAME_ROW, i + 1, example.name());
            terminal.println(TokenPrinter.INDENT + example.explanation());
            terminal.println(TokenPrinter.INDENT + FIX_LABEL + example.fix());
            terminal.println(TokenPrinter.INDENT + EXAMPLE_LABEL);
            TokenPrinter.print(terminal, example.sql());
        }
        terminal.waitForEnter();
    }
}
