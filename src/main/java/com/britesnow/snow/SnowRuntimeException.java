/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow;

import java.util.Map;

import com.britesnow.snow.util.MapUtil;

public class SnowRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 6761082312332003153L;

    private Enum              errorEnum;

    private Object            errorData;

    private Object[]          nameAndValueArray;

    protected Throwable       usefulCause;

    public SnowRuntimeException(Throwable cause) {
        super(cause);
    }

    public SnowRuntimeException(Enum errorEnum, Throwable cause, Object... nameAndValueArray) {
        super(errorEnum.name(), cause);
        this.errorEnum = errorEnum;
        this.nameAndValueArray = nameAndValueArray;
    }

    public SnowRuntimeException(Enum causeEnum, Object... nameAndValueArray) {
        super(causeEnum.name());
        this.errorEnum = causeEnum;
        this.nameAndValueArray = nameAndValueArray;
    }

    public SnowRuntimeException(Enum errorEnum, Object errorData) {
        super(errorEnum.name());
        this.errorData = errorData;
    }
    

    public SnowRuntimeException setErrorData(Object errorData) {
        this.errorData = errorData;
        return this;
    }
    
    public Object getErrorData(){
        return this.errorData;
    }

    public String getErrorCode() {
        if (errorEnum != null) {
            return errorEnum.getClass().getSimpleName() + "." + errorEnum.name();
        } else if (getCause() != null) {
            return getCause().getClass().getSimpleName();
        } else {
            return "";
        }
    }

    @Override
    public String getMessage() {

        if (errorEnum != null) {
            StringBuilder sb = null;

            sb = new StringBuilder(errorEnum.name());
            if (nameAndValueArray != null) {
                sb.append("  {");
                Map m = MapUtil.mapIt(nameAndValueArray);
                boolean first = true;
                for (Object name : m.keySet()) {
                    if (!first) {
                        sb.append(',');
                    } else {
                        first = false;
                    }

                    sb.append('"').append(name).append('"');
                    sb.append(':');
                    sb.append('"').append(m.get(name)).append('"');
                }
                sb.append('}');
            }
            return sb.toString();
        } else if (usefulCause != null) {
            return usefulCause.getMessage();
        } else {
            return super.getMessage();
        }
    }

}
