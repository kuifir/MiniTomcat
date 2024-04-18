package server;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

public class ServletWrapper extends ContainerBase {
    //wrapper内含了一个servlet实例和类
    private Servlet instance = null;
    private String servletClass;

    public ServletWrapper(String servletClass, ServletContext parent) {
        //以ServletContext为parent
        this.parent = parent;
        this.servletClass = servletClass;
        try {
            loadServlet();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }


    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }

    @Override
    public String getInfo() {
        return "Mini Servlet Wrapper, version 0.1";
    }

    public void setParent(ServletContext container) {
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
        ClassLoader classLoader = getLoader();
        Class<?> classClass = null;
        try {
            if (classLoader != null) {
                classClass = classLoader.loadClass(actualClass);
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
    public void invoke(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (instance != null) {
            instance.service(request, response);
        }
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
}