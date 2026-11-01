package com.avrosql;

import com.avrosql.cli.MainMenu;
import com.avrosql.cli.Terminal;

public final class LexerDemo {

    public static void main(String[] args) {
        new MainMenu(new Terminal(System.in, System.out)).run();
    }
}
