package com.avrosql.lexer.exception;

import com.avrosql.exception.ErrorCode;
import com.avrosql.exception.QueryException;
import com.avrosql.lexer.LexicalError;

public final class LexicalException extends QueryException implements LexicalError {

    private static final String UNEXPECTED_CHARACTER = "Unexpected character '%c' at line %d, column %d";

    private final char character;

    public LexicalException(char character, int line, int column) {
        this(ErrorCode.UNEXPECTED_CHARACTER, UNEXPECTED_CHARACTER.formatted(character, line, column),
                character, line, column);
    }

    public LexicalException(ErrorCode code, String message, char character, int line, int column) {
        super(code, message, line, column);
        this.character = character;
    }

    @Override
    public char character() {
        return character;
    }
}
