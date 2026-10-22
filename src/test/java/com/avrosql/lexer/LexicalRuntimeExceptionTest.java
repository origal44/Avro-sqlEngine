package com.avrosql.lexer;

import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.lexer.exception.LexicalRuntimeException;
import com.avrosql.lexer.reader.OperatorReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LexicalRuntimeExceptionTest {

    @Test
    void wrapsTheCheckedExceptionAndReportsItsPosition() {
        LexicalException checked = new LexicalException('#', 2, 5);
        LexicalRuntimeException e = new LexicalRuntimeException(checked);

        assertSame(checked, e.getCause());
        assertEquals(checked.getMessage(), e.getMessage());
        assertEquals('#', e.character());
        assertEquals(2, e.line());
        assertEquals(5, e.column());
    }

    @Test
    void readerThrowsRuntimeFormAndLexerRethrowsCheckedForm() {
        // Inside the lexer: a reader called directly throws the unchecked exception.
        assertThrows(LexicalRuntimeException.class, () -> new OperatorReader().read(new Cursor("!")));

        // At the boundary: tokenize() hands callers the checked exception it wrapped.
        LexicalException e = assertThrows(LexicalException.class, () -> new Lexer("!").tokenize());
        assertEquals('!', e.character());
        assertEquals(1, e.column());
    }
}
