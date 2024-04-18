package com.kuifir.mini.connector.http;

import javax.servlet.ServletException;
import java.io.IOException;

public class ServletProcessor {
    private HttpConnector connector;

    public ServletProcessor(HttpConnector connector) {
        this.connector = connector;
    }
    public void process(HttpRequestImpl request, HttpResponseImpl response) throws ServletException, IOException {
        this.connector.getContainer().invoke(request, response);
    }
}
