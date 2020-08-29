package de.jojomodding.np.ccs.expr;

import de.jojomodding.np.lts.Action;
import de.jojomodding.np.util.Pair;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class Choice extends CCSExpression {

    private final CCSExpression left, right;

    public Choice(CCSExpression left, CCSExpression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Choice choice = (Choice) o;
        return left.equals(choice.left) &&
               right.equals(choice.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    protected String deparse(int level) {
        return wrap(level, 2, left.deparse(2) + " + " + right.deparse(1));
    }

    @Override
    public Set<String> usedVariables() {
        HashSet<String> hs = new HashSet<>();
        hs.addAll(left.usedVariables());
        hs.addAll(right.usedVariables());
        return hs;
    }

    @Override
    public Set<Pair<Action, CCSExpression>> deriveTransitions(Function<String, Set<Pair<Action, CCSExpression>>> env) {
        HashSet<Pair<Action, CCSExpression>> hs = new HashSet<>();
        hs.addAll(left.deriveTransitions(env));
        hs.addAll(right.deriveTransitions(env));
        return hs;
    }

    public CCSExpression getLeft() {
        return left;
    }

    public CCSExpression getRight() {
        return right;
    }
}
