package de.jojomodding.np.ccs.expr;

import de.jojomodding.np.lts.Action;
import de.jojomodding.np.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class Parallel extends CCSExpression {

    private final CCSExpression left, right;

    public Parallel(CCSExpression left, CCSExpression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parallel parallel = (Parallel) o;
        return left.equals(parallel.left) &&
               right.equals(parallel.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String deparse(int level) {
        return wrap(level, 3, left.deparse(3) + " | " + right.deparse(2));
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
        Set<Pair<Action, CCSExpression>> res = new HashSet<>(), lr = left.deriveTransitions(env), rr = right.deriveTransitions(env);
        lr.forEach(p -> res.add(new Pair<>(p.first(), new Parallel(p.second(), right))));
        rr.forEach(p -> res.add(new Pair<>(p.first(), new Parallel(left, p.second()))));
        Map<Action, Set<CCSExpression>> syncIntermediary = new HashMap<>();
        lr.stream().filter(p -> !(p.first().isInternal())).forEach(p -> syncIntermediary.computeIfAbsent(p.first(), $ -> new HashSet<>()).add(p.second()));
        rr.forEach(p -> {
            Set<CCSExpression> lres = syncIntermediary.getOrDefault(p.first().inverse(), Set.of());
            lres.forEach(lt -> res.add(new Pair<>(Action.tau(), new Parallel(lt, p.second()))));
        });
        return res;
    }

    public CCSExpression getLeft() {
        return left;
    }

    public CCSExpression getRight() {
        return right;
    }
}
