package com.avrosql.lexer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CursorTest {

    @Test
    void emptySourceIsImmediatelyAtEnd() {
        Cursor cursor = new Cursor("");

        assertTrue(cursor.isAtEnd());
        assertEquals(Cursor.END, cursor.peek());
    }

    @Test
    void peekDoesNotConsume() {
        Cursor cursor = new Cursor("ab");

        assertEquals('a', cursor.peek());
        assertEquals('a', cursor.peek());
        assertEquals(1, cursor.column());
    }

    @Test
    void advanceReturnsCurrentAndMovesForward() {
        Cursor cursor = new Cursor("ab");

        assertEquals('a', cursor.advance());
        assertEquals('b', cursor.peek());
        assertEquals('b', cursor.advance());
        assertTrue(cursor.isAtEnd());
        assertEquals(Cursor.END, cursor.peek());
    }

    @Test
    void peekLetsNumberStopWithoutEatingNextChar() {
        // The 42+1 example: reading digits must leave '+' for the next token.
        Cursor cursor = new Cursor("42+1");
        StringBuilder number = new StringBuilder();
        while (Character.isDigit(cursor.peek())) {
            number.append(cursor.advance());
        }

        assertEquals("42", number.toString());
        assertEquals('+', cursor.peek());
    }

    @Test
    void columnAdvancesOnSameLine() {
        Cursor cursor = new Cursor("abc");
        cursor.advance();
        cursor.advance();

        assertEquals(1, cursor.line());
        assertEquals(3, cursor.column());
    }

    @Test
    void newlineMovesToNextLineAndResetsColumn() {
        Cursor cursor = new Cursor("a\nb");
        cursor.advance(); // a
        cursor.advance(); // \n

        assertEquals(2, cursor.line());
        assertEquals(1, cursor.column());
        assertEquals('b', cursor.peek());
    }

    @Test
    void windowsLineEndingCountsAsOneLine() {
        Cursor cursor = new Cursor("a\r\nb");
        cursor.advance(); // a
        cursor.advance(); // \r
        cursor.advance(); // \n

        assertEquals(2, cursor.line());
        assertEquals(1, cursor.column());
    }

    @Test
    void peekNextLooksOneAheadWithoutConsuming() {
        Cursor cursor = new Cursor("ab");

        assertEquals('b', cursor.peekNext());
        assertEquals('a', cursor.peek());
        cursor.advance();
        assertEquals(Cursor.END, cursor.peekNext());
        cursor.advance();
        assertEquals(Cursor.END, cursor.peekNext());
    }

    @Test
    void advancePastEndIsAnError() {
        Cursor cursor = new Cursor("a");
        cursor.advance();

        assertThrows(IllegalStateException.class, cursor::advance);
    }
}
