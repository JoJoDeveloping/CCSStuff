package de.jojomodding.np.algo;

import de.jojomodding.np.lts.Action;
import de.jojomodding.np.lts.LTS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Performs LTS minimisation
 */
public class Minimization {

    private static <T> BiFunction<Set<T>, Action, Set<T>> predecessors(Set<LTS.Transitions<T>> trans) {
        return (of, a) -> trans.stream().filter(t -> t.getAction().equals(a)).filter(t -> of.contains(t.getTarget())).map(LTS.Transitions::getSource).collect(Collectors.toSet());
    }

    /**
     * Finds the minimal LTS bisimilar to the given LTS
     * @param base the LTS to minimize
     * @return the smallest LTS bisimilar to base
     */
    public static <T> LTS<Set<T>> minimizeBisimilarity(LTS<T> base) {
        return minimizeStates(base, predecessors(base.getTransitions()));
    }

    private static <T> LTS<Set<T>> minimizeStates(LTS<T> base, BiFunction<Set<T>, Action, Set<T>> preds) {
        Set<Set<T>> equivalenceClasses = new HashSet<>();
        equivalenceClasses.add(new HashSet<>(base.getStates()));
        Set<Action> allActions = base.getTransitions().stream().map(LTS.Transitions::getAction).collect(Collectors.toUnmodifiableSet());
        outer:
        do {
            for (Action a : allActions) {
                for (Set<T> eq : equivalenceClasses) {
                    Set<T> pre = preds.apply(eq, a);
                    for (Set<T> eq2 : equivalenceClasses) {
                        Set<T> t2 = new HashSet<>(eq2);
                        t2.removeIf(Predicate.not(pre::contains));
                        if (t2.size() == 0 || t2.size() == eq2.size())
                            continue;
                        eq2.removeIf(pre::contains);
                        equivalenceClasses.add(t2);
                        continue outer;
                    }
                }
            }
            break;
        } while (true);
        Map<T, Set<T>> eqMap = new HashMap<>();
        equivalenceClasses.forEach(s -> s.forEach(t -> eqMap.put(t, s)));
        Set<LTS.Transitions<Set<T>>> newTrans = base.getTransitions().stream().map(p -> new LTS.Transitions<>(eqMap.get(p.getSource()), p.getAction(), eqMap.get(p.getTarget()))).collect(Collectors.toUnmodifiableSet());
        return new LTS<>(new ArrayList<>(equivalenceClasses), newTrans, eqMap.get(base.getStart()));
    }


    private static <T> BiFunction<Set<T>, Action, Set<T>> weakPredecessors(LTS<T> lts) {
        Map<T, Set<T>> tauBefores = new HashMap<>();

        lts.getStates().forEach(s -> {
            //make it reflexive
            tauBefores.computeIfAbsent(s, $ -> new HashSet<>()).add(s);
        });

        lts.getTransitions().stream()
           .filter(tr -> tr.getAction().isInternal())
           .forEach(tr -> {
               tauBefores.get(tr.getTarget()).add(tr.getSource());
           });
        int oldSize;
        do {
            oldSize = tauBefores.values().stream().mapToInt(Set::size).sum();
            lts.getTransitions().stream()
               .filter(tr -> tr.getAction().isInternal())
               .forEach(tr -> tauBefores.get(tr.getTarget()).addAll(tauBefores.get(tr.getSource())));
        } while (oldSize != tauBefores.values().stream().mapToInt(Set::size).sum());
        return (tgts, a) -> {
            Set<T> myTauBefores = tgts.stream()
                                      .flatMap(k -> tauBefores.get(k).stream())
                                      .collect(Collectors.toUnmodifiableSet());
            if (a.isInternal())
                return myTauBefores;
            else
                return lts.getTransitions().stream().filter(t -> myTauBefores.contains(t.getTarget()) && t.getAction().equals(a))
                          .flatMap(k -> tauBefores.get(k.getSource()).stream())
                          .collect(Collectors.toUnmodifiableSet());
        };
    }

    private static <T> boolean hasWeakTransaction(Set<LTS.Transitions<T>> t, T start, Action a, T stop) {
        Set<T> starts = new HashSet<>(), oldstarts, stops = new HashSet<>(), oldstops;
        starts.add(start);
        do {
            oldstarts = new HashSet<>(starts);
            t.stream().filter(p -> starts.contains(p.getSource()) && p.getAction().isInternal()).forEach(p -> starts.add(p.getTarget()));
        } while (starts.size() != oldstarts.size());
        stops.add(stop);
        do {
            oldstops = new HashSet<>(stops);
            t.stream().filter(p -> stops.contains(p.getTarget()) && p.getAction().isInternal()).forEach(p -> stops.add(p.getSource()));
        } while (stops.size() != oldstops.size());
        if (a.isInternal()) {
            return starts.stream().anyMatch(stops::contains);
        } else {
            return t.stream().filter(p -> starts.contains(p.getSource()) && stops.contains(p.getTarget())).anyMatch(p -> p.getAction().equals(a));
        }
    }

    /**
     * Finds the minimal LTS weakly bisimilar to the given LTS
     * @param base the LTS to minimize
     * @return the smallest LTS weakly bisimilar to base
     */
    public static <T> LTS<Set<T>> minimizeWeakBisimilarity(LTS<T> base) {
        LTS<Set<T>> minStates = minimizeStates(base, weakPredecessors(base));
        HashSet<LTS.Transitions<Set<T>>> trans = new HashSet<>(minStates.getTransitions());
        for (LTS.Transitions<Set<T>> k : minStates.getTransitions()) {
            trans.remove(k);
            if (!hasWeakTransaction(trans, k.getSource(), k.getAction(), k.getTarget()))
                trans.add(k);
        }
        return new LTS<>(minStates.getStates(), trans, minStates.getStart());
    }

    /**
     * Finds the minimal LTS observation congruent to the given LTS
     * @param base the LTS to minimize
     * @return the smallest LTS observation congruent to base
     */
    public static <T> LTS<Set<T>> minimizeObservationCongruence(LTS<T> base) {
        LTS<Set<T>> wbMin = minimizeWeakBisimilarity(base);
        if (base.getTransitions().stream().filter(t -> t.getSource().equals(base.getStart()) && t.getAction().isInternal())
                .anyMatch(t -> wbMin.getStart().contains(t.getTarget()))) {
            //wbMin.getTransitions() is a HashSet, and thus modifiable.
            wbMin.getTransitions().add(new LTS.Transitions<>(wbMin.getStart(), Action.tau(), wbMin.getStart()));
        }
        return wbMin;
    }


}
