package com.avrosql.exception;

import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.parser.Parser;
import com.avrosql.parser.exception.SyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ErrorCodeTest {

    private static QueryException failure(String sql) {
        return assertThrows(QueryException.class, () -> new Parser(new Lexer(sql).tokenize()).parse());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "SELECT # FROM t;                          | UNEXPECTED_CHARACTER",
            "SELECT 'abc FROM t;                       | UNTERMINATED_STRING",
            "SELECT a t;                               | UNEXPECTED_TOKEN",
            "SELECT FROM t;                            | EXPECTED_EXPRESSION",
            "SELECT a FROM t LIMIT 2.5;                | EXPECTED_INTEGER",
            "SELECT 99999999999999999999 FROM t;       | NUMBER_TOO_LARGE",
            "SELECT a FROM t WHERE a < b < c;          | CHAINED_COMPARISON",
            "SELECT a FROM t WHERE a NOT = 1;          | EXPECTED_AFTER_NOT",
            "SELECT LENGTH(a) FROM t;                  | UNKNOWN_FUNCTION"})
    void everyLexerAndParserErrorHasACode(String sql, ErrorCode code) {
        assertEquals(code, failure(sql).code());
    }

    @Test
    void lexerAndParserErrorsShareTheBaseClass() {
        QueryException lexical = failure("SELECT # FROM t;");
        assertInstanceOf(LexicalException.class, lexical);
        assertEquals(1, lexical.line());
        assertEquals(8, lexical.column());

        QueryException syntax = failure("SELECT a FROM t\nWHERE ;");
        assertInstanceOf(SyntaxException.class, syntax);
        assertEquals(2, syntax.line());
        assertEquals(7, syntax.column());
    }
}
