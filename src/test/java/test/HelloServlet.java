package test;

import javax.servlet.*;
import java.io.IOException;

public class HelloServlet implements Servlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        String doc = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="utf-8"><title>Test</title></head>
                <body bgcolor="#f0f0f0">
                <h1 align="center">Hello World 你好</h1>
                """;
        res.getWriter().println(doc);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }


    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}