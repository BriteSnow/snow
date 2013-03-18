package com.britesnow.snow.test.apptest;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.britesnow.snow.web.renderer.less.LessProcessor;

public class LessProcessorTest {
    
    
    
    @Test
    public void simpleLessTest(){
        String result; 
        
        File simpleLessFile = new File("src/test/resources/simpleApp/css/simple.less");
        
        LessProcessor lessProcessor = new LessProcessor();
        result = lessProcessor.compile(simpleLessFile);
        result = result.replaceAll("\n","").replaceAll("\t","");
        Assert.assertEquals(".colorMix {  color: #ffffff;}.some {  color: #ffffff;}", result);
    }
    
    @Test
    public void importLessTest(){
        String result; 
        
        File simpleLessFile = new File("src/test/resources/simpleApp/css/imports.less");
        
        LessProcessor lessProcessor = new LessProcessor();
        result = lessProcessor.compile(simpleLessFile);
        result = result.replaceAll("\n","").replaceAll("\t","");
        Assert.assertEquals(".colorMix {  color: #ffffff;}.some {  color: #ffffff;}.in-variables {  color: #ff0000;}.require-variables {  color: #ff0000;}",result);
    }
    
}
