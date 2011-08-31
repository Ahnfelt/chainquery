package com.chainquery;

/** This holds one of two types of objects (the "left" one or the "right" one). */
public abstract class Either<L, R> {
    public abstract <O, A> O accept(A argument, Visitor<L, R, O, A> visitor);

    public interface Visitor<L, R, O, A> {
        public O left(A argument, L value);
        public O right(A argument, R value);
    }

    public static <L, R> Either<L, R> left(final L value) {
        return new Either<L, R>() {
            public <O, A> O accept(A argument, Visitor<L, R, O, A> visitor) {
                return visitor.left(argument, value);
            }
        };
    }
    
    public static <L, R> Either<L, R> right(final R value) {
        return new Either<L, R>() {
            public <O, A> O accept(A argument, Visitor<L, R, O, A> visitor) {
                return visitor.right(argument, value);
            }
        };
    }
}

