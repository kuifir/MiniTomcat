package test;

import server.Request;
import server.Response;
import server.Servlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HelloServlet implements Servlet {
    @Override
    public void service(Request req, Response res) throws IOException {
        String doc = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="utf-8"><title>Test</title></head>
                <body bgcolor="#f0f0f0">
                <h1 align="center">Hello World 你好</h1>
                """;
        res.getOutput().write(doc.getBytes(StandardCharsets.UTF_8));
    }
}