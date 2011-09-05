package com.chainquery;

import java.util.*;

public class InMemoryDatabase extends Database {

    // A 'table', classified by the model class, is a mapping from model ID to the models field values.
    private final HashMap<Class, Map<String, Map<String, Object>>> tables =
            new HashMap<Class, Map<String, Map<String, Object>>>();

    @Override
    public <R extends Row> void save(R row, Class<R> _) {
        Map<Class, Map<String, Map<String, Object>>> newTables = new HashMap<Class, Map<String, Map<String, Object>>>();
        extractFieldsOnce(row, newTables);
        for (Class type: newTables.keySet()) {
            Map<String, Map<String, Object>> newTable = newTables.get(type);
            Map<String, Map<String, Object>> table = tables.get(type);
            if (table == null) {
                tables.put(type, newTable);
            } else {
                table.putAll(newTable);
            }
        }
    }

    // TODO: This functionality should be (partly) part of the ChainQuery library.
    private void extractFieldsOnce(
            Row row,
            Map<Class, Map<String, Map<String, Object>>> tables) {
        final Class type = Record.getType(row);
        Map<String, Map<String, Object>> table = tables.get(type);
        if (table == null) {
            table = new HashMap<String, Map<String, Object>>();
            tables.put(type, table);
        } else if (table.containsKey(row.getUnique())) {
            // Do not extract fields twice
            return;
        }
        final HashMap<String, Object> fields = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry: Record.getFields(row).entrySet()) {
            final Object value = entry.getValue();
            if (Record.isRecord(value)) {
                extractFieldsOnce((Row) value, tables);
            } else if (value.getClass().isArray()) {
                // TODO
            } else if (Iterable.class.isAssignableFrom(value.getClass())) {
                // TODO
            } else if (Map.class.isAssignableFrom(value.getClass())) {
                // TODO
            } else {
                fields.put(entry.getKey(), value);
            }
        }
        table.put(row.getUnique(), fields);
    }

    private <T extends Row> T createRecord(Class<T> type, Map<String, Object> fields) {
        final T row = Record.create(type);
        Record.setFields(row, fields);
        return row;
    }

    @Override
    public <R extends Row> List<R> list(Class<R> type, Constraint constraint) {
        Map<String, Map<String, Object>> table = tables.get(type);
        if (table == null) return Collections.emptyList();

        List<R> list = new ArrayList<R>();
        for (Map<String, Object> fields: table.values()) {
            if (constraint.accept(fields, acceptRowVisitor)) {
                list.add(createRecord(type, fields));
            }
        }
        return list;
    }

    private Constraint.Visitor<Boolean, Map<String, Object>> acceptRowVisitor = new Constraint.Visitor<Boolean, Map<String, Object>>() {
        public Boolean all(Map<String, Object> fields, List<Constraint> constraints) {
            for (Constraint constraint: constraints) {
                boolean accepted = constraint.accept(fields, acceptRowVisitor);
                if (! accepted) return false;
            }
            return true;
        }

        public Boolean any(Map<String, Object> fields, List<Constraint> constraints) {
            for (Constraint constraint: constraints) {
                boolean accepted = constraint.accept(fields, acceptRowVisitor);
                if (accepted) return true;
            }
            return false;
        }

        public Boolean has(Map<String, Object> fields, Either<Object, Selector> argument1, Relation relation, Either<Object, Selector> argument2) {
            final Object[] valueMaybe1 = fetchArgument(fields, argument1);
            final Object[] valueMaybe2 = fetchArgument(fields, argument2);
            if (valueMaybe1 == null || valueMaybe2 == null) return false;
            Object value1 = valueMaybe1[0];
            Object value2 = valueMaybe2[0];
            switch(relation) {
                case EQUAL: return value1.equals(value2);
                case NOT_EQUAL: return ! value1.equals(value2);
                case LESS: return false; // TODO
                case LESS_EQUAL: return false; // TODO
                case GREATER: return false; // TODO
                case GREATER_EQUAL: return false; // TODO
            }
            throw new RuntimeException("Un exhaustive switch on " + relation.getClass());
        }

        private Object[] fetchArgument(final Map<String, Object> fields, Either<Object, Selector> argument) {
            return argument.accept(null, new Either.Visitor<Object, Selector, Object[], Void>() {
                public Object[] left(Void o, Object value) {
                    return new Object[] {value};
                }

                public Object[] right(Void o, Selector selector) {
                    final Object o1 = fields.get(selector.getColumnName());
                    if (fields.containsKey(selector.getColumnName())) {
                        return new Object[] {fields.get(selector.getColumnName())};
                    }
                    return null;
                }
            });
        }
    };

}
