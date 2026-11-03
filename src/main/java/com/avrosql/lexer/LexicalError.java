package com.avrosql.lexer;

import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.lexer.exception.LexicalRuntimeException;

// The checked and unchecked exceptions can't share a superclass, so they share this.
public interface LexicalError {

    char character();

    int line();

    int column();
}
