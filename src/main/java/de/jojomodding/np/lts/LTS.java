package de.jojomodding.np.lts;

import de.jojomodding.np.Factory;
import de.jojomodding.np.ccs.expr.Binding;
import de.jojomodding.np.ccs.expr.CCSExpression;
import de.jojomodding.np.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.jojomodding.np.Factory.*;

public class LTS<T> {
    private final Set<T> states;
    private final T start;
    private final Set<Transitions<T>> transitions;


    public LTS(Set<Pair<T, Pair<Action, T>>> trans, T start) {
        this.start = start;
        this.transitions = trans.stream().map(Transitions::new).collect(Collectors.toUnmodifiableSet());
        this.states = transitions.stream().flatMap(e -> Stream.of(e.start, e.stop)).collect(Collectors.toUnmodifiableSet());
    }

    public LTS(Set<T> states, Set<Transitions<T>> transitions, T start) {
        this.states = states;
        this.start = start;
        this.transitions = transitions;
    }

    public Set<T> getStates() {
        return states;
    }

    public T getStart() {
        return start;
    }

    public Set<Transitions<T>> getTransitions() {
        return transitions;
    }

    public static class Transitions<T> {
        private final T start, stop;
        private final Action action;

        private Transitions(Pair<T, Pair<Action, T>> base) {
            this.start = base.first();
            this.action = base.second().first();
            this.stop = base.second().second();
        }

        public Transitions(T src, Action a, T tgt) {
            this.start = src;
            this.action = a;
            this.stop = tgt;
        }

        public Action getAction() {
            return action;
        }

        public T getSource() {
            return start;
        }

        public T getTarget() {
            return stop;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Transitions<?> that = (Transitions<?>) o;
            return start.equals(that.start) &&
                   stop.equals(that.stop) &&
                   action.equals(that.action);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, stop, action);
        }

        public String toTupleString() {
            return "(" + start + ", " + action + ", " + stop + ")";
        }

        @Override
        public String toString() {
            return start + " -" + action + "-> " + stop;
        }
    }

    @Override
    public String toString() {
        return "(" + states.stream().map(Objects::toString).collect(Collectors.joining(", ", "{", "}")) + ", "
               + transitions.stream().map(Transitions::toTupleString).collect(Collectors.joining(", ", "{", "}")) + ", "
               + start + ")";
    }

    public LTS<Integer> rename() {
        Map<T, Integer> m = new HashMap<>();
        Set<Integer> ns = new HashSet<>();
        for (T state : states) {
            ns.add(m.size());
            m.put(state, m.size());
        }
        return new LTS<>(ns, transitions.stream().map(p -> new Transitions<>(m.get(p.getSource()), p.getAction(), m.get(p.getTarget()))).collect(Collectors.toUnmodifiableSet()), m.get(start));
    }

    public Pair<Binding, CCSExpression> toCCS() {
        LTS<Integer> k = rename();
        Binding b = new Binding(k.getStates().stream()
                                 .map(i -> new Pair<>("X" + i,
                                                      k.transitions.stream().filter(t -> t.getSource().equals(i))
                                                                 .map(t -> t.getAction().then("X" + t.getTarget()))
                                                                 .collect(Factory.toChoice()))).collect(Collectors.toSet()));
        return new Pair<>(b, var("X" + k.getStart()));
    }

    public String toPseuco() {
        Pair<Binding, CCSExpression> k = toCCS();
        return k.first().toPseuco() + "\n\n" + k.second().toString();
    }

    public String randomWalk(boolean canTerminateEarly) {
        T t = start;
        String walk = t.toString();
        Random r = new Random();
        while (true) {
            final T tl = t;
            List<Pair<Action, T>> choices = transitions.stream().filter(tt -> tt.getSource().equals(tl)).map(p -> new Pair<>(p.getAction(), p.getTarget())).collect(Collectors.toList());
            if (choices.isEmpty()) {
                return walk;
            }
            int i = r.nextInt(choices.size() + (canTerminateEarly ? 1 : 0));
            if (i == choices.size())
                return walk;
            Pair<Action, T> next = choices.get(i);
            walk += " -" + next.first() + "-> " + next.second();
            t = next.second();
        }
    }
}
