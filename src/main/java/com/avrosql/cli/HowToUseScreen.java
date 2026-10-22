package com.avrosql.cli;

/** Explains to a new user what the lexer does, how to use the demo, and how to read its output. */
final class HowToUseScreen implements Screen {

    private static final String TITLE = "How to use";
    private static final String INSTRUCTIONS = """
            HOW TO USE

            
            Trying it
              1. Choose "Tokenize SQL" from the menu.
              2. Type a query on one line and press Enter, for example:
                   SELECT name, age FROM users WHERE age >= 18
              3. Keep typing queries. Enter an empty line to go back to the menu.

            Reading the output
              LINE:COL  where the token starts (both count from 1)
              TYPE      what kind of token it is
              TEXT      the exact characters from your query
              The last row is always EOF, which marks the end of the input.""";


    @Override
    public String title() {
        return TITLE;
    }

    @Override
    public void show(Terminal terminal) {
        terminal.println(INSTRUCTIONS);
        terminal.waitForEnter();
    }
}
