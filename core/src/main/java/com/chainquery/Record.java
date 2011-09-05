package com.chainquery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class Record implements InvocationHandler {

    private Class type;
    private Map<String, Object> fields = new HashMap<String, Object>();

    // No instantiation elsewhere
    private Record(Class type) {
        this.type = type;
        fields.put("Unique", UUID.randomUUID().toString());
    }

    public static boolean isRecord(Object row) {
        return row != null &&
                Proxy.isProxyClass(row.getClass()) &&
                Proxy.getInvocationHandler(row).getClass().equals(Record.class);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Row> Class<T> getType(T row) {
        return (Class<T>) row.type();
    }

    private static Record getRecord(Row row) {
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(row);
        if (!invocationHandler.getClass().equals(Record.class)) {
            if (Alias.isAlias(row)) throw new RuntimeException("The given row is an alias");
            else throw new RuntimeException("The given row is not a proper Row instance");
        }
        return (Record) invocationHandler;
    }

    public static <R extends Row> Map<String, Object> getFields(R row) {
        return Collections.unmodifiableMap(getRecord(row).fields);
    }

    public static <R extends Row> void setFields(R row, Map<String, Object> fields) {
        getRecord(row).fields = fields;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Row> T create(Class<T> type) {
        return (T) Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class[] { type },
            new Record(type));
    }

    @SuppressWarnings("unchecked")
    public Object invoke(Object object, Method method, Object[] arguments) throws Throwable {
        Class origin = method.getDeclaringClass();
        if(Row.class.isAssignableFrom(origin)) {
            String name = method.getName();
            if (name.equals("type")) return type;
            if(name.startsWith("get") && (arguments == null || arguments.length == 0)) {
                String label = name.substring(3);
                return fields.get(label);
            } else if(name.startsWith("set") && arguments.length == 1) {
                String label = name.substring(3);
                return fields.put(label, arguments[0]);
            } else {
                throw new UnsupportedOperationException("Cannot handle alias non-getter/non-setter: " + method);
            }
        } else if(Object.class.equals(origin)) {
            String name = method.getName();
            if(name.equals("equals") && arguments.length == 1) {
                if(arguments[0] == null || Row.class.isAssignableFrom(arguments[0].getClass())) {
                    return false;
                } else {
                    return fields.get("Unique").equals(((Row) arguments[0]).getUnique());
                }
            } else if(name.equals("hashCode") && (arguments == null || arguments.length == 0)) {
                return fields.get("Unique").hashCode();
            } else if(name.equals("toString") && (arguments == null || arguments.length == 0)) {
                return String.format("%s (Record %s)", type.toString(), fields.get("Unique"));
            } else {
                return method.invoke(this, arguments);
            }
        } else {
            throw new UnsupportedOperationException("Cannot handle alias method: " + method);
        }
    }
}

