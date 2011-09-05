package com.chainquery;

import java.util.List;

public abstract class Database {

    /**
     * <p>Create a new alias for doing queries on the specified type.</p>
     * <b><i>NB.</i></b> The created alias is <i>only</i> for queries and
     * cannot be used as a model object even though it has the right type.
     * @param type The interface class matching the desired alias.
     * @param <R> The interface
     * @return The alias
     */
    public <R extends Row> R alias(Class<R> type) {
        return Alias.create(type);
    }

    /**
     * Create a new row or model object of the specified type.
     * @param type The interface class to instantiate
     * @param <R> The Interface
     * @return The model instant.
     */
    public static <R extends Row> R create(Class<R> type) {
        return Record.create(type);
    }

    public <R extends Row> Select<R> select(R alias) {
        return select(Alias.getType(alias));
    }

    public <R extends Row> Select<R> select(Class<R> type) {
        return new Select<R>(type);
    }

    public class Select<R extends Row> {

        private final Class<R> type;

        public Select(Class<R> type) {
            this.type = type;
        }

        public Where<R> where(Constraint ... constraints) {
            return new Where<R>(Constraint.all(constraints), type);
        }
    }

    public class Where<R extends Row> {

        private final Constraint constraint;
        private final Class<R> type;

        protected Where(Constraint constraint, Class<R> type) {
            this.constraint = constraint;
            this.type = type;
        }

        public List<R> list() {
            return Database.this.list(type, constraint);
        }

        public R first() {
            return Database.this.first(type, constraint);
        }
    }

    public <R extends Row> void save(R row) {
        if (Alias.isAlias(row)) throw new RuntimeException("Trying to save alias object");
        save(row, Record.getType(row));
    }

    protected abstract <R extends Row> void save(R row, Class<R> type);

    protected abstract <R extends Row> List<R> list(Class<R> type, Constraint constraint);

    protected <R extends Row> R first(Class<R> type, Constraint constraint) {
        return list(type, constraint).get(0);
    }

}

