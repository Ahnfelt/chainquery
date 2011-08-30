import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;

public class Wildcard implements InvocationHandler {
    public static <T extends Row> T create(Class<T> type) {
        return create(type, true);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Row> T create(Class<T> type, boolean base) {
        return (T) Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class[] { type },
            new Wildcard(base));
    }
    
    private boolean base;
    
    private Wildcard(boolean base) {
        this.base = base;
    }

    // TODO: Every chained call will create a separate wildcard now,
    //       which might result in a lot of joins, but has the right
    //       behavior. Consider caching the chained wildcards, but 
    //       be careful when dealing with collections.
    @SuppressWarnings("unchecked")
    public Object invoke(Object object, Method method, Object[] arguments) throws Throwable {
        Class origin = method.getDeclaringClass();
        if(Row.class.isAssignableFrom(origin)) {
            String name = method.getName();
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
                    return getDefaultValue(result);
                }
            } else {
                throw new UnsupportedOperationException("Cannot handle wildcard non-getter: " + method);
            }
        } else if(Object.class.equals(origin)) {
            return method.invoke(this, arguments);
        } else {
            throw new UnsupportedOperationException("Cannot handle wildcard method: " + method);
        }
    }


    // Thanks to Jack Leow on Stackoverflow for suggesting the following:
    private static boolean DEFAULT_BOOLEAN;
    private static byte DEFAULT_BYTE;
    private static short DEFAULT_SHORT;
    private static int DEFAULT_INT;
    private static long DEFAULT_LONG;
    private static float DEFAULT_FLOAT;
    private static double DEFAULT_DOUBLE;

    public static Object getDefaultValue(Class type) {
        if(type.equals(boolean.class)) {
            return DEFAULT_BOOLEAN;
        } else if(type.equals(byte.class)) {
            return DEFAULT_BYTE;
        } else if(type.equals(short.class)) {
            return DEFAULT_SHORT;
        } else if(type.equals(int.class)) {
            return DEFAULT_INT;
        } else if(type.equals(long.class)) {
            return DEFAULT_LONG;
        } else if(type.equals(float.class)) {
            return DEFAULT_FLOAT;
        } else if(type.equals(double.class)) {
            return DEFAULT_DOUBLE;
        } else {
            return null;
        }
    }
}

