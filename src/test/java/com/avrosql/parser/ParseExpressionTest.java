package com.avrosql.parser;

import com.avrosql.lexer.Lexer;
import com.avrosql.lexer.exception.LexicalException;
import com.avrosql.lexer.token.TokenType;
import com.avrosql.parser.ast.Aggregate;
import com.avrosql.parser.ast.Between;
import com.avrosql.parser.ast.Binary;
import com.avrosql.parser.ast.ColumnRef;
import com.avrosql.parser.ast.Expr;
import com.avrosql.parser.ast.In;
import com.avrosql.parser.ast.IsNull;
import com.avrosql.parser.ast.Literal;
import com.avrosql.parser.ast.Unary;
import com.avrosql.parser.exception.SyntaxException;
import com.avrosql.parser.exception.SyntaxRuntimeException;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.avrosql.lexer.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

class ParseExpressionTest {

    private static Expr parse(String source) throws LexicalException, SyntaxException {
        Parser parser = new Parser(new Lexer(source).tokenize());
        try {
            Expr expr = parser.parseExpression();
            parser.expect(EOF);
            return expr;
        } catch (SyntaxRuntimeException e) {
            throw e.getCause();
        }
    }

    private static SyntaxException assertError(String source, String message) {
        SyntaxException e = assertThrows(SyntaxException.class, () -> parse(source));
        assertEquals(message, e.getMessage());
        return e;
    }

    private static Expr num(long value) {
        return new Literal(value);
    }

    private static Expr col(String name) {
        return new ColumnRef(name);
    }

    private static Expr bin(Expr left, TokenType op, Expr right) {
        return new Binary(left, op, right);
    }

    private static Expr agg(Aggregate.Function function, Expr argument) {
        return new Aggregate(function, Optional.of(argument));
    }

    private static Expr countStar() {
        return new Aggregate(Aggregate.Function.COUNT, Optional.empty());
    }

    private static Expr between(Expr value, Expr low, Expr high) {
        return new Between(value, low, high);
    }

    private static Expr in(Expr value, Expr... values) {
        return new In(value, List.of(values));
    }

    private static Expr isNull(Expr operand) {
        return new IsNull(operand);
    }

    private static Expr un(TokenType op, Expr operand) {
        return new Unary(op, operand);
    }

    @Test
    void integerBecomesLong() throws Exception {
        assertEquals(num(42), parse("42"));
    }

    @Test
    void decimalBecomesDouble() throws Exception {
        assertEquals(new Literal(3.5), parse("3.5"));
    }

    @Test
    void largestLongIsAccepted() throws Exception {
        assertEquals(num(Long.MAX_VALUE), parse("9223372036854775807"));
    }

    @Test
    void stringLosesQuotesAndUnescapes() throws Exception {
        assertEquals(new Literal("Haifa"), parse("'Haifa'"));
        assertEquals(new Literal("it's"), parse("'it''s'"));
        assertEquals(new Literal(""), parse("''"));
    }

    @Test
    void column() throws Exception {
        assertEquals(col("age"), parse("age"));
    }

    @Test
    void multiplicationBindsTighterThanAddition() throws Exception {
        assertEquals(bin(num(1), PLUS, bin(num(2), STAR, num(3))), parse("1 + 2 * 3"));
        assertEquals(bin(bin(num(2), STAR, num(3)), PLUS, num(1)), parse("2 * 3 + 1"));
    }

    @Test
    void subtractionIsLeftAssociative() throws Exception {
        assertEquals(bin(bin(num(10), MINUS, num(4)), MINUS, num(3)), parse("10 - 4 - 3"));
    }

    @Test
    void divisionAndModuloShareALevelAndGroupLeft() throws Exception {
        assertEquals(bin(bin(num(8), SLASH, num(4)), PERCENT, num(3)), parse("8 / 4 % 3"));
        assertEquals(bin(bin(num(8), PERCENT, num(4)), SLASH, num(3)), parse("8 % 4 / 3"));
    }

    @Test
    void mixedColumnsAndLiterals() throws Exception {
        assertEquals(bin(bin(col("price"), STAR, col("qty")), MINUS, bin(col("discount"), SLASH, num(2))),
                parse("price * qty - discount / 2"));
    }

