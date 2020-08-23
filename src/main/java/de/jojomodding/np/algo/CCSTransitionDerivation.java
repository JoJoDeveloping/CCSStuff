package de.jojomodding.np.algo;

import de.jojomodding.np.lts.Action;
import de.jojomodding.np.ccs.expr.Binding;
import de.jojomodding.np.ccs.expr.CCSExpression;
import de.jojomodding.np.ccs.expr.Variable;
import de.jojomodding.np.lts.LTS;
import de.jojomodding.np.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CCSTransitionDerivation {

    private final Binding binding;
    private final Map<String, Set<Pair<Action, CCSExpression>>> derivations;
    private final Map<CCSExpression, Set<Pair<Action, CCSExpression>>> cachedDerivations;

    public CCSTransitionDerivation(Binding b) {
        this.binding = b;
        this.derivations = new HashMap<>();
        this.cachedDerivations = new HashMap<>();
    }

    public Set<Pair<Action, CCSExpression>> derive(CCSExpression expr) {
        if (!cachedDerivations.containsKey(expr)) {
            deriveInternal(expr);
        }
        return cachedDerivations.get(expr);
    }

    private void deriveInternal(CCSExpression base) {
        Set<String> targetVariables = new HashSet<>(base.usedVariables());
        int card;
        do {
            card = targetVariables.size();
            targetVariables.addAll(targetVariables.stream().flatMap(s -> binding.lookup(s).map(CCSExpression::usedVariables).orElse(Set.of()).stream()).collect(Collectors.toSet()));
        } while (card < targetVariables.size());
        targetVariables.removeIf(derivations::containsKey);
        Map<String, Set<Pair<Action, CCSExpression>>> partialDerivations = new HashMap<>(), oldPDs, pdf1 = partialDerivations;

        targetVariables.forEach(e -> pdf1.put(e, new HashSet<>()));
        do {
            oldPDs = partialDerivations;
            partialDerivations = new HashMap<>();
            Map<String, Set<Pair<Action, CCSExpression>>> pdf2 = oldPDs;
            for (String var : targetVariables) {
                partialDerivations.put(var, binding.lookup(var).orElseGet(() -> new Variable(var)).deriveTransitions(
                        e -> {
                            Set<Pair<Action, CCSExpression>> tgt = derivations.get(e);
                            if (tgt != null) return tgt;
                            tgt = pdf2.get(e);
                            if (tgt != null) return tgt;
                            return Set.of();
                        }
                ));
            }
        } while (!partialDerivations.equals(oldPDs));
        partialDerivations.forEach(derivations::put);
        partialDerivations.forEach((k,v) -> cachedDerivations.put(new Variable(k), v));
        cachedDerivations.put(base, base.deriveTransitions(e -> derivations.getOrDefault(e, Set.of())));
    }

    public Set<Pair<CCSExpression, Pair<Action, CCSExpression>>> deriveAllReachable(CCSExpression base) {
        Set<CCSExpression> reachableFragment = new HashSet<>(), oldReachableFragment;
        reachableFragment.add(base);
        do {
            oldReachableFragment = new HashSet<>(reachableFragment);
            oldReachableFragment.stream().flatMap(p -> derive(p).stream().map(Pair::second)).forEach(reachableFragment::add);
        } while (reachableFragment.size() != oldReachableFragment.size());
        return reachableFragment.stream().flatMap(p -> derive(p).stream().map(v -> new Pair<>(p,v))).collect(Collectors.toSet());
    }

    public LTS<CCSExpression> getReachableLTS(CCSExpression base) {
        return new LTS<>(deriveAllReachable(base), base);
    }


}
