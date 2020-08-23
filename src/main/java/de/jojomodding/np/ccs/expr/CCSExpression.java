package de.jojomodding.np.ccs.expr;

import de.jojomodding.np.lts.Action;
import de.jojomodding.np.util.Pair;

import java.util.Set;
import java.util.function.Function;

public abstract class CCSExpression {

    @Override
    public abstract boolean equals(Object obj);
    @Override
    public abstract int hashCode();
    public abstract String deparse(int level);

    @Override
    public final String toString() {
        return deparse(Integer.MAX_VALUE);
    }

    protected final String wrap(int haslevel, int minlevel, String s) {
        if (haslevel < minlevel) {
            return "(" + s + ")";
        }
        return s;
    }

    public abstract Set<String> usedVariables();

    public abstract Set<Pair<Action, CCSExpression>> deriveTransitions(Function<String, Set<Pair<Action, CCSExpression>>> env);

}
