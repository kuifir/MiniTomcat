package com.kuifir.mini.core;


import com.kuifir.mini.Request;
import com.kuifir.mini.Response;
import com.kuifir.mini.ValveContext;
import com.kuifir.mini.connector.HttpRequestFacade;
import com.kuifir.mini.connector.HttpResponseFacade;
import com.kuifir.mini.connector.http.HttpRequestImpl;
import com.kuifir.mini.connector.http.HttpResponseImpl;
import com.kuifir.mini.valves.ValveBase;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StandardWrapperValve extends ValveBase {
    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        // TODO Auto-generated method stub
        System.out.println("StandardWrapperValve invoke()");
        HttpServletRequest requestFacade = new HttpRequestFacade((HttpRequestImpl) request);
        HttpServletResponse responseFacade = new HttpResponseFacade((HttpResponseImpl) response);
        Servlet instance = ((StandardWrapper) getContainer()).getServlet();
        if (instance != null) {
            instance.service(requestFacade, responseFacade);
        }
    }
}
