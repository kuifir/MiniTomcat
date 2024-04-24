package com.kuifir.mini.connector.http;

import com.kuifir.mini.Request;
import com.kuifir.mini.Response;

import javax.servlet.ServletException;
import java.io.IOException;

public class ServletProcessor {
    private HttpConnector connector;

    public ServletProcessor(HttpConnector connector) {
        this.connector = connector;
    }
    public void process(Request request, Response response) throws ServletException, IOException {
        this.connector.getContainer().invoke(request, response);
    }
}
