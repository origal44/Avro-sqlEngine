package com.avrosql.cli;

/** Explains to a new user what the lexer does, how to use the demo, and how to read its output. */
final class HowToUseScreen implements Screen {

    private static final String TITLE = "How to use";
    private static final String INSTRUCTIONS = """
            HOW TO USE

            What this does
              The lexer is the first stage of the SQL engine. It splits a SQL query into
              tokens: keywords, names, numbers, strings and symbols. It does not check
              whether the query makes sense; "FROM SELECT )" is tokenized without complaint.
              Checking the order of tokens is the Parser's job.

            Trying it
              1. Choose "Tokenize SQL" from the menu.
              2. Type a query on one line and press Enter, for example:
                   SELECT name, age FROM users WHERE age >= 18
              3. Keep typing queries. Enter an empty line to go back to the menu.

            Reading the output
              LINE:COL  where the token starts (both count from 1)
              TYPE      what kind of token it is (see "Token reference" in the menu)
              TEXT      the exact characters from your query
              The last row is always EOF, which marks the end of the input.

            Good to know
              - Keywords ignore case: select, Select and SELECT are all SELECT.
                The TEXT column still shows what you typed.
              - Names keep their case, because Avro field names are case-sensitive.
              - Strings use single quotes: 'Ori'. Write '' for a quote inside: 'it''s'.
              - COUNT, SUM, AVG, MIN and MAX are names (IDENTIFIER), not keywords.
              - If a character can't be read, the demo shows your query with a ^ under
                the problem. "Error explanations" in the menu lists each error.""";

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