    @Test
    void parenthesesOverridePrecedence() throws Exception {
        assertEquals(bin(bin(num(1), PLUS, num(2)), STAR, num(3)), parse("(1 + 2) * 3"));
        assertEquals(bin(num(10), MINUS, bin(num(4), MINUS, num(3))), parse("10 - (4 - 3)"));
    }

    @Test
    void parenthesesLeaveNoNodeOfTheirOwn() throws Exception {
        assertEquals(col("a"), parse("((a))"));
    }

    @Test
    void numberTooLargeForLong() {
        SyntaxException e = assertError("9223372036854775808",
                "Number too large: '9223372036854775808' at line 1, column 1");
        assertNull(e.expected());
        assertEquals(NUMBER, e.found().type());
    }

    @Test
    void numberTooLargeInsideExpression() {
        assertError("1 + 99999999999999999999", "Number too large: '99999999999999999999' at line 1, column 5");
    }

    @Test
    void decimalTooLargeForDouble() {
        String huge = "9".repeat(400) + ".5";
        assertError(huge, "Number too large: '" + huge + "' at line 1, column 1");
    }

    @Test
    void missingRightOperand() {
        SyntaxException e = assertError("1 +", "Expected an expression but found end of input (EOF) at line 1, column 4");
        assertNull(e.expected());
    }

    @Test
    void operatorWhereOperandShouldBe() {
        assertError("1 + )", "Expected an expression but found ')' (RIGHT_PAREN) at line 1, column 5");
        assertError("* 2", "Expected an expression but found '*' (STAR) at line 1, column 1");
    }

    @Test
    void emptyParentheses() {
        assertError("()", "Expected an expression but found ')' (RIGHT_PAREN) at line 1, column 2");
    }

    @Test
    void unclosedParenthesis() {
        SyntaxException e = assertError("(1 + 2", "Expected RIGHT_PAREN but found end of input (EOF) at line 1, column 7");
        assertEquals(RIGHT_PAREN, e.expected());
    }

    @Test
    void extraClosingParenthesisStopsTheExpression() {
        // ')' isn't an operator, so the loop stops there and the caller sees it
        assertError("1 + 2)", "Expected EOF but found ')' (RIGHT_PAREN) at line 1, column 6");
    }

    @Test
    void twoOperandsWithoutOperator() {
        assertError("1 2", "Expected EOF but found '2' (NUMBER) at line 1, column 3");
    }

    @Test
    void unaryMinusBindsTighterThanProduct() throws Exception {
        assertEquals(bin(un(MINUS, col("a")), STAR, col("b")), parse("-a * b"));
        assertEquals(bin(un(MINUS, col("a")), PLUS, col("b")), parse("-a + b"));
    }

    @Test
    void unaryMinusOnTheRightSide() throws Exception {
        assertEquals(bin(num(2), STAR, un(MINUS, num(3))), parse("2 * -3"));
        assertEquals(bin(col("a"), MINUS, un(MINUS, col("b"))), parse("a - -b"));
    }

    @Test
    void unaryMinusNestsAndTakesParentheses() throws Exception {
        assertEquals(un(MINUS, un(MINUS, num(5))), parse("- -5"));
        assertEquals(un(MINUS, bin(col("a"), PLUS, col("b"))), parse("-(a + b)"));
    }

    @Test
    void everyComparisonOperator() throws Exception {
        assertEquals(bin(col("a"), EQUAL, num(1)), parse("a = 1"));
        assertEquals(bin(col("a"), NOT_EQUAL, num(1)), parse("a <> 1"));
        assertEquals(bin(col("a"), NOT_EQUAL, num(1)), parse("a != 1"));
        assertEquals(bin(col("a"), LESS, num(1)), parse("a < 1"));
        assertEquals(bin(col("a"), LESS_EQUAL, num(1)), parse("a <= 1"));
        assertEquals(bin(col("a"), GREATER, num(1)), parse("a > 1"));
        assertEquals(bin(col("a"), GREATER_EQUAL, num(1)), parse("a >= 1"));
    }

    @Test
    void arithmeticBindsTighterThanComparison() throws Exception {
        assertEquals(bin(bin(col("age"), PLUS, num(1)), GREATER, bin(col("b"), STAR, num(2))),
                parse("age + 1 > b * 2"));
    }

