package com.britesnow.snow.test;

import org.junit.Assert;

/**
 * Created by jeremychone on 2/8/14.
 */
public class AssertUtils {
	public static void assertContains(String[] contains,String result){
		for (String contain : contains){
			if (result.indexOf(contain) < 0){
				Assert.fail("Result did not contain [" + contain + "] but was:\n" + result);
			}
		}
	}
}
