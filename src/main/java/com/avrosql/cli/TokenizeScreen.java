package com.avrosql.cli;

/** Reads SQL one line at a time and prints its tokens, until the user enters an empty line. */
final class TokenizeScreen implements Screen {

    private static final String TITLE = "Tokenize SQL";
    private static final String INTRO = "Type a SQL query on one line and press Enter.\nEnter an empty line to go back to the menu.";
    private static final String PROMPT = "sql> ";
    @Override
    public String title() {
        return TITLE;
    }

    @Override
    public void show(Terminal terminal) {
        terminal.println(INTRO);
        while (true) {
            String sql = terminal.readLine(PROMPT);
            if (sql == null || sql.isBlank() ) {
                return;
            }
            terminal.println(INTRO);
            TokenPrinter.print(terminal, sql);


        }
    }
}
