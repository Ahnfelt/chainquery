package com.chainquery;

/*
Person person = database.alias(Person.class);
database.select(person).where(
    has(person.getName()).equalTo("Hansen"), 
    has(person.getAge()).greaterOrEqualTo(21),
    has(person.getFirstChild().getAge()).greaterThan(21),
    has(person.getChildren().any().getAge()).greaterThan(21)
    ).list();
*/

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/** Constraints for the where clause and static methods to generate them. */
public class Where {

    /** Requires all of the listed constraints to be fulfilled. */
    public static Constraint all(final Constraint... constraints) {
        if(constraints.length == 1) {
            return constraints[0];
        }
        return new Constraint() {
            public <R, A> R accept(A argument, Visitor<R, A> visitor) {
                return visitor.all(argument, Arrays.asList(constraints));
            }
        };
    }

    /** Requires at least one of the listed constraints to be fulfilled. */
    public static Constraint any(final Constraint... constraints) {
        if(constraints.length == 1) {
            return constraints[0];
        }
        return new Constraint() {
            public <R, A> R accept(A argument, Visitor<R, A> visitor) {
                return visitor.any(argument, Arrays.asList(constraints));
            }
        };
    }

    /** Creates (part of) a relation constraint (==, >, <, etc.). */
    public static Completion<String> has(final String value) {
        return new Completion<String>(value);
    }

    public static Completion<Double> has(final Double value) {
        return new Completion<Double>(value);
    }
    
    
    /** Represents the possible completions for an atomic constraint. */
    public static class Completion<T> {
        private Either<Object, List<Selector>> argument1;
        
        public Completion(T value) {
            this.argument1 = pick(value, Magic.threadLocal().takeSelectors());
        }
        
        public Constraint equalTo(T value) {
            return relation(value, Relation.EQUAL);
        }

        public Constraint notEqualTo(T value) {
            return relation(value, Relation.NOT_EQUAL);
        }

        public Constraint greaterThan(T value) {
            return relation(value, Relation.GREATER);
        }

        public Constraint greaterOrEqualTo(T value) {
            return relation(value, Relation.GREATER_EQUAL);
        }

        public Constraint lessThan(T value) {
            return relation(value, Relation.LESS);
        }

        public Constraint lessThanOrEqualTo(T value) {
            return relation(value, Relation.LESS_EQUAL);
        }

        public Constraint relation(T value, Relation relation) {
            Either<Object, List<Selector>> argument2 = pick(value, Magic.threadLocal().takeSelectors());
            return resolve(argument1, relation, argument2);
        }
        
        private Either<Object, List<Selector>> pick(Object value, List<Selector> selectors) {
            return selectors.isEmpty() ? 
                Either.<Object, List<Selector>>left(value) : 
                Either.<Object, List<Selector>>right(selectors);
        }
    }
    

    /** A constraint for the where clause. Can only be inspected by a visitor.
        The accept method is parameterized with the return and argument types,
        and the argument is passed to the visitor methods as the first argument. */
    public static abstract class Constraint {
        public abstract <R, A> R accept(A argument, Visitor<R, A> visitor);
        public String toString() {
            return prettyConstraint(this);
        }
    }
    
    /** A visitor for where clause constraints. It's parameterized with the return 
        and argument types, so that it can be used to recursively visit the tree. */
    public interface Visitor<R, A> {
        public R all(A argument, List<Constraint> constraints);
        public R any(A argument, List<Constraint> constraints);
        public R has(A argument, 
            Either<Object, Selector> argument1, 
            Relation relation, 
            Either<Object, Selector> argument2);
    }


    // Just a pretty printer for the constraints
    private static String prettyConstraint(Constraint constraint) {
        StringBuilder builder = new StringBuilder();
        prettyConstraint(builder, constraint);
        return builder.toString();
    }
    
