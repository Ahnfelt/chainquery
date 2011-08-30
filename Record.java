import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class Record implements InvocationHandler {
    @SuppressWarnings("unchecked")
    private static <T extends Row> T create(Class<T> type) {
        return (T) Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class[] { type },
            new Record(type));
    }
    
    private Class type;
    private Map<String, Object> fields = new HashMap<String, Object>();
    
    private Record(Class type) {
        this.type = type;
        fields.put("Unique", UUID.randomUUID().toString());
    }

    @SuppressWarnings("unchecked")
    public Object invoke(Object object, Method method, Object[] arguments) throws Throwable {
        Class origin = method.getDeclaringClass();
        if(Row.class.isAssignableFrom(origin)) {
            String name = method.getName();
            if(name.startsWith("get") && arguments.length == 0) {
                String label = name.substring(3);
                return fields.get(label);
            } else if(name.startsWith("set") && arguments.length == 1) {
                String label = name.substring(3);
                return fields.put(label, arguments[0]);
            } else {
                throw new UnsupportedOperationException("Cannot handle wildcard non-getter/non-setter: " + method);
            }
        } else if(Object.class.equals(origin)) {
            String name = method.getName();
            if(name.equals("equals") && arguments.length == 1) {
                if(arguments[0] == null || Row.class.isAssignableFrom(arguments[0].getClass())) {
                    return false;
                } else {
                    return fields.get("Unique").equals(((Row) arguments[0]).getUnique());
                }
            } else if(name.equals("hashCode") && arguments.length == 0) {
                return fields.get("Unique").hashCode();
            } else {
                return method.invoke(this, arguments);
            }
        } else {
            throw new UnsupportedOperationException("Cannot handle wildcard method: " + method);
        }
    }
}

