package com.avrosql.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * The console the menu talks to. Taking the streams as parameters lets tests
 * drive the menu with prepared input instead of a real keyboard.
 */
public final class Terminal {

    private static final String PRESS_ENTER = "Press Enter to return to the menu...";

    private final Scanner in;
    private final PrintStream out;

    public Terminal(InputStream in, PrintStream out) {
        this.in = new Scanner(in);
        this.out = out;
    }

    /** Prints the prompt and reads one line; returns null when the input has ended. */
    public String readLine(String prompt) {
        out.print(prompt);
        return in.hasNextLine() ? in.nextLine() : null;
    }

    public void waitForEnter() {
        out.println();
        readLine(PRESS_ENTER);
    }

    public void println(String text) {
        out.println(text);
    }

    public void println() {
        out.println();
    }

    public void printf(String format, Object... args) {
        out.printf(format, args);
    }
}
