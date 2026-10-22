package com.avrosql;

import com.avrosql.cli.MainMenu;
import com.avrosql.cli.Terminal;

/**
 * Interactive demo: opens the main menu, where the user can tokenize SQL
 * or read the instructions, token reference and error explanations.
 */
public final class LexerDemo {

    public static void main(String[] args) {
        new MainMenu(new Terminal(System.in, System.out)).run();
    }
}
