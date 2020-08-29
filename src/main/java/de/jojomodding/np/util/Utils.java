package de.jojomodding.np.util;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class Utils {

    @SafeVarargs
    public static <S,T> Function<S, T> fallingBackTo(T fallback, Function<S, Optional<T>>... base) {
        return s -> {
            for (Function<S, Optional<T>> f : base) {
                Optional<T> v = f.apply(s);
                if (v.isPresent())
                    return v.get();
            }
            return fallback;
        };
    }

    public static <S,T> Function<S,Optional<T>> asPartialFunction(Map<S,T> m) {
        return s -> Optional.ofNullable(m.get(s));
    }

}
