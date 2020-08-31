package de.jojomodding.np.algo;

import de.jojomodding.np.lts.Action;
import de.jojomodding.np.lts.LTS;
import de.jojomodding.np.util.Either;

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
     *
     * @param base the LTS to minimize
     * @return the smallest LTS bisimilar to base
     */
    public static <T> LTS<Set<T>> minimizeBisimilarity(LTS<T> base) {
        return minimizeStates(base, predecessors(base.getTransitions()), $ -> false);
    }

    private static <T> LTS<Set<T>> minimizeStates(LTS<T> base, BiFunction<Set<T>, Action, Set<T>> preds, Predicate<Set<Set<T>>> stopIf) {
        Set<Set<T>> equivalenceClasses = new HashSet<>();
        equivalenceClasses.add(new HashSet<>(base.getStates()));
        Set<Action> allActions = base.getTransitions().stream().map(LTS.Transitions::getAction).collect(Collectors.toUnmodifiableSet());
        outer:
        do {
            if (stopIf.test(equivalenceClasses))
                break;
            for (Action a : allActions) {
                for (Set<T> eq : equivalenceClasses) {
                    Set<T> pre = preds.apply(eq, a);
                    Set<Set<T>> newEQ = new HashSet<>();
                    for (Set<T> eq2 : equivalenceClasses) {
                        Set<T> splt = new HashSet<>(eq2);
                        splt.removeIf(pre::contains);
                        if (splt.size() == 0 || splt.size() == eq2.size()) {
                            newEQ.add(eq2);
                        } else {
                            eq2.removeIf(t -> !pre.contains(t));
                            newEQ.add(splt);
                            newEQ.add(eq2);
                        }
                    }
                    if (newEQ.size() != equivalenceClasses.size()) {
                        equivalenceClasses = newEQ;
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
        if (a.isInternal())
            return starts.contains(stop);

        stops.add(stop);
        do {
            oldstops = new HashSet<>(stops);
            t.stream().filter(p -> stops.contains(p.getTarget()) && p.getAction().isInternal()).forEach(p -> stops.add(p.getSource()));
        } while (stops.size() != oldstops.size());

        return t.stream().filter(p -> starts.contains(p.getSource()) && stops.contains(p.getTarget())).anyMatch(p -> p.getAction().equals(a));
    }

    /**
     * Finds the minimal LTS weakly bisimilar to the given LTS
     *
     * @param base the LTS to minimize
     * @return the smallest LTS weakly bisimilar to base
     */
    public static <T> LTS<Set<T>> minimizeWeakBisimilarity(LTS<T> base) {
        LTS<Set<T>> minStates = minimizeStates(base, weakPredecessors(base), $ -> false);
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
     *
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

    private static <T, U> boolean equalsUpTo(LTS<T> aa, LTS<U> bb, BiFunction<LTS<Either<Integer, Integer>>, Predicate<Set<Set<Either<Integer, Integer>>>>, LTS<Set<Either<Integer, Integer>>>> eqNotion) {
        LTS<Integer> a = aa.rename(), b = bb.rename();
        LTS<Either<Integer, Integer>> merge = a.mergeWith(b);
        LTS<Set<Either<Integer, Integer>>> minimized = eqNotion.apply(merge, ss -> ss.stream().noneMatch(
                s -> s.contains(Either.left(a.getStart())) && s.contains(Either.right(b.getStart())
                )));
        return minimized.getStates().stream().anyMatch(
                s -> s.contains(Either.left(a.getStart())) && s.contains(Either.right(b.getStart()))
        );
    }

    public static <T, U> boolean equalsUpToBisimilarity(LTS<T> a, LTS<U> b) {
        return equalsUpTo(a, b, (base, pred) -> minimizeStates(base, predecessors(base.getTransitions()), pred));
    }

    public static <T, U> boolean equalsUpToWeakBisimilarity(LTS<T> a, LTS<U> b) {
        //we don't care about superfluous transactions
        return equalsUpTo(a, b, (base, pred) -> minimizeStates(base, weakPredecessors(base), pred));
    }

    public static <T, U> boolean equalsUpToObservationCongruence(LTS<T> aa, LTS<U> bb) {
        LTS<Integer> a = aa.rename(), b = bb.rename();
        LTS<Either<Integer, Integer>> merge = a.mergeWith(b);
        LTS<Set<Either<Integer, Integer>>> minimized
                = minimizeStates(merge, weakPredecessors(merge),
                                 ss -> ss.stream().noneMatch(
                                         s -> s.contains(Either.left(a.getStart())) && s.contains(Either.right(b.getStart()))));
        if (minimized.getStates().stream().noneMatch(
                s -> s.contains(Either.left(a.getStart())) && s.contains(Either.right(b.getStart()))))
            return false;
        //check whether initial tau transitions coincide. apart from that we don't care about superfluous transitions, as above
        boolean leftInitialTau = false, rightInitialTau = false;
        if (a.getTransitions().stream().filter(t -> t.getSource().equals(a.getStart()) && t.getAction().isInternal())
             .anyMatch(t -> minimized.getStart().contains(Either.left(t.getTarget())))) {
            leftInitialTau = true;
        }
        if (b.getTransitions().stream().filter(t -> t.getSource().equals(b.getStart()) && t.getAction().isInternal())
             .anyMatch(t -> minimized.getStart().contains(Either.right(t.getTarget())))) {
            rightInitialTau = true;
        }
        return leftInitialTau == rightInitialTau;
    }


}
