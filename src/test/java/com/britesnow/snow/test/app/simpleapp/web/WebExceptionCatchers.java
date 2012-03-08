package com.britesnow.snow.test.app.simpleapp.web;

import java.io.StringReader;

import javax.inject.Singleton;

import com.britesnow.snow.test.app.simpleapp.dao.DaoException;
import com.britesnow.snow.web.RequestContext;
import com.britesnow.snow.web.exception.WebExceptionContext;
import com.britesnow.snow.web.exception.annotation.WebExceptionCatcher;
import com.britesnow.snow.web.renderer.HttpWriter;
import com.google.inject.Inject;

@Singleton
public class WebExceptionCatchers {

    @Inject
    HttpWriter httpWriter;
    
    @WebExceptionCatcher
    public void catchDaoException(DaoException e, WebExceptionContext context, RequestContext rc){
        httpWriter.writeStringContent(rc, "error.txt", new StringReader("From WebExceptionCatchers.catchDaoException: " + e.getMessage()), false, null);
    }
}
