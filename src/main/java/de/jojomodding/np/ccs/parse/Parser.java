package de.jojomodding.np.ccs.parse;

import de.jojomodding.np.Factory;
import de.jojomodding.np.ccs.expr.Binding;
import de.jojomodding.np.ccs.expr.CCSExpression;
import de.jojomodding.np.ccs.expr.Choice;
import de.jojomodding.np.ccs.expr.FilterSet;
import de.jojomodding.np.ccs.expr.Parallel;
import de.jojomodding.np.ccs.expr.Prefix;
import de.jojomodding.np.ccs.expr.Restrict;
import de.jojomodding.np.ccs.expr.Stop;
import de.jojomodding.np.ccs.expr.Variable;
import de.jojomodding.np.lts.Action;
import de.jojomodding.np.lts.Channel;
import de.jojomodding.np.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Parser {

    private final Lexer lexer;

    public Parser(String s) {
        this.lexer = new Lexer(s);
    }

    public static Pair<Binding, CCSExpression> parse(String s) {
        return new Parser(s).parseContextExpression();
    }

    public Pair<Binding, CCSExpression> parseContextExpression() {
        Binding b = parseBinding();
        CCSExpression e = parseExpression();
        return new Pair<>(b, e);
    }

    private Binding parseBinding() {
        HashMap<String, CCSExpression> binding = new HashMap<>();
        Token t1 = lexer.nextToken(), t2 = lexer.nextToken();
        while (true) {
            if (t1.getType() == Token.Type.IDENT && t2.getType().equals(Token.Type.COLONEQ)) {
                CCSExpression e = parseExpression();
                binding.put(t1.getText(), e);
                t1 = lexer.nextToken();
                t2 = lexer.nextToken();
            } else {
                lexer.pushBack(t2);
                lexer.pushBack(t1);
                return new Binding(binding);
            }
        }
    }

    private CCSExpression parseRestrict() {
        CCSExpression k = parsePrefix();
        Token t = lexer.nextToken();
        if (t.getType() == Token.Type.BACKSLASH) {
            FilterSet f = parseFilter();
            return new Restrict(k, f);
        } else {
            lexer.pushBack(t);
            return k;
        }
    }

    private FilterSet parseFilter() {
        Token t = lexer.nextToken();
        if (t.getType() != Token.Type.LBRACE)
            throw new IllegalArgumentException("Expected LBRACE, not " + t.getType() + "!");
        t = lexer.nextToken();

        boolean isExclusive = true;
        Set<Channel> elems = new HashSet<>();
        if (t.getType() == Token.Type.STAR) {
            isExclusive = false;
            t = lexer.nextToken();
        }

        if (t.getType() == Token.Type.RBRACE)
            return new FilterSet(isExclusive, Set.of());

        loop:
        do {
            if (t.getType() != Token.Type.IDENT)
                throw new IllegalArgumentException("Expected IDENT, not " + t.getType() + "!");
            elems.add(Factory.of(t.getText()));
            t = lexer.nextToken();
            switch (t.getType()) {
                case COMMA:
                    t = lexer.nextToken();
                    continue;
                case RBRACE:
                    break loop;
                default:
                    throw new IllegalArgumentException("Expected COMMA or RBRACE, not " + t.getType() + "!");
            }
        } while (true);
        return new FilterSet(isExclusive, elems);
    }

    private CCSExpression parseExpression() {
        CCSExpression lhs = parseChoice();
        Token t = lexer.nextToken();
        while (t.getType() == Token.Type.PIPE) {
            CCSExpression rhs = parsePrefix();
            lhs = new Parallel(lhs, rhs);
            t = lexer.nextToken();
        }
        lexer.pushBack(t);
        return lhs;
    }

    private CCSExpression parseChoice() {
        CCSExpression lhs = parseRestrict();
        Token t = lexer.nextToken();
        while (t.getType() == Token.Type.PLUS) {
            CCSExpression rhs = parsePrefix();
            lhs = new Choice(lhs, rhs);
            t = lexer.nextToken();
        }
        lexer.pushBack(t);
        return lhs;
    }

    private CCSExpression parsePrefix() {
        Action a = parseAction();
        if (a == null) {
            return parseBase();
        }
        ;
        Token t = lexer.nextToken();
        if (t.getType() != Token.Type.DOT)
            throw new IllegalArgumentException("Expected DOT, not " + t.getType() + "!");
        return new Prefix(a, parsePrefix());
    }

    private Action parseAction() {
        Token t = lexer.nextToken();
        if (t.getType() == Token.Type.TAU)
            return Action.tau();
        if (t.getType() != Token.Type.IDENT) {
            lexer.pushBack(t);
            return null;
        }
        Token t2 = lexer.nextToken();
        switch (t2.getType()) {
            case QUESTIONMARK:
                return Action.receiving(Factory.of(t.getText()));
            case BANG:
                return Action.sending(Factory.of(t.getText()));
            default:
                lexer.pushBack(t2);
                lexer.pushBack(t);
                return null;
        }
    }

    private CCSExpression parseBase() {
        Token k = lexer.nextToken();
        switch (k.getType()) {
            case IDENT:
                return new Variable(k.getText());
            case STOP:
                return new Stop();
            case LPAREN:
                CCSExpression c = parseExpression();
                k = lexer.nextToken();
                if (k.getType() != Token.Type.RPAREN)
                    throw new IllegalArgumentException("Expected RPAREN, not " + k.getType() + "!");
                return c;
        }
        throw new IllegalArgumentException("Expected IDENT, STOP or RPAREN, not " + k.getType() + "!");
    }

}
