package de.jojomodding.np.ccs.expr;

import de.jojomodding.np.lts.Action;
import de.jojomodding.np.util.Pair;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class Prefix extends CCSExpression {

    private final CCSExpression then;
    private final Action prefix;

    public Prefix(Action prefix, CCSExpression then) {
        this.then = then;
        this.prefix = prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prefix prefix1 = (Prefix) o;
        return then.equals(prefix1.then) &&
               prefix.equals(prefix1.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(then, prefix);
    }

    @Override
    protected String deparse(int level) {
        return wrap(level, 0, prefix.toString() + "." + then.deparse(0));
    }

    @Override
    public Set<String> usedVariables() {
        return then.usedVariables();
    }

    @Override
    public Set<Pair<Action, CCSExpression>> deriveTransitions(Function<String, Set<Pair<Action, CCSExpression>>> env) {
        return Set.of(new Pair<>(prefix, then));
    }

    public Action getPrefix() {
        return prefix;
    }

    public CCSExpression getThen() {
        return then;
    }
}
