package com.kuifir.mini.core;


import com.kuifir.mini.Request;
import com.kuifir.mini.Response;
import com.kuifir.mini.ValveContext;
import com.kuifir.mini.connector.HttpRequestFacade;
import com.kuifir.mini.connector.HttpResponseFacade;
import com.kuifir.mini.connector.http.HttpRequestImpl;
import com.kuifir.mini.connector.http.HttpResponseImpl;
import com.kuifir.mini.valves.ValveBase;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StandardWrapperValve extends ValveBase {
    private FilterDef filterDef = null;

    @Override
    public void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException {
        System.out.println("StandardWrapperValve invoke()");
        HttpServletRequest requestFacade = new HttpRequestFacade((HttpRequestImpl) request);
        HttpServletResponse responseFacade = new HttpResponseFacade((HttpResponseImpl) response);
        Servlet instance = ((StandardWrapper) getContainer()).getServlet();
        //创建filter Chain，再调用filter，然后调用servlet
        ApplicationFilterChain filterChain = createFilterChain(request, instance);
        if (instance != null) {
            if (filterChain != null) {
                filterChain.doFilter((ServletRequest) request, (ServletResponse) response);
                filterChain.release();
            } else {
                instance.service(requestFacade, responseFacade);
            }
        }
    }

    //根据context中的filter map信息挑选出符合模式的filter，创建filterChain
    private ApplicationFilterChain createFilterChain(Request request, Servlet servlet) {
        System.out.println("createFilterChain()");
        if (servlet == null) {
            return null;
        }
        ApplicationFilterChain filterChain = new ApplicationFilterChain();
        filterChain.setServlet(servlet);
        StandardWrapper wrapper = (StandardWrapper) getContainer();
        StandardContext context = (StandardContext) wrapper.getParent();
        //从context中拿到filter的信息
        FilterMap[] filterMaps = context.findFilterMaps();
        if ((filterMaps == null) || (filterMaps.length == 0)) {
            return (filterChain);
        }
        //要匹配的路径
        String requestPath = null;
        if (request instanceof HttpServletRequest) {
            String contextPath = "";
            String requestURI = ((HttpRequestImpl) request).getUri();
            //((HttpServletRequest) request).getRequestURI();
            if (requestURI.length() >= contextPath.length()) {
                requestPath = requestURI.substring(contextPath.length());
            }
        }
        //要匹配的servlet名
        String servletName = wrapper.getName();
        //下面遍历filter Map，找到匹配URL模式的filter，加入到filterChain中
        int n = 0;
        for (int i = 0; i < filterMaps.length; i++) {
            if (!matchFiltersURL(filterMaps[i], requestPath)) continue;
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) context.findFilterConfig(filterMaps[i].getFilterName());
            if (filterConfig == null) {
                continue;
            }
            filterChain.addFilter(filterConfig);
            n++;
        }
        //下面遍历filter Map，找到匹配servlet的filter，加入到filterChain中
        for (FilterMap filterMap : filterMaps) {
            if (!matchFiltersServlet(filterMap, servletName)) continue;
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) context.findFilterConfig(filterMap.getFilterName());
            if (filterConfig == null) {
                continue;
            }
            filterChain.addFilter(filterConfig);
            n++;
        }
        return (filterChain);
    }

    //字符串模式匹配filter的过滤路径
    private boolean matchFiltersURL(FilterMap filterMap, String requestPath) {
        if (requestPath == null) return (false);
        String testPath = filterMap.getURLPattern();
        if (testPath == null) return (false);
        if (testPath.equals(requestPath)) return (true);
        if (testPath.equals("/*")) return (true);
        if (testPath.endsWith("/*")) {
            //路径符合/前缀，通配成功
            String comparePath = requestPath;
            while (true) {
                //以/截取前段字符串，循环匹配
                if (testPath.equals(comparePath + "/*")) return (true);
                int slash = comparePath.lastIndexOf('/');
                if (slash < 0) break;
                comparePath = comparePath.substring(0, slash);
            }
            return (false);
        }
        if (testPath.startsWith("*.")) {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            if ((slash >= 0) && (period > slash)) return (testPath.equals("*." + requestPath.substring(period + 1)));
        }
        return (false); // NOTE - Not relevant for selecting filters
    }

    private boolean matchFiltersServlet(FilterMap filterMap, String servletName) {
        if (servletName == null) {
            return (false);
        } else {
            return (servletName.equals(filterMap.getServletName()));
        }
    }
}