    @Test
    void andBindsTighterThanOr() throws Exception {
        Expr a = bin(col("a"), EQUAL, num(1));
        Expr b = bin(col("b"), EQUAL, num(2));
        Expr c = bin(col("c"), EQUAL, num(3));
        assertEquals(bin(a, OR, bin(b, AND, c)), parse("a = 1 OR b = 2 AND c = 3"));
        assertEquals(bin(bin(a, AND, b), OR, c), parse("a = 1 AND b = 2 OR c = 3"));
    }

    @Test
    void andAndOrGroupLeft() throws Exception {
        assertEquals(bin(bin(col("a"), OR, col("b")), OR, col("c")), parse("a OR b OR c"));
        assertEquals(bin(bin(col("a"), AND, col("b")), AND, col("c")), parse("a AND b AND c"));
    }

    @Test
    void notTakesTheWholeComparison() throws Exception {
        assertEquals(un(NOT, bin(col("a"), EQUAL, num(1))), parse("NOT a = 1"));
    }

    @Test
    void notStopsAtAnd() throws Exception {
        assertEquals(bin(un(NOT, bin(col("a"), EQUAL, num(1))), AND, bin(col("b"), EQUAL, num(2))),
                parse("NOT a = 1 AND b = 2"));
    }

    @Test
    void notNestsAndTakesParentheses() throws Exception {
        assertEquals(un(NOT, un(NOT, col("a"))), parse("NOT NOT a"));
        assertEquals(un(NOT, bin(col("a"), OR, col("b"))), parse("NOT (a OR b)"));
        assertEquals(bin(bin(col("a"), OR, col("b")), AND, col("c")), parse("(a OR b) AND c"));
    }

    @Test
    void chainedComparisonIsRejected() {
        SyntaxException e = assertError("a < b < c", "Comparisons can't be chained: found '<' (LESS) at line 1, column 7");
        assertNull(e.expected());
        assertError("a = b = c", "Comparisons can't be chained: found '=' (EQUAL) at line 1, column 7");
        assertError("a < b + 1 < c", "Comparisons can't be chained: found '<' (LESS) at line 1, column 11");
        assertError("NOT a = 1 <> b", "Comparisons can't be chained: found '<>' (NOT_EQUAL) at line 1, column 11");
    }

    @Test
    void comparisonsJoinedByAndAreFine() throws Exception {
        assertEquals(bin(bin(col("a"), LESS, col("b")), AND, bin(col("b"), LESS, col("c"))),
                parse("a < b AND b < c"));
    }

    @Test
    void parenthesizedComparisonIsLeftForTypeChecking() throws Exception {
        // syntactically fine; comparing a boolean with c is a type error, not a syntax error
        assertEquals(bin(bin(col("a"), LESS, col("b")), LESS, col("c")), parse("(a < b) < c"));
    }

    @Test
    void prefixOperatorWithoutOperand() {
        assertError("-", "Expected an expression but found end of input (EOF) at line 1, column 2");
        assertError("NOT", "Expected an expression but found end of input (EOF) at line 1, column 4");
    }

    @Test
    void infixOperatorWithoutRightSide() {
        assertError("a AND", "Expected an expression but found end of input (EOF) at line 1, column 6");
        assertError("a =", "Expected an expression but found end of input (EOF) at line 1, column 4");
    }

    @Test
    void isNull() throws Exception {
        assertEquals(isNull(col("email")), parse("email IS NULL"));
    }

    @Test
    void isNotNullIsNotAroundIsNull() throws Exception {
        assertEquals(un(NOT, isNull(col("email"))), parse("email IS NOT NULL"));
        assertEquals(parse("NOT email IS NULL"), parse("email IS NOT NULL"));
    }

    @Test
    void isNullAppliesToTheWholeArithmetic() throws Exception {
        assertEquals(isNull(bin(col("a"), PLUS, num(1))), parse("a + 1 IS NULL"));
        assertEquals(isNull(un(MINUS, col("a"))), parse("-a IS NULL"));
    }

    @Test
    void isNullBindsTighterThanAndOr() throws Exception {
        assertEquals(bin(isNull(col("a")), AND, bin(col("b"), EQUAL, num(1))), parse("a IS NULL AND b = 1"));
        assertEquals(bin(isNull(col("a")), OR, un(NOT, isNull(col("b")))), parse("a IS NULL OR b IS NOT NULL"));
    }

