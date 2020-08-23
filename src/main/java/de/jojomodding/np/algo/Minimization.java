package de.jojomodding.np.algo;

import de.jojomodding.np.lts.Action;
import de.jojomodding.np.lts.LTS;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Minimization {

    private static <T> BiFunction<Set<T>, Action, Set<T>> predecessors(Set<LTS.Transitions<T>> trans) {
        return (of, a) -> trans.stream().filter(t -> t.getAction().equals(a)).filter(t -> of.contains(t.getTarget())).map(LTS.Transitions::getSource).collect(Collectors.toSet());
    }

    public static <T> LTS<Set<T>> minimizeBisimilarity(LTS<T> base) {
        return minimizeStates(base, predecessors(base.getTransitions()));
    }

    private static <T> LTS<Set<T>> minimizeStates(LTS<T> base, BiFunction<Set<T>, Action, Set<T>> preds) {
        Set<Set<T>> equivalenceClasses = new HashSet<>(), oldEquivalenceClasses;
        equivalenceClasses.add(base.getStates());
        Set<Action> allActions = base.getTransitions().stream().map(LTS.Transitions::getAction).collect(Collectors.toUnmodifiableSet());
        AtomicBoolean didSplit = new AtomicBoolean(false);
        do {
            oldEquivalenceClasses = new HashSet<>(equivalenceClasses);
            didSplit.set(false);
            for (Set<T> eq : oldEquivalenceClasses) {
                for (Action a : allActions) {
                    Set<T> pre = preds.apply(eq, a);
                    equivalenceClasses = equivalenceClasses.stream().flatMap(p -> {
                        Set<T> t1 = new HashSet<>(p), t2 = new HashSet<>(p);
                        t1.removeIf(pre::contains);
                        t2.removeIf(Predicate.not(pre::contains));
                        if (t1.size() == 0)
                            return Stream.of(t2);
                        if (t2.size() == 0)
                            return Stream.of(t1);
                        didSplit.set(true);
                        return Stream.of(t1, t2);
                    }).collect(Collectors.toUnmodifiableSet());
                }
            }
        } while (didSplit.get());
        Map<T, Set<T>> eqMap = new HashMap<>();
        equivalenceClasses.forEach(s -> s.forEach(t -> eqMap.put(t, s)));
        Set<LTS.Transitions<Set<T>>> newTrans = base.getTransitions().stream().map(p -> new LTS.Transitions<>(eqMap.get(p.getSource()), p.getAction(), eqMap.get(p.getTarget()))).collect(Collectors.toUnmodifiableSet());
        return new LTS<>(equivalenceClasses, newTrans, eqMap.get(base.getStart()));
    }

//    private static <T> Set<LTS.Transitions<T>> weakTransClosure(Set<LTS.Transitions<T>> t) {
//        Set<LTS.Transitions<T>> step = new HashSet<>(t), oldStep;
//        Map<T, Set<T>> tauAfters = new HashMap<>(), tauBefores = new HashMap<>();
//        step.stream().filter(tr->tr.getAction().isInternal())
//            .forEach(tr -> {
//                tauAfters.computeIfAbsent(tr.getSource(), $ -> new HashSet<>()).add(tr.getTarget());
//                tauBefores.computeIfAbsent(tr.getTarget(), $ -> new HashSet<>()).add(tr.getSource());
//            });
//        do {
//            oldStep = new HashSet<>(step);
//            oldStep.forEach(tr -> {
//                tauAfters.getOrDefault(tr.getTarget(), Set.of()).forEach(tgt ->
//                        step.add(new LTS.Transitions<>(tr.getSource(), tr.getAction(), tgt)));
//                tauBefores.getOrDefault(tr.getSource(), Set.of()).forEach(src ->
//                        step.add(new LTS.Transitions<>(src, tr.getAction(), tr.getTarget())));
//            });
//        } while (step.size() != oldStep.size());
//        return step;
//    }

    private static <T> BiFunction<Set<T>, Action, Set<T>> weakPredecessors(Set<LTS.Transitions<T>> lts) {
        Map<T, Set<T>> tauBefores = new HashMap<>(), oldTauBefores;

        lts.stream().filter(tr->tr.getAction().isInternal())
            .forEach(tr -> {
                tauBefores.computeIfAbsent(tr.getTarget(), $ -> new HashSet<>()).add(tr.getSource());
            });
        lts.forEach(tr -> {
            //make it reflexive
            tauBefores.computeIfAbsent(tr.getTarget(), $ -> new HashSet<>()).add(tr.getTarget());
            tauBefores.computeIfAbsent(tr.getSource(), $ -> new HashSet<>()).add(tr.getSource());
        });

        Set<T> empty = Set.of();
        do {
            oldTauBefores = new HashMap<>(tauBefores);
            lts.stream().filter(tr->tr.getAction().isInternal())
               .forEach(tr -> {
                   tauBefores.computeIfAbsent(tr.getTarget(), $ -> new HashSet<>()).addAll(tauBefores.getOrDefault(tr.getSource(), empty));
               });
        } while (!oldTauBefores.equals(tauBefores));
        return (tgts, a) -> {
            Set<T> myTauBefores = tgts.stream().flatMap(k -> tauBefores.getOrDefault(k, empty).stream()).collect(Collectors.toUnmodifiableSet());
            return lts.stream().filter(t -> myTauBefores.contains(t.getTarget()) && t.getAction().equals(a))
                    .flatMap(k -> tauBefores.getOrDefault(k.getSource(), empty).stream()).collect(Collectors.toUnmodifiableSet());
        };
    }

    private static <T> boolean hasWeakTransaction(Set<LTS.Transitions<T>> t, T start, Action a, T stop) {
        Set<T> starts = new HashSet<>(), oldstarts, stops = new HashSet<>(), oldstops;
        starts.add(start);
        do {
            oldstarts = new HashSet<>(starts);
            t.stream().filter(p -> starts.contains(p.getSource()) && p.getAction().isInternal()).forEach(p->starts.add(p.getTarget()));
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

    public static <T> LTS<Set<T>> minimizeWeakBisimilarity(LTS<T> base) {
        LTS<Set<T>> minStates = minimizeStates(base, weakPredecessors(base.getTransitions()));
        HashSet<LTS.Transitions<Set<T>>> trans = new HashSet<>(minStates.getTransitions());
//        int counter = 0;
        for (LTS.Transitions<Set<T>> k : minStates.getTransitions()) {
            trans.remove(k);
            if (!hasWeakTransaction(trans, k.getSource(), k.getAction(), k.getTarget()))
                trans.add(k);
//            if(((counter++)%100) == 99) {
//                System.out.println(counter);
//            }
        }
        return new LTS<>(minStates.getStates(), trans, minStates.getStart());
    }

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
