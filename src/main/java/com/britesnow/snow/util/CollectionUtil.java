package com.britesnow.snow.util;


public class CollectionUtil {

    
    /**
     * Find the index of a value inside an array. If not found, or any of the param is null, return -1
     * @param array The array to lookup the value (if null, return -1)
     * @param value The value to lookup (if null, return -1)
     * @return the index of the match value, or -1 is not found
     */
    public static int findIndex(Object[] array,Object value){
        if (array == null || value == null){
            return -1;
        }
        int i = 0;
        for (Object obj : array){
            if (obj.equals(value)){
                return i;
            }
            i++;
        }
        return -1;
    }
}