    @Test
    void isWithoutNull() {
        SyntaxException e = assertError("a IS", "Expected NULL but found end of input (EOF) at line 1, column 5");
        assertEquals(NULL, e.expected());
        assertError("a IS 5", "Expected NULL but found '5' (NUMBER) at line 1, column 6");
        assertError("a IS NOT", "Expected NULL but found end of input (EOF) at line 1, column 9");
        assertError("a IS NOT 5", "Expected NULL but found '5' (NUMBER) at line 1, column 10");
    }

    @Test
    void isNullCannotChainWithComparisons() {
        assertError("a IS NULL IS NULL", "Comparisons can't be chained: found 'IS' (IS) at line 1, column 11");
        assertError("a = 1 IS NULL", "Comparisons can't be chained: found 'IS' (IS) at line 1, column 7");
    }

    @Test
    void isWithoutLeftSide() {
        assertError("IS NULL", "Expected an expression but found 'IS' (IS) at line 1, column 1");
    }

    @Test
    void nullWithoutIs() {
        assertError("a NULL", "Expected EOF but found 'NULL' (NULL) at line 1, column 3");
        // NOT after a left side must start NOT IN or NOT LIKE
        assertError("a NOT NULL", "Expected IN, LIKE or BETWEEN but found 'NULL' (NULL) at line 1, column 7");
    }

    @Test
    void between() throws Exception {
        assertEquals(between(col("a"), num(1), num(5)), parse("a BETWEEN 1 AND 5"));
    }

    @Test
    void betweenFollowedByLogicalAnd() throws Exception {
        assertEquals(bin(between(col("a"), num(1), num(5)), AND, bin(col("b"), EQUAL, num(2))),
                parse("a BETWEEN 1 AND 5 AND b = 2"));
    }

    @Test
    void betweenAfterLogicalAnd() throws Exception {
        assertEquals(bin(bin(col("b"), EQUAL, num(2)), AND, between(col("a"), num(1), num(5))),
                parse("b = 2 AND a BETWEEN 1 AND 5"));
    }

    @Test
    void betweenBoundsTakeArithmetic() throws Exception {
        assertEquals(between(col("a"), bin(col("x"), PLUS, num(1)), bin(col("y"), STAR, num(2))),
                parse("a BETWEEN x + 1 AND y * 2"));
        assertEquals(between(col("a"), un(MINUS, num(5)), num(5)), parse("a BETWEEN -5 AND 5"));
    }

    @Test
    void betweenValueTakesArithmetic() throws Exception {
        assertEquals(between(bin(col("a"), PLUS, num(1)), num(1), num(5)), parse("a + 1 BETWEEN 1 AND 5"));
    }

    @Test
    void notAndOrAroundBetween() throws Exception {
        assertEquals(un(NOT, between(col("a"), num(1), num(5))), parse("NOT a BETWEEN 1 AND 5"));
        assertEquals(bin(between(col("a"), num(1), num(5)), OR, col("c")), parse("a BETWEEN 1 AND 5 OR c"));
    }

    @Test
    void betweenMissingAnd() {
        SyntaxException e = assertError("a BETWEEN 1", "Expected AND but found end of input (EOF) at line 1, column 12");
        assertEquals(AND, e.expected());
        assertError("a BETWEEN 1 OR 5", "Expected AND but found 'OR' (OR) at line 1, column 13");
    }

    @Test
    void betweenBoundStopsAtComparison() {
        // the lower bound is just b, so the separator AND is missing
        assertError("a BETWEEN b = 1 AND 5", "Expected AND but found '=' (EQUAL) at line 1, column 13");
    }

    @Test
    void betweenMissingBound() {
        assertError("a BETWEEN AND 5", "Expected an expression but found 'AND' (AND) at line 1, column 11");
        assertError("a BETWEEN 1 AND", "Expected an expression but found end of input (EOF) at line 1, column 16");
    }

    @Test
    void betweenCannotChain() {
        assertError("a BETWEEN 1 AND 5 BETWEEN 2 AND 3",
                "Comparisons can't be chained: found 'BETWEEN' (BETWEEN) at line 1, column 19");
        assertError("a BETWEEN 1 AND 5 = b", "Comparisons can't be chained: found '=' (EQUAL) at line 1, column 19");
    }

    @Test
    void inList() throws Exception {
        assertEquals(in(col("a"), num(1), num(2), num(3)), parse("a IN (1, 2, 3)"));
        assertEquals(in(col("a"), num(1)), parse("a IN (1)"));
        assertEquals(in(col("city"), new Literal("Haifa"), new Literal("Tel Aviv")), parse("city IN ('Haifa', 'Tel Aviv')"));
    }