    // Just a pretty printer for the constraints
    private static void prettyConstraint(final StringBuilder builder, Constraint constraint) {
        constraint.accept(null, new Visitor<Void, Void>() {
            public Void all(Void _, List<Constraint> constraints) {
                builder.append("all(");
                for(int i = 0; i < constraints.size(); i++) {
                    if(i > 0) {
                        builder.append(", ");
                    }
                    prettyConstraint(builder, constraints.get(i));
                }
                builder.append(")");
                return null;
            }
            public Void any(Void _, List<Constraint> constraints) {
                builder.append("any(");
                for(int i = 0; i < constraints.size(); i++) {
                    if(i > 0) {
                        builder.append(", ");
                    }
                    prettyConstraint(builder, constraints.get(i));
                }
                builder.append(")");
                return null;
            }
            public Void has(Void _, 
                    Either<Object, Selector> argument1, 
                    Relation relation, 
                    Either<Object, Selector> argument2) {
                builder.append("has(");
                prettyArgument(builder, argument1);
                builder.append(" "); 
                builder.append(relation);
                builder.append(" "); 
                prettyArgument(builder, argument2);
                builder.append(")");
                return null;
            }
        });
    }
    
    // Just a pretty printer for the constraints
    private static void prettyArgument(final StringBuilder builder, Either<Object, Selector> argument) {
        argument.accept(null, new Either.Visitor<Object, Selector, Void, Void>() {
            public Void left(Void _, Object value) {
                builder.append(value);
                return null;
            }
            
            public Void right(Void _, Selector selector) {
                //builder.append(selector.getTableName()); TODO
                //builder.append("@");
                builder.append(selector.getaliasName());
                builder.append(".");
                builder.append(selector.getColumnName());
                return null;
            }
        });
    }
    
    // Reads out the getter methods called on aliases (signalled via thread local side effects),
    // builds up extra constraints for any chaining involved and then builds the final constraint
    // that is either the one-selector or two-selector version of has().
    private static Constraint resolve(
            final Either<Object, List<Selector>> argument1, 
            final Relation relation, 
            final Either<Object, List<Selector>> argument2) {

        final List<Constraint> constraints = new ArrayList<Constraint>();
        final Either<Object, Selector> chained1 = chain(constraints, argument1);
        final Either<Object, Selector> chained2 = chain(constraints, argument2);
        constraints.add(new Constraint() {
            public <R, A> R accept(A argument, Visitor<R, A> visitor) {
                return visitor.has(argument, chained1, relation, chained2);
            }
        });
        if(constraints.size() == 1) {
            return constraints.get(0);
        } else {
            return new Constraint() {
                public <R, A> R accept(A argument, Visitor<R, A> visitor) {
                    return visitor.all(argument, constraints);
                }
            };
        }
    }
    
    private static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }
    
    // Adds equality constraints on the unique ID for any chaining involved in the selectors.
    private static Either<Object, Selector> chain(
            final List<Constraint> constraints, 
            final Either<Object, List<Selector>> argument) {
            
        return argument.accept(null, new Either.Visitor<Object, List<Selector>, Either<Object, Selector>, Void>() {
            public Either<Object, Selector> left(Void _, Object value) {
                return Either.left(value);
            }
            
            public Either<Object, Selector> right(Void _, List<Selector> selectors) {
                for(int i = 0; i < selectors.size() - 1; i++) {
                    final Selector before = selectors.get(i);
                    final Selector after = selectors.get(i + 1);
                    final Selector selector = new Selector(
                        after.getalias(),
                        Magic.uniqueMethod(after.getalias().getClass()));
                    constraints.add(new Constraint() {
                        public <R, A> R accept(A argument, Visitor<R, A> visitor) {
                            return visitor.has(argument, Either.right(before), Relation.EQUAL, Either.right(selector));
                        }
                    });
                }
                return Either.right(last(selectors));
            }
        });
    }
    
    // TODO: Delete the below
    public static void main(String[] _) {
        Person person = Alias.create(Person.class);
        Constraint constraint = all(
            has(person.getName()).equalTo("Peter"),
            has(person.getSpouse().getAge()).equalTo(21.1),
            has(person.getSpouse().getName()).equalTo("Susan")
            );
        System.out.println(constraint);
    }
    
    public static interface Person extends Row {
        public String getName();
        public Double getAge();
        public Person getSpouse();
    }
}

