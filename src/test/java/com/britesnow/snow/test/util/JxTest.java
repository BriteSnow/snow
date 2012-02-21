package com.britesnow.snow.test.util;

import java.io.File;

import org.junit.Test;

import com.britesnow.snow.util.Jx;

import static com.britesnow.snow.util.Jx.x;

import static org.junit.Assert.assertEquals;


public class JxTest {
    static File TEST_ASSETS_FOLDER = new File("src/test/resources/");

    @Test
    public void testFind() {
        File iformFile = new File(TEST_ASSETS_FOLDER, "jx/iform.xml");
        

        Jx jx = x(iformFile);

        // check first root element
        assertEquals("Root Element Node Name", "iform", jx.e().getNodeName());
        // check /iform/field Jx size
        assertEquals("Number of fields", 2, jx.find("/iform/field").list().size());
        // check children size
        assertEquals("Number of fields", 2, jx.children().size());
        // check attribute xpath attr(.) method
        assertEquals("First Field Attribute", "CompanyName", jx.find("/iform/field[@name='CompanyName']").attr("name"));
        // check the //xpath, list(), and attr(.)
        assertEquals("Second Field name", "PlanName", jx.find("//field").list().get(1).attr("name"));
        // check xpath [index], attrNames
        assertEquals("Number Field Attributes", 1, jx.find("//field[1]").attrNames().length);
        assertEquals("First Field attribute name", "name", jx.find("//field[1]").attrNames()[0]);
        assertEquals("First Field Input attribute name", "type", jx.find("//field/input").attrNames()[0]);
    }

    @Test
    public void testMergeField() {
        Jx jxForm = x(new File(TEST_ASSETS_FOLDER, "jx/iform.xml"));
        Jx jxTmpl = x(new File(TEST_ASSETS_FOLDER, "jx/itemplate.xml"));

        // check merge CompanyName field
        Jx formCompanyField = jxForm.find("//field[@name='CompanyName']");

        Jx tmplCompanyField = jxTmpl.find("//field[@name='CompanyName']");

        Jx mergedField = tmplCompanyField.merge(formCompanyField);
        
        // check number of attributes (should be 3)
        assertEquals("Number Field Attributes", 3, mergedField.attrNames().length);
        // check the attrubite "type" of the input element
        assertEquals("field.input.@type", "text", mergedField.find("./input").attr("type"));
    }

    @Test
    public void testMergeAllFields() {
        Jx jxForm = x(new File(TEST_ASSETS_FOLDER, "jx/iform.xml"));
        Jx jxTmpl = x(new File(TEST_ASSETS_FOLDER, "jx/itemplate.xml"));

        // make sure there is only one Field
        assertEquals("Template Number of Fields", 1, jxTmpl.find("//field").list().size());

        for (Jx fieldJx : jxForm.children()) {
            Jx jxTmplField = jxTmpl.find("//field[@name='" + fieldJx.attr("name") + "']");
            if (jxTmplField.e() != null) {
                jxTmplField.merge(fieldJx);
            } else {
                jxTmpl.add(fieldJx);
            }
        }

        // check number of attributes (should be 3)
        assertEquals("Number Field", 2, jxTmpl.find("//field").list().size());
        // get the show attribute value
        assertEquals("CompanyName show attribute value", "true", jxTmpl.children().get(0).attr("show"));
        // check the input element
        assertEquals("Input element type", "text", jxTmpl.children().get(0).find("./input").attr("type"));
        // check the default value
        assertEquals("DefaultValue text value", "BitsAndBuzz",jxTmpl.find("/itemplate/field[@name='CompanyName']/defaultValue").value());
    }
    
    @Test 
    public void testV(){
        Jx jxForm = x(new File(TEST_ASSETS_FOLDER, "jx/iform.xml"));
        Jx jxField = jxForm.find("//field").list().get(0);
        
        //check label node value
        assertEquals("Label Node Value", "Company Name", jxField.child("label").value());
        //check changing the node value
        Jx labelJx = jxField.child("label");
        labelJx.value("New Label");
        assertEquals("Label Node Value", "New Label", labelJx.value());
    }
    
    @Test
    public void testChild(){
        Jx jxForm = x(new File(TEST_ASSETS_FOLDER, "jx/iform.xml"));
        Jx jxField = jxForm.find("//field").list().get(0);
        
        //get the input child, and check its attribute
        assertEquals("Field Input type","text",jxField.child("input").attr("type"));
        //get a new child, and set an attribute
        jxField.child("testElem").attr("someParam","someValue").value("someContent");
        assertEquals("testElem","someValue",jxField.child("testElem").attr("someParam"));
        assertEquals("testElem","someContent",jxField.child("testElem").value());
    }
}
