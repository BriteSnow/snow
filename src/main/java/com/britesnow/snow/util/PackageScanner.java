package com.britesnow.snow.util;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

/**
 * Just a convenient Guava ClassPath wraper to find Classes based on Predicate.
 * 
 * For now, just assume ClassScanner classloader, and TopLevel Recursive scal
 * 
 * @author jeremychone
 * 
 */
public class PackageScanner {

    private ClassLoader cl = PackageScanner.class.getClassLoader();

    private String      basePackage;

    public PackageScanner(String basePackage) {
        this.basePackage = basePackage;
    }

    public Class[] findClasses(Predicate<Class> p) {
        Set<ClassInfo> classInfoSet = getTopLevelClassesRecursive();
        List<Class> classList = new ArrayList<Class>();

        for (ClassInfo ci : classInfoSet) {
            Class cls = ci.load();
            if (p.apply(cls)) {
                classList.add(cls);
            }
        }

        Class[] classes = new Class[classList.size()];
        classList.toArray(classes);
        return classes;
    }

    public Class[] findAnnotatedClasses(Class<? extends Annotation>... annotations) {
        Set<ClassInfo> classInfoSet = getTopLevelClassesRecursive();
        List<Class> classList = new ArrayList<Class>();
        
        for (ClassInfo ci : classInfoSet) {
            Class cls = ci.load();
            boolean pass = true;
            for (Class anCls : annotations){
                Annotation a = cls.getAnnotation(anCls);
                if (a == null){
                    pass = false;
                    break;
                }
            }
            if (pass) {
                classList.add(cls);
            }
        }
        
        Class[] classes = new Class[classList.size()];
        classList.toArray(classes);
        return classes;        
    }

    private Set<ClassInfo> getTopLevelClassesRecursive() {
        try {
            ClassPath cp = ClassPath.from(cl);
            Set<ClassInfo> classInfoSet = cp.getTopLevelClassesRecursive(basePackage);
            return classInfoSet;
        } catch (IOException io) {
            throw Throwables.propagate(io);
        }
    }
}
