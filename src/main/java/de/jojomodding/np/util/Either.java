package de.jojomodding.np.util;

public abstract class Either<L,R> {

    public static class EitherL<L,R> extends Either<L,R> {
        private final L l;

        private EitherL(L l) {
            this.l = l;
        }

        public L getValue() {
            return l;
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final EitherL<?, ?> eitherL = (EitherL<?, ?>) o;

            return l.equals(eitherL.l);
        }

        @Override
        public int hashCode() {
            return l.hashCode();
        }

        @Override
        public String toString() {
            return "inl " + l;
        }
    }


    public static class EitherR<L,R> extends Either<L,R> {
        private final R r;

        private EitherR(R r) {
            this.r = r;
        }

        public R getValue() {
            return r;
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final EitherR<?, ?> eitherR = (EitherR<?, ?>) o;

            return r.equals(eitherR.r);
        }

        @Override
        public int hashCode() {
            return 0xcafebabe ^ r.hashCode();
        }

        @Override
        public String toString() {
            return "inr " + r;
        }
    }

    public abstract boolean isLeft();

    public final boolean isRight() {
        return !isLeft();
    }

    public static <L,R> Either<L,R> left(L l) {
        return new EitherL<>(l);
    }

    public static <L,R> Either<L,R> right(R r) {
        return new EitherR<>(r);
    }
}
