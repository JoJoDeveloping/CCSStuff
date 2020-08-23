package de.jojomodding.np.ccs.expr;

import de.jojomodding.np.util.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Binding {

    private final Function<String, Optional<CCSExpression>> base;
    private final Set<String> domain;

    public Binding(Map<String, CCSExpression> map) {
        this.base = s -> Optional.ofNullable(map.get(s));
        this.domain = map.keySet();
    }

    public Binding(Function<String, Optional<CCSExpression>> f) {
        this.base = f;
        this.domain = null;
    }

    public Binding(Set<Pair<String, CCSExpression>> map) {
        this(map.stream().collect(Collectors.toMap(Pair::first, Pair::second)));
    }

    public Optional<CCSExpression> lookup(String var) {
        if (domain != null && !domain.contains(var))
            return Optional.empty();
        return base.apply(var);
    }


    @Override
    public String toString() {
        if (domain == null)
            return "Γ := " + base;
        return domain.stream().map(k -> base.apply(k).map(v -> k + " -> " + v)).flatMap(Optional::stream).collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Binding binding = (Binding) o;
        if (domain == null || binding.domain == null)
            return super.equals(o);
        if (!domain.equals(binding.domain))
            return false;
        return domain.stream().allMatch(k -> base.apply(k).equals(binding.base.apply(k)));
    }

    @Override
    public int hashCode() {
        if (domain == null)
            return super.hashCode();
        return domain.hashCode() ^ Arrays.hashCode(domain.stream().map(k -> new Pair<>(k, base.apply(k))).toArray());
    }

    public String toPseuco() {
        return domain.stream().map(k -> base.apply(k).map(v -> k + " := " + v)).flatMap(Optional::stream).collect(Collectors.joining("\n"));
    }
}
