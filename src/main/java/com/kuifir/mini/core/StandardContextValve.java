package com.kuifir.mini.core;

import com.kuifir.mini.Request;
import com.kuifir.mini.Response;
import com.kuifir.mini.ValveContext;
import com.kuifir.mini.connector.http.HttpRequestImpl;
import com.kuifir.mini.valves.ValveBase;

import javax.servlet.ServletException;
import java.io.IOException;

final class StandardContextValve extends ValveBase {
    private static final String info =
            "com.kuifir.mini.core.StandardContextValve/1.0";

    public String getInfo() {
        return (info);
    }

    public void invoke(Request request, Response response, ValveContext valveContext) throws IOException, ServletException {
        System.out.println("StandardContextValve invoke()");
        StandardWrapper servletWrapper = null;
        String uri = ((HttpRequestImpl) request).getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        String servletClassName = servletName;
        StandardContext context = (StandardContext) getContainer();
        servletWrapper = (StandardWrapper) context.getWrapper(servletName);
        try {
            System.out.println("Call service()");
            servletWrapper.invoke(request, response);
        } catch (Exception e) {
            System.out.println(e.toString());
        } catch (Throwable e) {
            System.out.println(e.toString());
        }
    }
}
