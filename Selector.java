import java.lang.reflect.Method;

public class Selector {

    private final String tableName;
    private final String columnName;
    private final Row wildcard;
    private final Method method;

    public Selector(Row wildcard, Method method) {
        this.tableName = wildcard.getClass().toString(); // TODO: This will return the useless name of the proxy
        this.columnName = method.getName().substring(3);
        this.wildcard = wildcard;
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
    
    /** The name of the wildcard (unique within a thread). */
    public String getWildcardName() {
        // NOTE: This must not be cached as it differs between threads
        return Magic.threadLocal().wildcardName(wildcard);
    }

    /** The wildcard that was used (rarely needed). */
    public Row getWildcard() {
        return wildcard;
    }
    
    /** The getter method that was used (rarely needed). */
    public Method getMethod() {
        return method;
    }
}

