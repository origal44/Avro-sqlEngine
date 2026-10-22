package com.avrosql.cli;

import java.util.List;

/** The numbered main menu: shows the options, runs the chosen screen, and comes back until the user exits. */
public final class MainMenu {

    private static final int EXIT_CHOICE = 0;
    private static final String BANNER = "=== Avro SQL Lexer ===";
    private static final String NEW_USER_HINT = "New here? Choose %d for instructions.";
    private static final String OPTION_ROW = "%d. %s%n";
    private static final String EXIT_TITLE = "Exit";
    private static final String CHOOSE = "Choose an option: ";
    private static final String INVALID_CHOICE = "'%s' is not an option. Enter a number from the menu.";
    private static final String GOODBYE = "Goodbye.";

    private final Terminal terminal;
    private final List<Screen> screens;

    public MainMenu(Terminal terminal) {
        this(terminal, List.of(
                new TokenizeScreen(),
                new HowToUseScreen()));
    }

    MainMenu(Terminal terminal, List<Screen> screens) {
        this.terminal = terminal;
        this.screens = List.copyOf(screens);
    }

    public void run() {
        terminal.println(BANNER);
        int howToUse = numberOf(HowToUseScreen.class);
        if (howToUse != EXIT_CHOICE) {
            terminal.println(NEW_USER_HINT.formatted(howToUse));
        }

        while (true) {
            printOptions();
            String choice = terminal.readLine(CHOOSE);
            if (choice == null || choice.trim().equals(String.valueOf(EXIT_CHOICE))) {
                terminal.println(GOODBYE);
                return;
            }
            terminal.println();
            Screen screen = screenFor(choice.trim());
            if (screen == null) {
                terminal.println(INVALID_CHOICE.formatted(choice.trim()));
            } else {
                screen.show(terminal);
            }
        }
    }

    private void printOptions() {
        terminal.println();
        for (int i = 0; i < screens.size(); i++) {
            terminal.printf(OPTION_ROW, i + 1, screens.get(i).title());
        }
        terminal.printf(OPTION_ROW, EXIT_CHOICE, EXIT_TITLE);
    }

    /** The screen for a menu number, or null if the text is not a valid option. */
    private Screen screenFor(String choice) {
        try {
            int number = Integer.parseInt(choice);
            return number >= 1 && number <= screens.size() ? screens.get(number - 1) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** The menu number of the first screen of this type, or EXIT_CHOICE if there is none. */
    private int numberOf(Class<? extends Screen> type) {
        for (int i = 0; i < screens.size(); i++) {
            if (type.isInstance(screens.get(i))) {
                return i + 1;
            }
        }
        return EXIT_CHOICE;
    }
}
