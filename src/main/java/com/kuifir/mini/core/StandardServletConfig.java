package com.kuifir.mini.core;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardServletConfig implements ServletConfig {
    private Map<String, String> servletInitParamMap = new ConcurrentHashMap<>();
    private ServletContext servletContext;
    private String servletName;
    public StandardServletConfig(String servletName, ServletContext servletContext, Map<String, String> servletInitParamMap) {
        this.servletInitParamMap = servletInitParamMap;
        this.servletContext = servletContext;
        this.servletName = servletName;
    }
    @Override
    public String getServletName() {
        return servletName;
    }
    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }
    @Override
    public String getInitParameter(String s) {
        return servletInitParamMap.get(s);
    }
    @Override
    public Enumeration<String> getInitParameterNames() {
        return null;
    }
}