    @Test
    void inItemsAreAnyExpression() throws Exception {
        assertEquals(in(col("a"), un(MINUS, num(5)), col("b"), bin(col("c"), PLUS, num(1))), parse("a IN (-5, b, c + 1)"));
    }

    @Test
    void notIn() throws Exception {
        assertEquals(un(NOT, in(col("a"), num(1), num(2))), parse("a NOT IN (1, 2)"));
        assertEquals(parse("NOT a IN (1, 2)"), parse("a NOT IN (1, 2)"));
    }

    @Test
    void inWithArithmeticAndLogic() throws Exception {
        assertEquals(in(bin(col("a"), PLUS, num(1)), num(2)), parse("a + 1 IN (2)"));
        assertEquals(bin(in(col("a"), num(1), num(2)), AND, bin(col("b"), EQUAL, num(3))), parse("a IN (1, 2) AND b = 3"));
        assertEquals(bin(un(NOT, in(col("a"), num(1))), OR, col("c")), parse("a NOT IN (1) OR c"));
    }

    @Test
    void emptyInListIsRejected() {
        assertError("a IN ()", "Expected an expression but found ')' (RIGHT_PAREN) at line 1, column 7");
    }

    @Test
    void inNeedsParentheses() {
        SyntaxException e = assertError("a IN 1, 2", "Expected LEFT_PAREN but found '1' (NUMBER) at line 1, column 6");
        assertEquals(LEFT_PAREN, e.expected());
        assertError("a IN (1, 2", "Expected RIGHT_PAREN but found end of input (EOF) at line 1, column 11");
    }

    @Test
    void badInListSeparators() {
        assertError("a IN (1,)", "Expected an expression but found ')' (RIGHT_PAREN) at line 1, column 9");
        assertError("a IN (1 2)", "Expected RIGHT_PAREN but found '2' (NUMBER) at line 1, column 9");
    }

    @Test
    void infixNotMustBeFollowedByInLikeOrBetween() {
        SyntaxException e = assertError("a NOT (1)",
                "Expected IN, LIKE or BETWEEN but found '(' (LEFT_PAREN) at line 1, column 7");
        assertNull(e.expected());
        assertError("a NOT", "Expected IN, LIKE or BETWEEN but found end of input (EOF) at line 1, column 6");
        assertError("a NOT 5", "Expected IN, LIKE or BETWEEN but found '5' (NUMBER) at line 1, column 7");
    }

    @Test
    void notBetween() throws Exception {
        assertEquals(un(NOT, between(col("a"), num(1), num(5))), parse("a NOT BETWEEN 1 AND 5"));
        assertEquals(parse("NOT a BETWEEN 1 AND 5"), parse("a NOT BETWEEN 1 AND 5"));
    }

    @Test
    void notBetweenWithLogic() throws Exception {
        assertEquals(bin(un(NOT, between(col("a"), num(1), num(5))), AND, bin(col("b"), EQUAL, num(2))),
                parse("a NOT BETWEEN 1 AND 5 AND b = 2"));
    }

    @Test
    void notBetweenErrors() {
        assertError("a NOT BETWEEN 1 OR 5", "Expected AND but found 'OR' (OR) at line 1, column 17");
        assertError("a NOT BETWEEN 1 AND 5 < 3", "Comparisons can't be chained: found '<' (LESS) at line 1, column 23");
    }

    @Test
    void inCannotChain() {
        assertError("a IN (1) IN (2)", "Comparisons can't be chained: found 'IN' (IN) at line 1, column 10");
        assertError("a = 1 NOT IN (1)", "Comparisons can't be chained: found 'NOT' (NOT) at line 1, column 7");
    }

    @Test
    void like() throws Exception {
        assertEquals(bin(col("name"), LIKE, new Literal("A%")), parse("name LIKE 'A%'"));
    }

    @Test
    void likePatternIsAnyExpression() throws Exception {
        assertEquals(bin(col("name"), LIKE, col("pattern")), parse("name LIKE pattern"));
        assertEquals(bin(col("name"), LIKE, bin(new Literal("A"), PLUS, new Literal("%"))), parse("name LIKE 'A' + '%'"));
    }

