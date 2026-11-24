package com.avrosql.cli;

interface Screen {

    String title();

    void show(Terminal terminal);
}
