package server;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ServletContainer {
    //一个全局的class loader
    public static URLClassLoader loader = null;
    HttpConnector connector;
    //包含servlet类和实例的map
    Map<String, String> servletClsMap = new ConcurrentHashMap<>(); //servletName - ServletClassName
    Map<String, ServletWrapper> servletInstanceMap = new ConcurrentHashMap<>();//servletName - servlet

    public ServletContainer() {
        try {
            // create a URLClassLoader
            URL[] urls = new URL[1];
            //这个URLClassloader的工作目录设置在HttpServer.WEB_ROOT
            File classpath = new File(HttpServer.WEB_ROOT);
            urls[0] = Paths.get(classpath.getCanonicalPath() + File.separator).toUri().toURL();
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    //invoke方法用于从map中找到相关的servlet，然后调用
    public void invoke(HttpRequest request, HttpResponse response) throws IOException, ServletException {
        ServletWrapper servletWrapper = null;
        // 首先根据uri最后一个/号来定位，后面的字符串认为是servlet名字
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        String servletClassName = servletName;
        servletWrapper = servletInstanceMap.get(servletName);

        //如果容器内没有这个servlet，先要load类，创建新实例
        if (Objects.isNull(servletWrapper)) {
            servletWrapper = new ServletWrapper(servletClassName,this);
             this.servletClsMap.put(servletName, servletClassName);
             this.servletInstanceMap.put(servletName, servletWrapper);
        }
        //然后调用service()
        try {
            HttpRequestFacade requestFacade = new HttpRequestFacade(request);
            HttpResponseFacade responseFacade = new HttpResponseFacade(response);
            System.out.println("Call Service()");
            servletWrapper.invoke(requestFacade, responseFacade);
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }


    }

    public static URLClassLoader getLoader() {
        return loader;
    }

    public static void setLoader(URLClassLoader loader) {
        ServletContainer.loader = loader;
    }

    public HttpConnector getConnector() {
        return connector;
    }

    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }
}
