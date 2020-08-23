package de.jojomodding.np.ccs.expr;

import de.jojomodding.np.lts.Action;
import de.jojomodding.np.util.Pair;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Restrict extends CCSExpression {

    private final CCSExpression expr;
    private final FilterSet filter;

    public Restrict(CCSExpression expr, FilterSet filter) {
        this.expr = expr;
        this.filter = filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restrict restrict = (Restrict) o;
        return expr.equals(restrict.expr) &&
               filter.equals(restrict.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr, filter);
    }

    @Override
    public String deparse(int level) {
        return wrap(level, 1, expr.deparse(0) + " \\ " + filter.toString());
    }

    @Override
    public Set<String> usedVariables() {
        return expr.usedVariables();
    }

    @Override
    public Set<Pair<Action, CCSExpression>> deriveTransitions(Function<String, Set<Pair<Action, CCSExpression>>> env) {
        return expr.deriveTransitions(env).stream()
                   .filter(p -> filter.admits(p.first()))
                   .map(k -> new Pair<Action, CCSExpression>(k.first(), new Restrict(k.second(), filter)))
                   .collect(Collectors.toSet());
    }

    public CCSExpression getExpression() {
        return expr;
    }

    public FilterSet getFilter() {
        return filter;
    }
}
