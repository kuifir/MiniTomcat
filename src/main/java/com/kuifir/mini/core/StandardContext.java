package com.kuifir.mini.core;

import com.kuifir.mini.Context;
import com.kuifir.mini.Request;
import com.kuifir.mini.Response;
import com.kuifir.mini.Wrapper;
import com.kuifir.mini.connector.http.HttpConnector;
import com.kuifir.mini.startup.Bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardContext extends ContainerBase implements Context {

    // 与本容器关联的connector
    HttpConnector connector;
    //包含servlet类和实例的map
    Map<String, String> servletClsMap = new ConcurrentHashMap<>(); //servletName - ServletClassName
    Map<String, StandardWrapper> servletInstanceMap = new ConcurrentHashMap<>();//servletName - servlet

    public StandardContext() {
        super();
        pipeline.setBasic(new StandardContextValve());
        try {
            // create a URLClassLoader
            URL[] urls = new URL[1];
            //这个URLClassloader的工作目录设置在HttpServer.WEB_ROOT
            File classpath = new File(Bootstrap.WEB_ROOT);
            urls[0] = Paths.get(classpath.getCanonicalPath() + File.separator).toUri().toURL();
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        log("Container created.");
    }

    @Override
    public String getInfo() {
        return "Mini Servlet Context, version 0.1";
    }

    //invoke方法用于从map中找到相关的servlet，然后调用
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        System.out.println("StandardContext invoke()");
        super.invoke(request, response);
//        StandardWrapper standardWrapper = null;
//        // 首先根据uri最后一个/号来定位，后面的字符串认为是servlet名字
//        String uri = ((HttpRequestImpl) request).getUri();
//        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
//        String servletClassName = servletName;
//
//        //从容器中获取servlet wrapper
//        standardWrapper = servletInstanceMap.get(servletName);
//        //如果容器内没有这个servlet，先要load类，创建新实例
//        if (Objects.isNull(standardWrapper)) {
//            standardWrapper = new StandardWrapper(servletClassName, this);
//            this.servletClsMap.put(servletName, servletClassName);
//            this.servletInstanceMap.put(servletName, standardWrapper);
//        }
//
//        //然后将调用传递到下层容器即wrapper中,调用service()
//        try {
//            HttpRequestFacade requestFacade = new HttpRequestFacade(request);
//            HttpResponseFacade responseFacade = new HttpResponseFacade(response);
//            System.out.println("Call Service()");
//            standardWrapper.invoke(requestFacade, responseFacade);
//        } catch (Throwable e) {
//            System.out.println(e.getMessage());
//        }

    }

    public Wrapper getWrapper(String name) {
        StandardWrapper servletWrapper = servletInstanceMap.get(name);
        if (servletWrapper == null) {
            String servletClassName = name;
            servletWrapper = new StandardWrapper(servletClassName, this);
            this.servletClsMap.put(name, servletClassName);
            this.servletInstanceMap.put(name, servletWrapper);
        }
        return servletWrapper;
    }

    public void setLoader(URLClassLoader loader) {
        this.loader = loader;
    }

    public HttpConnector getConnector() {
        return connector;
    }

    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void setDisplayName(String displayName) {

    }

    @Override
    public String getDocBase() {
        return null;
    }

    @Override
    public void setDocBase(String docBase) {

    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public void setPath(String path) {

    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int timeout) {

    }

    @Override
    public String getWrapperClass() {
        return null;
    }

    @Override
    public void setWrapperClass(String wrapperClass) {

    }

    @Override
    public Wrapper createWrapper() {
        return null;
    }

    @Override
    public String findServletMapping(String pattern) {
        return null;
    }

    @Override
    public String[] findServletMappings() {
        return new String[0];
    }

    @Override
    public void reload() {

    }
}
