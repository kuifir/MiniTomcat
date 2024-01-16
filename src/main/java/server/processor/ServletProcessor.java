package server.processor;

import org.apache.commons.lang3.text.StrSubstitutor;
import server.HttpServer;
import server.Request;
import server.Response;
import server.Servlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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

    public void process(Request request, Response response) {
        // 首先根据uri最后一个/号来定位，后面的字符串认为是servlet名字
        String uri = request.getUri();
        String serverName = uri.substring(uri.lastIndexOf("/") + 1);
        URLClassLoader loader = null;
        OutputStream output = null;
        try {
            // create a URLClassLoader
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classpath = new File(HttpServer.WEB_ROOT);
            urls[0] =Paths.get(classpath.getCanonicalPath() + File.separator).toUri().toURL();
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        //由上面的URLClassLoader加载这个servlet
        Class<?> servletClass = null;
        try {
            servletClass = loader.loadClass(serverName);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        // 写响应头
        output = response.getOutput();
        String head = composeResponseHead();
        try {
            output.write(head.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //创建servlet新实例，然后调用service()，由它来写动态内容到响应体
        Servlet servlet = null;
        try {
            servlet = (Servlet) servletClass.newInstance();
            servlet.service(request, response);
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
        try {
            output.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    //拼响应头，填充变量值
    private String composeResponseHead() {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("StatusCode", "200");
        valuesMap.put("StatusName", "OK");
        valuesMap.put("ContentType", "text/html;charset=uft-8");
        valuesMap.put("ZonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String responseHead = sub.replace(OKMessage);
        return responseHead;
    }
}
