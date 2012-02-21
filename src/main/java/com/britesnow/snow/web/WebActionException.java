/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.web;

import java.util.Map;

import com.britesnow.snow.util.MapUtil;

// DEPRECATED
public class WebActionException extends RuntimeException {
    private static final long serialVersionUID = 310867550571525604L;

    Throwable                 throwable;
    Enum                      alert;

    Object[]                  treeMapArgs;

    // On Demand
    Map<String, Object>       data;
    String                    message;

    public WebActionException(Throwable throwable) {
        this.throwable = throwable;
    }

    public WebActionException(Enum alert, Object... treeMapArgs) {
        this.alert = alert;
        this.treeMapArgs = treeMapArgs;

    }

    public Map<String, Object> getData() {
        if (data == null && treeMapArgs != null) {
            data = MapUtil.nestMapIt(treeMapArgs);
        }
        return data;
    }

    public Enum getAlert() {
        return alert;
    }

    @Override
    public String getMessage() {
        if (message == null) {
            StringBuilder sb = new StringBuilder();
            if (alert != null) {
                sb = new StringBuilder(alert.getClass().getCanonicalName()).append('.').append(alert.name());
                sb.append(alert.name());
                if (treeMapArgs != null) {
                    sb.append(" - ");
                    int l = treeMapArgs.length;
                    for (int i = 0; i < l;) {
                        if (i > 0) {
                            sb.append(',');
                        }
                        sb.append(treeMapArgs[i++]);
                        if (i < l) {
                            sb.append(':').append(treeMapArgs[i++]);
                        }
                    }
                }
            } else if (throwable != null) {
                sb.append(throwable.getMessage());
            }
            message = sb.toString();

        }
        return message;
    }

}
