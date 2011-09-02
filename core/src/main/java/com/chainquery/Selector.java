package com.chainquery;

import java.lang.reflect.Method;

public class Selector {

    private final String tableName;
    private final String columnName;
    private final Row alias;
    private final Method method;

    public Selector(Row alias, Method method) {
        this.tableName = alias.getClass().toString(); // TODO: This will return the useless name of the proxy
        this.columnName = method.getName().substring(3);
        this.alias = alias;
        this.method = method;
    }
    
    /** The name of the table (unique within a database). */
    public String getTableName() {
        return tableName;
    }
    
    /** The name of the column (unique within a table). */
    public String getColumnName() {
        return columnName;
    }
    
    /** The name of the alias (unique within a thread). */
    public String getAliasName() {
        // NOTE: This must not be cached as it differs between threads
        return Magic.threadLocal().aliasName(alias);
    }

    /** The alias that was used (rarely needed). */
    public Row getAlias() {
        return alias;
    }
    
    /** The getter method that was used (rarely needed). */
    public Method getMethod() {
        return method;
    }
}

