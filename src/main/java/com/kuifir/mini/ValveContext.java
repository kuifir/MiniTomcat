package com.kuifir.mini;

import java.io.IOException;
import javax.servlet.ServletException;

/**
 * ValveContext 接口负责调用下一个 Valve，这样就会形成一系列对 Valve 的调用。
 */
public interface ValveContext {
    String getInfo();
    void invokeNext(Request request, Response response) throws IOException, ServletException;
}