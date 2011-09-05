package com.chainquery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;

public class Alias implements InvocationHandler {

    private boolean base;
    private final Class<? extends Row> type;

    private Alias(boolean base, Class<? extends Row> type) {
        this.base = base;
        this.type = type;
    }

    public static <T extends Row> T create(Class<T> type) {
        return create(type, true);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Row> Class<T> getType(T alias) {
        return (Class<T>) alias.type();
    }

    public static <R extends Row> boolean isAlias(R row) {
        return Proxy.getInvocationHandler(row).getClass().equals(Alias.class);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Row> T create(Class<T> type, boolean base) {
        return (T) Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class[] { type },
            new Alias(base, type));
    }

    // TODO: Every chained call will create a separate alias now,
    //       which might result in a lot of joins, but has the right
    //       behavior. Consider caching the chained aliases, but
    //       be careful when dealing with collections.
    @SuppressWarnings("unchecked")
    public Object invoke(Object object, Method method, Object[] arguments) throws Throwable {
        Class origin = method.getDeclaringClass();
        String name = method.getName();
        if(Row.class.isAssignableFrom(origin)) {
            if (name.equals("type")) return type;
            if(name.startsWith("get") && (arguments == null || arguments.length == 0)) {
                Selector selector = new Selector((Row) object, method);
                if(base) {
                    Magic.threadLocal().baseSelector(selector);
                } else {
                    Magic.threadLocal().chainSelector(selector);
                }
                Class result = method.getReturnType();
                if(Row.class.isAssignableFrom(result)) {
                    return create(result, false);
                } else {
                    return Types.getDefaultValue(result);
                }
            } else {
                throw new UnsupportedOperationException("Cannot handle alias non-getter: " + method);
            }
        } else if(Object.class.equals(origin)) {
            if(name.equals("toString") && (arguments == null || arguments.length == 0)) {
                return String.format("%s (Alias)", type.toString());
            } else {
                return method.invoke(this, arguments);
            }
        } else {
            throw new UnsupportedOperationException("Cannot handle alias method: " + method);
        }
    }
}

