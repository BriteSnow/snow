package com.britesnow.snow.test.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.britesnow.snow.util.FileUtil;

public class FileUtilTest {

    @Test
    public void testSplitIdFolder2() {
        String[][] testValues = { { "/firms/[1]", "/firms/01" }, { "/firms/[10]", "/firms/10" },
                { "/firms/[101]", "/firms/01/01" }, { "/firms/[10101]", "/firms/01/01/01" }, { "[110]/photo", "01/10_dir/photo" } };

        //make the "/" the same as the file.separatorChar
        /*
        for (String[] inAndOut : testValues) {
            inAndOut[0] = inAndOut[0].replace('/', File.separatorChar);
            inAndOut[1] = inAndOut[1].replace('/', File.separatorChar);
        }
        */


        //perform the tes
        for (String[] inAndOut : testValues) {
            assertEquals("Test for '" + inAndOut[0] + "'", inAndOut[1], FileUtil.splitIdFolder2(inAndOut[0],'/'));
        }
    }
    
    @Test
    public void testExtraMimeType(){
    	//format: {{fileName,expectedMimeType},..}
    	String[][] testValues = {{"some.xlsm","application/vnd.ms-excel.sheet.macroEnabled.12"}};
    	
    	for (String[] fileAndType : testValues){
    		String fileName = fileAndType[0];
    		assertEquals(fileAndType[1],FileUtil.getExtraMimeType(fileName));
    	}
    	
    }
}
