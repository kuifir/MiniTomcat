package server.processor;

import org.apache.commons.lang3.text.StrSubstitutor;
import server.*;

import javax.servlet.Servlet;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ServletProcessor {
    //响应头定义，里面包含变量
    private static String OKMessage = """
            HTTP/1.1 ${StatusCode} ${StatusName}
            Content-Type: ${ContentType}
            Server: minit
            Date: ${ZonedDateTime}
                        
            """;

    public void process(HttpRequest request, HttpResponse response) {
        // 首先根据uri最后一个/号来定位，后面的字符串认为是servlet名字
        String uri = request.getUri();
        String serverName = uri.substring(uri.lastIndexOf("/") + 1);

        //response默认为UTF-8编码
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        //由上面的URLClassLoader加载这个servlet
        Class<?> servletClass = null;
        try {
            servletClass = HttpConnector.loader.loadClass(serverName);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        //创建servlet新实例，然后调用service()，由它来写动态内容到响应体
        Servlet servlet = null;
        try {
            servlet = (Servlet) servletClass.newInstance();
            HttpRequestFacade requestFacade = new HttpRequestFacade(request);
            HttpResponseFacade responseFacade = new HttpResponseFacade(response);
            System.out.println("Call Service()");
            servlet.service(requestFacade, responseFacade);
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }

    }
}
