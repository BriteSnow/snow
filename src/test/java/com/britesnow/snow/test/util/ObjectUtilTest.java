package com.britesnow.snow.test.util;

import java.util.Map;

import org.junit.Test;

import com.britesnow.snow.test.util.mock.MockAddress;
import com.britesnow.snow.test.util.mock.MockUser;
import com.britesnow.snow.util.ObjectUtil;

import static com.britesnow.snow.util.MapUtil.mapIt;
import static org.junit.Assert.*;

public class ObjectUtilTest {

    @Test
    public void testFormattedNumer(){
        //test simple getValue
        Long num = ObjectUtil.getValue("1,000.00", Long.class, null);
        assertEquals("1,000.00 long",1000L,num.longValue());
        Map map = mapIt("id","52,000");
        
        //set the 
        MockUser user = new MockUser();
        ObjectUtil.populate(user, map);
        assertEquals("User id",52000L,user.getId().longValue());
    }
    @Test
    public void testSimplePopulate(){
        
        Map map = mapIt("id","52","firstName","firstName 1","lastName","lastName 1","level","manager","other.param","other value");
        
        MockUser user = new MockUser();
        user.setId(-1L);
        user.setFirstName("oldFirstName 1");
        user.setLastName("oldLast name 1");
        

        
        try {
            ObjectUtil.populate(user, map);
            assertEquals("firstName 1", user.getFirstName());
            assertEquals("lastName 1", user.getLastName());
            assertEquals("manager", user.getLevel().name());
            assertEquals((Long)52L, user.getId());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            // TODO Auto-generated catch block
            
        }
    }
    
    @Test
    public void testNestedPopulate(){
        ////Seed
        MockUser user = new MockUser();
        
        MockAddress address = new MockAddress();
        address.setStreet("__TEST__ 19 Bd Paul Cezanne");
        address.setCity("__TEST__ Clermont Ferrand");
        
        user.setAddress(address);
        
        Map map = mapIt("id","52","firstName","firstName 1","address.street","Lyon street");
        
        ////Exec
        try {
            ObjectUtil.populate(user, map);
            assertEquals("firstName 1", user.getFirstName());
            //the street should have changed
            assertEquals("Lyon street", user.getAddress().getStreet());
            //the city should be the same
            assertEquals("__TEST__ Clermont Ferrand", user.getAddress().getCity());
            assertEquals((Long)52L, user.getId());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            // TODO Auto-generated catch block
            
        }
    }
    
    @Test 
    public void testNestedPopulateWithCreate(){
        ////Seed
        MockUser user = new MockUser();
        
        
        Map map = mapIt("address.id","12","address.street","Lyon street","id","52","firstName","firstName 1");
        
        ////Exec
        try {
            ObjectUtil.populate(user, map);
            assertEquals("firstName 1", user.getFirstName());
            assertEquals("Lyon street", user.getAddress().getStreet());
            assertEquals((Long)12L, user.getAddress().getId());
            assertEquals((Long)52L, user.getId());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            // TODO Auto-generated catch block
            
        }
    }
}