    @Test
    void notLike() throws Exception {
        assertEquals(un(NOT, bin(col("name"), LIKE, new Literal("A%"))), parse("name NOT LIKE 'A%'"));
        assertEquals(parse("NOT name LIKE 'A%'"), parse("name NOT LIKE 'A%'"));
    }

    @Test
    void likeWithLogic() throws Exception {
        assertEquals(bin(bin(col("name"), LIKE, new Literal("A%")), AND, bin(col("age"), GREATER, num(3))),
                parse("name LIKE 'A%' AND age > 3"));
        assertEquals(bin(un(NOT, bin(col("a"), LIKE, new Literal("x"))), OR, col("b")), parse("a NOT LIKE 'x' OR b"));
    }

    @Test
    void likeMissingPattern() {
        assertError("name LIKE", "Expected an expression but found end of input (EOF) at line 1, column 10");
        assertError("name NOT LIKE", "Expected an expression but found end of input (EOF) at line 1, column 14");
    }

    @Test
    void likeCannotChain() {
        assertError("a LIKE 'x' LIKE 'y'", "Comparisons can't be chained: found 'LIKE' (LIKE) at line 1, column 12");
        assertError("a NOT LIKE 'x' = b", "Comparisons can't be chained: found '=' (EQUAL) at line 1, column 16");
    }

    @Test
    void countOfStar() throws Exception {
        assertEquals(countStar(), parse("COUNT(*)"));
    }

    @Test
    void everyAggregateFunction() throws Exception {
        assertEquals(agg(Aggregate.Function.COUNT, col("x")), parse("COUNT(x)"));
        assertEquals(agg(Aggregate.Function.SUM, col("price")), parse("SUM(price)"));
        assertEquals(agg(Aggregate.Function.AVG, col("age")), parse("AVG(age)"));
        assertEquals(agg(Aggregate.Function.MIN, col("age")), parse("MIN(age)"));
        assertEquals(agg(Aggregate.Function.MAX, col("age")), parse("MAX(age)"));
    }

    @Test
    void functionNamesIgnoreCase() throws Exception {
        assertEquals(countStar(), parse("count(*)"));
        assertEquals(agg(Aggregate.Function.SUM, col("x")), parse("Sum(x)"));
    }

    @Test
    void aggregateArgumentIsAnyExpression() throws Exception {
        assertEquals(agg(Aggregate.Function.SUM, bin(col("price"), STAR, col("qty"))), parse("SUM(price * qty)"));
    }

    @Test
    void aggregatesInsideLargerExpressions() throws Exception {
        assertEquals(bin(agg(Aggregate.Function.MAX, col("a")), MINUS, agg(Aggregate.Function.MIN, col("a"))),
                parse("MAX(a) - MIN(a)"));
        assertEquals(bin(countStar(), GREATER, num(5)), parse("COUNT(*) > 5"));
    }

    @Test
    void nameWithoutParenthesisIsAColumn() throws Exception {
        assertEquals(col("count"), parse("count"));
        assertEquals(bin(col("count"), PLUS, num(1)), parse("count + 1"));
    }

    @Test
    void functionNameWithoutParenthesis() {
        assertError("COUNT x", "Expected EOF but found 'x' (IDENTIFIER) at line 1, column 7");
    }

    @Test
    void missingArgument() {
        assertError("COUNT()", "Expected an expression but found ')' (RIGHT_PAREN) at line 1, column 7");
    }

    @Test
    void onlyCountTakesStar() {
        assertError("SUM(*)", "Expected an expression but found '*' (STAR) at line 1, column 5");
    }

    @Test
    void unknownFunction() {
        SyntaxException e = assertError("FOO(x)", "Unknown function 'FOO' at line 1, column 1");
        assertNull(e.expected());
        assertError("upper(name)", "Unknown function 'upper' at line 1, column 1");
        assertError("1 + FOO(2)", "Unknown function 'FOO' at line 1, column 5");
    }

    @Test
    void oneArgumentAndAClosingParenthesis() {
        assertError("COUNT(x", "Expected RIGHT_PAREN but found end of input (EOF) at line 1, column 8");
        assertError("COUNT(*, x)", "Expected RIGHT_PAREN but found ',' (COMMA) at line 1, column 8");
        assertError("COUNT(x, y)", "Expected RIGHT_PAREN but found ',' (COMMA) at line 1, column 8");
    }
}
