package com.avrosql.parser;

import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.parser.ast.Aggregate;
import com.avrosql.parser.ast.Between;
import com.avrosql.parser.ast.Binary;
import com.avrosql.parser.ast.Expr;
import com.avrosql.parser.ast.In;
import com.avrosql.parser.ast.IsNull;
import com.avrosql.parser.ast.Position;
import com.avrosql.parser.ast.Unary;
import com.avrosql.parser.exception.SyntaxException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParsePositionTest {

    private static Expr parse(String source) throws LexicalException {
        return new Parser(new Lexer(source).tokenize()).parseExpression();
    }

    private static void assertAt(int line, int column, Expr expr) {
        assertEquals(new Position(line, column), expr.position());
    }

    @Test
    void binaryPointsAtItsOperatorAndLeavesAtTheirTokens() throws LexicalException {
        Binary b = (Binary) parse("age > 'abc'");
        assertAt(1, 5, b);
        assertAt(1, 1, b.left());
        assertAt(1, 7, b.right());
    }

    @Test
    void positionsCountLinesInAWholeStatement() throws LexicalException, SyntaxException {
        Expr where = new Parser(new Lexer("SELECT name\nFROM users\nWHERE city = 5;").tokenize())
                .parse().where().orElseThrow();
        Binary b = (Binary) where;
        assertAt(3, 12, b);
        assertAt(3, 7, b.left());
        assertAt(3, 14, b.right());
    }

    @Test
    void parenthesesDoNotMoveTheInnerOperator() throws LexicalException {
        assertAt(1, 4, parse("(a + b)"));
    }

    @Test
    void prefixOperatorsPointAtThemselves() throws LexicalException {
        assertAt(1, 1, parse("-x"));
        Unary not = (Unary) parse("NOT a");
        assertAt(1, 1, not);
        assertAt(1, 5, not.operand());
    }

    @Test
    void isNullPointsAtIsAndNotAtNot() throws LexicalException {
        assertAt(1, 3, parse("a IS NULL"));
        Unary not = (Unary) parse("a IS NOT NULL");
        assertAt(1, 6, not);
        assertAt(1, 3, (IsNull) not.operand());
    }

    @Test
    void betweenAndInPointAtTheirKeyword() throws LexicalException {
        assertAt(1, 3, (Between) parse("a BETWEEN 1 AND 2"));
        assertAt(1, 3, (In) parse("a IN (1, 2)"));
    }

    @Test
    void infixNotPointsAtNotAndItsOperandAtTheKeyword() throws LexicalException {
        Unary notBetween = (Unary) parse("a NOT BETWEEN 1 AND 2");
        assertAt(1, 3, notBetween);
        assertAt(1, 7, notBetween.operand());

        Unary notIn = (Unary) parse("a NOT IN (1)");
        assertAt(1, 3, notIn);
        assertAt(1, 7, notIn.operand());

        Unary notLike = (Unary) parse("a NOT LIKE 'x%'");
        assertAt(1, 3, notLike);
        assertAt(1, 7, notLike.operand());
    }

    @Test
    void aggregatePointsAtTheFunctionName() throws LexicalException {
        assertAt(1, 1, parse("COUNT(*)"));
        Aggregate sum = (Aggregate) ((Binary) parse("x + SUM(price)")).right();
        assertAt(1, 5, sum);
        assertAt(1, 9, sum.argument().orElseThrow());
    }
}
