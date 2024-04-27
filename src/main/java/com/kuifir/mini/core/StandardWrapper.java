package com.kuifir.mini.core;

import com.kuifir.mini.Container;
import com.kuifir.mini.Request;
import com.kuifir.mini.Response;
import com.kuifir.mini.Wrapper;
import com.kuifir.mini.valves.AccessLogValve;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

public class StandardWrapper extends ContainerBase implements Wrapper {
    //wrapper内含了一个servlet实例和类
    private Servlet instance = null;
    private String servletClass;

    public StandardWrapper(String servletClass, StandardContext parent) {
        super();
        pipeline.setBasic(new StandardWrapperValve());
        pipeline.addValve(new AccessLogValve());
        //以ServletContext为parent
        this.parent = parent;
        this.servletClass = servletClass;
        try {
            loadServlet();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getInfo() {
        return "Mini Servlet Wrapper, version 0.1";
    }

    public void setParent(StandardContext container) {
        parent = container;
    }

    public Servlet getServlet() {
        return this.instance;
    }

    // load servlet类，创建新实例，并调用init()方法
    public Servlet loadServlet() throws ServletException {
        if (instance != null)
            return instance;
        Servlet servlet;
        String actualClass = servletClass;
        if (actualClass == null) {
            throw new ServletException("servlet class has not been specified");
        }
        WebappClassLoader classLoader = getLoader();
        Class<?> classClass = null;
        try {
            if (classLoader != null) {
                classClass = classLoader.getClassLoader().loadClass(actualClass);
            }
        } catch (ClassNotFoundException e) {
            throw new ServletException("Servlet class not found");
        }
        try {
            servlet = (Servlet) Objects.requireNonNull(classClass).getConstructor().newInstance();
        } catch (Throwable e) {
            throw new ServletException("Failed to instantiate servlet");
        }
        try {
            servlet.init(null);
        } catch (Throwable f) {
            throw new ServletException("Failed initialize servlet.");
        }
        instance = servlet;
        return servlet;
    }

    //wrapper是最底层容器，调用将转化为service()方法
    @Override
    public void invoke(Request request, Response response)
            throws IOException, ServletException {
        System.out.println("StandardWrapper invoke()");
        super.invoke(request, response);
    }

    public void addChild(Container child) {
    }

    public Container findChild(String name) {
        return null;
    }

    public Container[] findChildren() {
        return null;
    }

    public void removeChild(Container child) {
    }

    @Override
    public int getLoadOnStartup() {
        return 0;
    }

    @Override
    public void setLoadOnStartup(int value) {

    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }

    @Override
    public void addInitParameter(String name, String value) {

    }

    @Override
    public Servlet allocate() throws ServletException {
        return null;
    }

    @Override
    public String findInitParameter(String name) {
        return null;
    }

    @Override
    public String[] findInitParameters() {
        return new String[0];
    }

    @Override
    public void load() throws ServletException {

    }

    @Override
    public void removeInitParameter(String name) {

    }
}