/*
Person person = database.wildcard(Person.class);
database.select(person, all(
    has(person.getName(), EQUAL, "Hansen"), 
    has(person.getAge(), GREATER, 21),
    has(person.firstChild().getAge(), GREATER, 21),
    has(person.getChildren().any().getAge(), GREATER, 21)));
*/

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/** Constraints for the where clause and static methods to generate them. */
public class Where {

    /** Requires all of the listed constraints to be fulfilled. */
    public static Constraint all(final Constraint... constraints) {
        return new Constraint() {
            public <R, A> R accept(A argument, Visitor<R, A> visitor) {
                return visitor.all(argument, Arrays.asList(constraints));
            }
        };
    }

    /** Requires at least one of the listed constraints to be fulfilled. */
    public static Constraint any(final Constraint... constraints) {
        return new Constraint() {
            public <R, A> R accept(A argument, Visitor<R, A> visitor) {
                return visitor.any(argument, Arrays.asList(constraints));
            }
        };
    }

    /** Requires that the given relation between the cell (must be a getter on a wildcard) 
        and the constantOrCell (must be a value or a getter on a wildcard) is fulfilled. */
    public static Constraint has(final Double cell, final Relation relation, final Double constantOrCell) {
        return resolve(relation, new Type() {
            public <R, A> R accept(A argument, Type.Visitor<R, A> visitor) {
                return visitor.visit(argument, constantOrCell);
            }
        });
    }
    
    /** Requires that the given relation between the cell (must be a getter on a wildcard) 
        and the constantOrCell (must be a value or a getter on a wildcard) is fulfilled. */
    public static Constraint has(final String cell, final Relation relation, final String constantOrCell) {
        return resolve(relation, new Type() {
            public <R, A> R accept(A argument, Type.Visitor<R, A> visitor) {
                return visitor.visit(argument, constantOrCell);
            }
        });
    }
    

    /** A constraint for the where clause. Can only be inspected by a visitor.
        The accept method is parameterized with the return and argument types,
        and the argument is passed to the visitor methods as the first argument. */
    public interface Constraint {
        public <R, A> R accept(A argument, Visitor<R, A> visitor);
    }
    
    /** A visitor for where clause constraints. It's parameterized with the return 
        and argument types, so that it can be used to recursively visit the tree. */
    public interface Visitor<R, A> {
        public R all(A argument, List<Constraint> constraints);
        public R any(A argument, List<Constraint> constraints);
        public R has(A argument, Selector selector, Relation relation, Type constant);
        public R has(A argument, Selector selector1, Relation relation, Selector selector2, Type type);
    }
    
    
    // Reads out the getter methods called on wildcards (signalled via thread local side effects),
    // builds up extra constraints for any chaining involved and then builds the final constraint
    // that is either the one-selector or two-selector version of has().
    private static Constraint resolve(final Relation relation, final Type type) {
        final Magic magic = Magic.threadLocal();
        final List<Constraint> constraints = new ArrayList<Constraint>();
        final List<Selector> selectors1 = magic.takeSlot();
        chain(constraints, selectors1);
        if(magic.hasSlot()) {
            final List<Selector> selectors2 = magic.takeSlot();
            chain(constraints, selectors2);
            constraints.add(new Constraint() {
                public <R, A> R accept(A argument, Visitor<R, A> visitor) {
                    return visitor.has(argument, last(selectors1), relation, last(selectors2), type);
                }
            });
        } else {
            constraints.add(new Constraint() {
                public <R, A> R accept(A argument, Visitor<R, A> visitor) {
                    return visitor.has(argument, last(selectors1), relation, type);
                }
            });
        }
        magic.expectNoSlot();
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
    private static void chain(List<Constraint> constraints, List<Selector> selectors) {
        for(int i = 0; i < selectors.size() - 1; i++) {
            final Selector left = selectors.get(i);
            final Selector right = selectors.get(i + 1);
            final Selector selector = new Selector(
                right.getWildcard(), 
                Magic.uniqueMethod(right.getWildcard().getClass()));
            constraints.add(new Constraint() {
                public <R, A> R accept(A argument, Visitor<R, A> visitor) {
                    return visitor.has(argument, left, Relation.EQUAL, selector, uniqueType);
                }
            });
        }
    }
    
    private static Type uniqueType = new Type() {
        public <R, A> R accept(A argument, Type.Visitor<R, A> visitor) {
            return visitor.visit(argument, "The unique ID of a Row is a String.");
        }
    };
}

// TODO: What happens when the chaining is not observable? eg.
// Address address = person.getAddress();
// database.select(person, has(address.getStreet(), ));
// This should either work or give a clear error.
// The last option could be implemented by knowing which slot to extend,
// and then it's a simple check to see if the slot is empty, in which case
// it's an error.

