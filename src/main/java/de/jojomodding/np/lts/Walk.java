package de.jojomodding.np.lts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Walk<T> {
    private final List<T> states;
    private final List<Action> action;

    public Walk(T start) {
        this.states = new LinkedList<>();
        this.action = new LinkedList<>();
        states.add(start);
    }

    public void addStep(Action a, T tgt) {
        states.add(tgt);
        action.add(a);
    }

    @Override
    public String toString() {
        Iterator<T> statesI = states.iterator();
        Iterator<Action> actionI = action.iterator();
        StringBuilder walk = new StringBuilder(statesI.next().toString());
        while (statesI.hasNext()) {
            walk.append(" -").append(actionI.next().toString()).append("-> ").append(statesI.next().toString());
        }
        return walk.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Walk<?> walk = (Walk<?>) o;

        if (states != null ? !states.equals(walk.states) : walk.states != null) return false;
        return action != null ? action.equals(walk.action) : walk.action == null;
    }

    @Override
    public int hashCode() {
        int result = states != null ? states.hashCode() : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }
}
