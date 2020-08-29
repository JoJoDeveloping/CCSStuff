package de.jojomodding.np.util;

import java.util.Objects;

public class Pair<T, U> {

    private final T t;
    private final U u;

    public Pair(T t, U u) {
        this.t = t;
        this.u = u;
    }

    public T first() {
        return t;
    }

    public U second() {
        return u;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(t, pair.t) &&
               Objects.equals(u, pair.u);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, u);
    }

    @Override
    public String toString() {
        return "(" + t + ", " + u + ")";
    }
}
