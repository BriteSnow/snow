package com.britesnow.snow.test.util;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.britesnow.snow.util.MapUtil;

public class MapUtilTest {

    @Test
    public void testGetTreeMapValue(){
        Map treeMap = MapUtil.deepMapIt("user.name","jon","user.age",14L,"group","ace team");
        assertEquals("user.name","jon", MapUtil.getDeepValue(treeMap, "user.name", String.class, null));
        assertEquals("user.age int",(Integer)14, MapUtil.getDeepValue(treeMap, "user.age", Integer.class, null));
        assertEquals("user.age long",(Long)14L, MapUtil.getDeepValue(treeMap, "user.age", Long.class, null));
        assertEquals("group","ace team", MapUtil.getDeepValue(treeMap, "group", String.class, null));
    }
}
