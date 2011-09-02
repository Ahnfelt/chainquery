package com.chainquery;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;

/** This class contains the logic for capturing getters and chaining on aliases
    via thread local side effects. It depends heavily on the Java order of 
    evaluation guarantee (left before right, inner before outer). */
public class Magic {

    private List<Selector> selectors = new ArrayList<Selector>();
    private Map<Row, String> aliasNames = new WeakHashMap<Row, String>();
    private long aliasCounter = 0;

    // No instantiation elsewhere
    private Magic() {}

    private static ThreadLocal<Magic> threadLocalMagic = new ThreadLocal<Magic>() {
        protected Magic initialValue() {
            return new Magic();
        }
    };

    public static Magic threadLocal() {
        return threadLocalMagic.get();
    }
    
    public void baseSelector(Selector selector) {
        if(!selectors.isEmpty()) {
            throw new IllegalStateException("Two aliases used where at most one was expected.");
        } 
        selectors.add(selector);
    }

    public void chainSelector(Selector selector) {
        if(selectors.isEmpty()) {
            throw new IllegalStateException("Chaining is not allowed without the original alias.");
        } 
        selectors.add(selector);
    }

    public List<Selector> takeSelectors() {
        if(selectors.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<Selector> result = selectors;
            selectors = new ArrayList<Selector>();
            return result;
        }
    }

    public String aliasName(Row row) {
        String name = aliasNames.get(row);
        if(name == null) {
            name = "w" + aliasCounter;
            aliasCounter++;
            aliasNames.put(row, name);
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
}

