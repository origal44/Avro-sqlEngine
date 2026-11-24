package com.avrosql.parser;

import com.avrosql.lexer.token.Token;
import com.avrosql.lexer.token.TokenType;
import com.avrosql.parser.ast.Aggregate;
import com.avrosql.parser.ast.Between;
import com.avrosql.parser.ast.Binary;
import com.avrosql.parser.ast.ColumnRef;
import com.avrosql.parser.ast.Expr;
import com.avrosql.parser.ast.In;
import com.avrosql.parser.ast.IsNull;
import com.avrosql.parser.ast.Literal;
import com.avrosql.parser.ast.OrderItem;
import com.avrosql.parser.ast.Position;
import com.avrosql.parser.ast.SelectItem;
import com.avrosql.parser.ast.SelectStatement;
import com.avrosql.parser.ast.Unary;
import com.avrosql.parser.exception.SyntaxException;
import com.avrosql.parser.exception.SyntaxRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class Parser {

    private static final int NONE = 0;
    private static final int EITHER = 10;
    private static final int BOTH = 20;
    private static final int NEGATION = 30;
    private static final int COMPARISON = 40;
    private static final int SUM = 50;
    private static final int PRODUCT = 60;
    private static final int UNARY_MINUS = 70;

    private static final String EXPECTED_EXPRESSION = "Expected an expression but found %s";
    private static final String NUMBER_TOO_LARGE = "Number too large: '%s'";
    private static final String CHAINED_COMPARISON = "Comparisons can't be chained: found %s";
    private static final String EXPECTED_AFTER_NOT = "Expected IN, LIKE or BETWEEN but found %s";
    private static final String UNKNOWN_FUNCTION = "Unknown function '%s'";
    private static final String EXPECTED_INTEGER = "Expected a whole number but found %s";

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        if (tokens.isEmpty() || tokens.getLast().type() != TokenType.EOF) {
            throw new IllegalArgumentException("token list must end with EOF");
        }
        this.tokens = List.copyOf(tokens);
    }

    public SelectStatement parse() throws SyntaxException {
        try {
            return parseSelect();
        } catch (SyntaxRuntimeException e) {
            // parse methods throw the unchecked one, callers get the checked one
            throw e.getCause();
        }
    }

    private SelectStatement parseSelect() {
        expect(TokenType.SELECT);
        boolean distinct = match(TokenType.DISTINCT);
        boolean selectAll = match(TokenType.STAR);
        List<SelectItem> items = selectAll ? List.of() : parseSelectList();
        expect(TokenType.FROM);
        String table = expect(TokenType.IDENTIFIER).text();
        Optional<Expr> where = parseWhere();
        List<Expr> groupBy = parseGroupBy();
        // HAVING without GROUP BY is allowed: the whole table is then one group
        Optional<Expr> having = parseHaving();
        List<OrderItem> orderBy = parseOrderBy();
        Optional<Long> limit = parseLimit();
        Optional<Long> offset = limit.isEmpty() ? Optional.empty() : parseOffset();
        expect(TokenType.SEMICOLON);
        expect(TokenType.EOF);
        return new SelectStatement(distinct, selectAll, items, table, where, groupBy, having, orderBy, limit, offset);
    }

    private Optional<Expr> parseWhere() {
        if (!match(TokenType.WHERE)) {
            return Optional.empty();
        }
        return Optional.of(parseExpression());
    }

    private List<Expr> parseGroupBy() {
        if (!match(TokenType.GROUP)) {
            return List.of();
        }
        expect(TokenType.BY);
        List<Expr> keys = new ArrayList<>();
        keys.add(parseExpression());
        while (match(TokenType.COMMA)) {
            keys.add(parseExpression());
        }
        return keys;
    }

    private Optional<Expr> parseHaving() {
        if (!match(TokenType.HAVING)) {
            return Optional.empty();
        }
        return Optional.of(parseExpression());
    }

    private List<OrderItem> parseOrderBy() {
        if (!match(TokenType.ORDER)) {
            return List.of();
        }
        expect(TokenType.BY);
        List<OrderItem> order = new ArrayList<>();
        order.add(parseOrderItem());
        while (match(TokenType.COMMA)) {
            order.add(parseOrderItem());
        }
        return order;
    }

    // ASC is the default, so it's optional
    private OrderItem parseOrderItem() {
        Expr expression = parseExpression();
        if (match(TokenType.DESC)) {
            return OrderItem.desc(expression);
        }
        match(TokenType.ASC);
        return OrderItem.asc(expression);
    }

    private Optional<Long> parseLimit() {
        if (!match(TokenType.LIMIT)) {
            return Optional.empty();
        }
        return Optional.of(wholeNumber());
    }

    private Optional<Long> parseOffset() {
        if (!match(TokenType.OFFSET)) {
            return Optional.empty();
        }
        return Optional.of(wholeNumber());
    }

    // a plain NUMBER token, not an expression: no minus sign, no 1 + 1
    private long wholeNumber() {
        Token token = expect(TokenType.NUMBER);
        if (token.text().indexOf('.') >= 0) {
            throw error(EXPECTED_INTEGER.formatted(SyntaxException.describe(token)), token);
        }
        try {
            return Long.parseLong(token.text());
        } catch (NumberFormatException e) {
            throw error(NUMBER_TOO_LARGE.formatted(token.text()), token);
        }
    }

    private List<SelectItem> parseSelectList() {
        List<SelectItem> items = new ArrayList<>();
        items.add(parseSelectItem());
        // no comma means the list is over
        while (match(TokenType.COMMA)) {
            items.add(parseSelectItem());
        }
        return items;
    }

    // AS is required, so a forgotten comma ("name age") stays an error
    private SelectItem parseSelectItem() {
        Expr expression = parseExpression();
        if (!match(TokenType.AS)) {
            return SelectItem.of(expression);
        }
        return SelectItem.as(expression, expect(TokenType.IDENTIFIER).text());
    }

    Expr parseExpression() {
        return parseExpression(NONE);
    }

    private Expr parseExpression(int minPower) {
        Expr left = parsePrefix();
        // strict '>': an operator of the same power is left for the caller, which makes - and / left-associative
        while (infixPower(peek().type()) > minPower) {
            Token op = advance();
            left = switch (op.type()) {
                case IS -> parseIsNull(left, op);
                case BETWEEN -> parseBetween(left, op);
                case IN -> parseIn(left, op);
                case NOT -> parseInfixNot(left, op);
                default -> new Binary(left, op.type(), parseExpression(infixPower(op.type())), at(op));
            };
            // a < b < c would compare a boolean with c
            if (infixPower(op.type()) == COMPARISON && infixPower(peek().type()) == COMPARISON) {
                throw error(CHAINED_COMPARISON.formatted(SyntaxException.describe(peek())), peek());
            }
        }
        return left;
    }

    // postfix: no right side to parse, only the fixed words [NOT] NULL
    // the NOT of IS NOT NULL is the position of the Unary, IS the position of the IsNull
    private Expr parseIsNull(Expr operand, Token is) {
        Token not = peek();
        boolean negated = match(TokenType.NOT);
        expect(TokenType.NULL);
        Expr isNull = new IsNull(operand, at(is));
        return negated ? new Unary(TokenType.NOT, isNull, at(not)) : isNull;
    }

    // bounds stop below AND, so the first AND is the separator and a later one stays logical
    private Expr parseBetween(Expr value, Token between) {
        Expr low = parseExpression(COMPARISON);
        expect(TokenType.AND);
        Expr high = parseExpression(COMPARISON);
        return new Between(value, low, high, at(between));
    }

    private Expr parseIn(Expr value, Token in) {
        expect(TokenType.LEFT_PAREN);
        List<Expr> values = new ArrayList<>();
        values.add(parseExpression(NONE));
        while (match(TokenType.COMMA)) {
            values.add(parseExpression(NONE));
        }
        expect(TokenType.RIGHT_PAREN);
        return new In(value, values, at(in));
    }

    // NOT after a left side, as in a NOT IN (...); a leading NOT is handled in parsePrefix
    private Expr parseInfixNot(Expr value, Token not) {
        Token next = peek();
        if (match(TokenType.IN)) {
            return new Unary(TokenType.NOT, parseIn(value, next), at(not));
        }
        if (match(TokenType.LIKE)) {
            Expr like = new Binary(value, TokenType.LIKE, parseExpression(COMPARISON), at(next));
            return new Unary(TokenType.NOT, like, at(not));
        }
        if (match(TokenType.BETWEEN)) {
            return new Unary(TokenType.NOT, parseBetween(value, next), at(not));
        }
        throw error(EXPECTED_AFTER_NOT.formatted(SyntaxException.describe(next)), next);
    }

    private Expr parsePrefix() {
        Token token = advance();
        return switch (token.type()) {
            case NUMBER -> numberLiteral(token);
            case STRING -> stringLiteral(token);
            case NULL -> new Literal(null, at(token));
            case TRUE -> new Literal(true, at(token));
            case FALSE -> new Literal(false, at(token));
            // a name followed by '(' is a function call, otherwise a column
            case IDENTIFIER -> match(TokenType.LEFT_PAREN) ? parseAggregate(token) : new ColumnRef(token.text(), at(token));
            case MINUS -> new Unary(TokenType.MINUS, parseExpression(UNARY_MINUS), at(token));
            case NOT -> new Unary(TokenType.NOT, parseExpression(NEGATION), at(token));
            case LEFT_PAREN -> {
                Expr inner = parseExpression(NONE);
                expect(TokenType.RIGHT_PAREN);
                yield inner;
            }
            default -> throw error(EXPECTED_EXPRESSION.formatted(SyntaxException.describe(token)), token);
        };
    }

    // called after the '(' has been matched
    private Expr parseAggregate(Token name) {
        Aggregate.Function function = aggregateFunction(name);
        Optional<Expr> argument;
        if (function == Aggregate.Function.COUNT && match(TokenType.STAR)) {
            argument = Optional.empty();
        } else {
            argument = Optional.of(parseExpression(NONE));
        }
        expect(TokenType.RIGHT_PAREN);
        return new Aggregate(function, argument, at(name));
    }

    private static Aggregate.Function aggregateFunction(Token name) {
        try {
            return Aggregate.Function.valueOf(name.text().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw error(UNKNOWN_FUNCTION.formatted(name.text()), name);
        }
    }

    // tokens that aren't infix operators get NONE, so the loop stops at them
    private static int infixPower(TokenType type) {
        return switch (type) {
            case STAR, SLASH, PERCENT -> PRODUCT;
            case PLUS, MINUS -> SUM;
            case EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, IS, BETWEEN, IN, NOT, LIKE -> COMPARISON;
            case AND -> BOTH;
            case OR -> EITHER;
            default -> NONE;
        };
    }

    private Literal numberLiteral(Token token) {
        String text = token.text();
        if (text.indexOf('.') >= 0) {
            double value = Double.parseDouble(text);
            if (Double.isInfinite(value)) {
                throw error(NUMBER_TOO_LARGE.formatted(text), token);
            }
            return new Literal(value, at(token));
        }
        try {
            return new Literal(Long.parseLong(text), at(token));
        } catch (NumberFormatException e) {
            throw error(NUMBER_TOO_LARGE.formatted(text), token);
        }
    }

    // 'it''s' -> it's
    private static Literal stringLiteral(Token token) {
        String text = token.text();
        return new Literal(text.substring(1, text.length() - 1).replace("''", "'"), at(token));
    }

    private static Position at(Token token) {
        return new Position(token.line(), token.column());
    }

    private static SyntaxRuntimeException error(String problem, Token token) {
        return new SyntaxRuntimeException(new SyntaxException(problem, token));
    }

    Token peek() {
        return tokens.get(current);
    }

    // stays on EOF, so peek() always has a token
    Token advance() {
        Token token = peek();
        if (token.type() != TokenType.EOF) {
            current++;
        }
        return token;
    }

    boolean check(TokenType type) {
        return peek().type() == type;
    }

    boolean match(TokenType type) {
        if (!check(type)) {
            return false;
        }
        advance();
        return true;
    }

    Token expect(TokenType type) {
        if (!check(type)) {
            throw new SyntaxRuntimeException(new SyntaxException(type, peek()));
        }
        return advance();
    }
}
