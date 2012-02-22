package com.britesnow.snow.web.renderer;

import java.io.Writer;

public interface JsonRenderer {

    public void render(Object data, Writer out);
}
