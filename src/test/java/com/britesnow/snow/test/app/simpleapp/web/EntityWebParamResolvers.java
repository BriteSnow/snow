package com.britesnow.snow.test.app.simpleapp.web;

import com.britesnow.snow.test.app.simpleapp.dao.EmployeeDao;
import com.britesnow.snow.test.app.simpleapp.entity.Employee;
import com.britesnow.snow.util.AnnotationMap;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.param.annotation.WebParam;
import com.britesnow.snow.web.param.resolver.annotation.WebParamResolver;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EntityWebParamResolvers {

    @Inject
    private EmployeeDao employeeDao;
    
    
    @WebParamResolver
    public Employee resolveEmployee(AnnotationMap annotationMap, Class paramType, RequestContext rc) {
        WebParam webParam = annotationMap.get(WebParam.class);
        String idParamName = webParam.value();
        Long idValue = rc.getParamAs(idParamName, Long.class);
        
        return employeeDao.get(idValue);
    }
}
