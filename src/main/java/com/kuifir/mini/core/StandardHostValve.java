package com.kuifir.mini.core;

import com.kuifir.mini.Request;
import com.kuifir.mini.Response;
import com.kuifir.mini.ValveContext;
import com.kuifir.mini.connector.http.HttpRequestImpl;
import com.kuifir.mini.valves.ValveBase;

import javax.servlet.ServletException;
import java.io.IOException;

public class StandardHostValve extends ValveBase {
    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        System.out.println("StandardHostValve invoke()");
        String docbase = ((HttpRequestImpl)request).getDocbase();
        System.out.println("StandardHostValve invoke getdocbase : " + docbase);
        StandardHost host = (StandardHost)getContainer();
        StandardContext servletContext = host.getContext(docbase);
        try {
            servletContext.invoke(request, response);
        }
        catch (Throwable e) {
            System.out.println(e);
        }
    }
}
