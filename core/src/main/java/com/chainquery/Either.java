package com.chainquery;

/** This holds one of two types of objects (the "left" one or the "right" one). */
public abstract class Either<Left, Right> {
    public abstract <Return, Argument> Return accept(Argument argument, Visitor<Left, Right, Return, Argument> visitor);

    public interface Visitor<Left, Right, Return, Argument> {
        public Return left(Argument argument, Left value);
        public Return right(Argument argument, Right value);
    }

    public static <Left, Right> Either<Left, Right> left(final Left value) {
        return new Either<Left, Right>() {
            public <Return, Argument> Return accept(Argument argument, Visitor<Left, Right, Return, Argument> visitor) {
                return visitor.left(argument, value);
            }
        };
    }
    
    public static <Left, Right> Either<Left, Right> right(final Right value) {
        return new Either<Left, Right>() {
            public <Return, Argument> Return accept(Argument argument, Visitor<Left, Right, Return, Argument> visitor) {
                return visitor.right(argument, value);
            }
        };
    }
}

