package de.jojomodding.np.ccs.expr;

import de.jojomodding.np.lts.Action;
import de.jojomodding.np.util.Pair;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class Variable extends CCSExpression {

    private final String var;

    public Variable(String v) {
        this.var = v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return var.equals(variable.var);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var);
    }

    @Override
    public String deparse(int level) {
        return var;
    }

    @Override
    public Set<String> usedVariables() {
        return Set.of(var);
    }

    @Override
    public Set<Pair<Action, CCSExpression>> deriveTransitions(Function<String, Set<Pair<Action, CCSExpression>>> env) {
        return env.apply(var);
    }

    public String getVar() {
        return var;
    }
}
