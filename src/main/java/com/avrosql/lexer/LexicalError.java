package com.avrosql.lexer;

import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.lexer.exception.LexicalRuntimeException;

/**
 * What every lexical error reports: the offending character and where it is.
 * Shared by the checked {@link LexicalException} and the unchecked {@link LexicalRuntimeException},
 * which cannot share a superclass because one extends Exception and the other RuntimeException.
 */
public interface LexicalError {

    char character();

    int line();

    int column();
}
