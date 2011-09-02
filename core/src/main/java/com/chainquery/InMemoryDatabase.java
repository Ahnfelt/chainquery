package com.chainquery;

import java.util.HashMap;
import java.util.List;

public class InMemoryDatabase extends Database {

    private final HashMap<Class, Row> rows = new HashMap<Class, Row>();

    @Override
    public <R extends Row> void save(R row, Class<R> type) {
        rows.put(type, row);
    }

    @Override
    public <R extends Row> List<R> list(Class<R> type, Constraint constraint) {
        return null;
    }

}
