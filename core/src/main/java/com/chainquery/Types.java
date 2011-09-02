package com.chainquery;

public class Types {

    // Thanks to Jack Leow on Stackoverflow for suggesting the following:
    public static boolean DEFAULT_BOOLEAN;
    public static byte DEFAULT_BYTE;
    public static short DEFAULT_SHORT;
    public static int DEFAULT_INT;
    public static long DEFAULT_LONG;
    public static float DEFAULT_FLOAT;
    public static double DEFAULT_DOUBLE;

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
