package com.avrosql.cli;

/** One option of the main menu. Adding an option means adding a Screen, not editing the menu loop. */
interface Screen {

    /** The text shown next to this option's number in the menu. */
    String title();

    /** Runs the screen; returning goes back to the menu. */
    void show(Terminal terminal);
}
