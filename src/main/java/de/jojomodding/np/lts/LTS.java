package de.jojomodding.np.lts;

import de.jojomodding.np.Factory;
import de.jojomodding.np.ccs.expr.Binding;
import de.jojomodding.np.ccs.expr.CCSExpression;
import de.jojomodding.np.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.jojomodding.np.Factory.var;

/**
 * A (finite) LTS
 * @param <T> the type of the states. Should have proper equality and toString()
 */
public class LTS<T> {
    private final List<T> states;
    private final T start;
    private final Set<Transitions<T>> transitions;


    public LTS(Set<Pair<T, Pair<Action, T>>> trans, T start) {
        this.start = start;
        this.transitions = trans.stream().map(Transitions::new).collect(Collectors.toUnmodifiableSet());
        this.states = transitions.stream().flatMap(e -> Stream.of(e.start, e.stop)).distinct().collect(Collectors.toList());
    }

    public LTS(List<T> states, Set<Transitions<T>> transitions, T start) {
        this.states = states;
        this.start = start;
        this.transitions = transitions;
    }

    public List<T> getStates() {
        return states;
    }

    public T getStart() {
        return start;
    }

    public Set<Transitions<T>> getTransitions() {
        return transitions;
    }

    /**
     * Computes the formal representation of an LTS as a tuple of states, transitions and start state.
     */
    @Override
    public String toString() {
        return "(" + states.stream().map(Objects::toString).collect(Collectors.joining(", ", "{", "}")) + ", "
               + transitions.stream().map(Transitions::toTupleString).collect(Collectors.joining(", ", "{", "}")) + ", "
               + start + ")";
    }

    public LTS<Integer> rename() {
        Map<T, Integer> m = new HashMap<>();
        Map<Integer, T> mm = new HashMap<>();
        List<Integer> ns = new ArrayList<>(states.size());
        for (T state : states) {
            if (ns.isEmpty() || ns.get(ns.size() - 1) != m.size())
                ns.add(m.size());
            mm.put(m.size(), state);
            m.put(state, m.size());
        }

        return new LTS<>(ns, transitions.stream().map(p -> new Transitions<>(m.get(p.getSource()), p.getAction(), m.get(p.getTarget()))).collect(Collectors.toUnmodifiableSet()), m.get(start));
    }

    /**
     * Returns a pair consisting of a binding and a CCS expression, which in that binding generates an LTS isomorphic to this
     */
    public Pair<Binding, CCSExpression> toCCS() {
        LTS<Integer> k = rename();
        Binding b = new Binding(k.getStates().stream()
                                 .map(i -> new Pair<>("X" + i,
                                                      k.transitions.stream().filter(t -> t.getSource().equals(i))
                                                                   .map(t -> t.getAction().then("X" + t.getTarget()))
                                                                   .collect(Factory.toChoice()))).collect(Collectors.toUnmodifiableList()));
        return new Pair<>(b, var("X" + k.getStart()));
    }

    /**
     * Returns a string that can be copy-pasted into pseuco.com and then generate an LTS isomorphic to this.
     */
    public String toPseuco() {
        Pair<Binding, CCSExpression> k = toCCS();
        return k.first().toPseuco() + "\n\n" + k.second().toString();
    }

    /**
     * Performs a random walk through this LTS
     * @param canTerminateEarly whether we can terminate before reaching a terminal state
     * @return a string representation of the walk
     */
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
}
