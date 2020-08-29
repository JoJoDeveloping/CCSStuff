package de.jojomodding.np.ccs.expr;

import de.jojomodding.np.lts.Action;
import de.jojomodding.np.util.Pair;

import java.util.Set;
import java.util.function.Function;

public class Stop extends CCSExpression {
    private static final Stop theStop = new Stop();

    public static Stop instance() {
        return theStop;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Stop;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String deparse(int level) {
        return "0";
    }

    @Override
    public Set<String> usedVariables() {
        return Set.of();
    }

    @Override
    public Set<Pair<Action, CCSExpression>> deriveTransitions(Function<String, Set<Pair<Action, CCSExpression>>> env) {
        return Set.of();
    }


}
