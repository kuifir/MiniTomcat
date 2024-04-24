package com.kuifir.mini;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Valve 接口表示的 Container 中的一段用户增加的逻辑，主要就是一个 invoke 方法。
 */
public interface Valve {
    String getInfo();

    Container getContainer();

    void setContainer(Container container);

    void invoke(Request request, Response response, ValveContext context)
            throws IOException, ServletException;
}