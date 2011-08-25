import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** This class contains the logic for capturing getters and chaining on wildcards
    via thread local side effects. It depends heavily on the Java order of 
    evaluation guarantee (left before right, inner before outer). */
public class Magic {

    public static Magic threadLocal() {
        return threadLocalMagic.get();
    }
    
    public List<Selector> takeSlot() {
        if(!hasSlot()) {
            throw new IllegalStateException("Expected a getter call on a wildcard.");
        }
        return null; // TODO
    }

    public boolean hasSlot() {
        return false; // TODO
    }
    
    public void expectNoSlot() {
        if(hasSlot()) {
            throw new IllegalStateException("Too many unchained getter calls on wildcards.");
        }
    }
    
    public String wildcardName(Row row) {
        String name = wildcardNames.get(row);
        if(name == null) {
            name = "w" + wildcardCounter;
            wildcardCounter++;
            wildcardNames.put(row, name);
        }
        return name;
    }
    
    public static Method uniqueMethod(Class<? extends Row> type) {
        try {
            return type.getMethod("getUnique");
        } catch(NoSuchMethodException e) {
            throw new IllegalArgumentException("Row classes should always have a getUnique() method.");
        }
    }
    
    private Magic() {}
    
    private Map<Row, String> wildcardNames = new WeakHashMap<Row, String>();
    private long wildcardCounter = 0;
    
    private static ThreadLocal<Magic> threadLocalMagic = new ThreadLocal<Magic>() {
        protected Magic initialValue() {
            return new Magic();
        }
    };
}

