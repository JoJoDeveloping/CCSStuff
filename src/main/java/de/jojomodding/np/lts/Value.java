package de.jojomodding.np.lts;

public class Value {

    private final int contained;

    public Value(int i) {
        this.contained = i;
    }

    public int getContained() {
        return contained;
    }

    @Override
    public String toString() {
        return Integer.toString(contained);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Value value = (Value) o;

        return contained == value.contained;
    }

    @Override
    public int hashCode() {
        return contained;
    }
}
