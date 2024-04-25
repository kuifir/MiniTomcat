package com.kuifir.mini.core;

import com.kuifir.mini.connector.HttpRequestFacade;
import com.kuifir.mini.connector.HttpResponseFacade;
import com.kuifir.mini.connector.http.HttpRequestImpl;
import com.kuifir.mini.connector.http.HttpResponseImpl;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class ApplicationFilterChain implements FilterChain {
    private ArrayList<ApplicationFilterConfig> filters = new ArrayList<>();
    private Iterator<ApplicationFilterConfig> iterator = null;
    private Servlet servlet = null;

    public ApplicationFilterChain() {
        super();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        System.out.println("FilterChain doFilter()");
        internalDoFilter(request, response);
    }

    private void internalDoFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (Objects.isNull(this.iterator)) {
            this.iterator = filters.iterator();
        }
        if (iterator.hasNext()) {
            // 拿到下一个
            ApplicationFilterConfig filterConfig = iterator.next();
            Filter filter = null;
            try {
                //进行过滤，这是职责链模式，一个一个往下传
                filter = filterConfig.getFilter();
                System.out.println("Filter doFilter()");
                //调用filter的过滤逻辑，根据规范，filter中要再次调用filterChain.doFilter
                // 这样又会回到internalDoFilter()方法，就会再拿到下一个filter，
                // 如此实现一个一个往下传
                filter.doFilter(request, response, this);
            } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException |
                     InstantiationException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (Throwable e) {
                throw new ServletException("filterChain.filter", e);
            }
            return;
        }
        try {
            //最后调用servlet
            HttpServletRequest requestFacade = new HttpRequestFacade((HttpRequestImpl) request);
            HttpServletResponse responseFacade = new HttpResponseFacade((HttpResponseImpl) response);
            servlet.service(requestFacade, responseFacade);
        } catch (IOException | ServletException | RuntimeException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new ServletException("filterChain.servlet", e);
        }

    }

    void addFilter(ApplicationFilterConfig filterConfig) {
        this.filters.add(filterConfig);
    }

    void release() {
        this.filters.clear();
        this.iterator = iterator;
        this.servlet = null;
    }

    void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }
}
