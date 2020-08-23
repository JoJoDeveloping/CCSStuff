package de.jojomodding.np.ccs.expr;

import de.jojomodding.np.lts.Action;
import de.jojomodding.np.lts.Channel;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class FilterSet {

    /**
     * if true, restrict those in filtered
     * if false, allow only those in filtered
     */
    private final boolean isExclusive;
    private final Set<Channel> filtered;

    public FilterSet(boolean isExclusive, Set<Channel> filtered) {
        this.isExclusive = isExclusive;
        this.filtered = filtered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterSet filterSet = (FilterSet) o;
        return isExclusive == filterSet.isExclusive &&
               filtered.equals(filterSet.filtered);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isExclusive, filtered);
    }

    public boolean admits(Action a) {
        return a.getChannel().map(c -> isExclusive != filtered.contains(c)).orElse(true);
    }

    @Override
    public String toString() {
        if (isExclusive) {
            return filtered.stream().map(Channel::toString).collect(Collectors.joining(", ", "{", "}"));
        } else {
            return filtered.stream().map(Channel::toString).collect(Collectors.joining(", ", "{*, ", "}"));
        }
    }

}
