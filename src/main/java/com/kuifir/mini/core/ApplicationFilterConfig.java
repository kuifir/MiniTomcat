package com.kuifir.mini.core;

import com.kuifir.mini.Context;
import com.kuifir.mini.Loader;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

public class ApplicationFilterConfig implements FilterConfig {

    private Context context = null;
    private Filter filter = null;
    private FilterDef filterDef = null;

    public ApplicationFilterConfig(Context context, FilterDef filterDef) throws ClassCastException, ClassNotFoundException, IllegalAccessException, InstantiationException, ServletException, InvocationTargetException, NoSuchMethodException {
        super();
        this.context = context;
        setFilterDef(filterDef);
    }

    Filter getFilter() throws ClassCastException, ClassNotFoundException, IllegalAccessException, InstantiationException, ServletException, NoSuchMethodException, InvocationTargetException {
        // 返回现有的过滤器实例（如果有的话）
        if (this.filter != null){
            return (this.filter);
        }
        // 确定我们将使用的类加载器
        String filterClass = filterDef.getFilterClass();
        Loader classLoader = null;
        classLoader = context.getLoader();
        ClassLoader oldCtxClassLoader = Thread.currentThread().getContextClassLoader();
        // 实例化这个过滤器的新实例并返回
        Class<?> clazz = classLoader.getClassLoader().loadClass(filterClass);
        this.filter = (Filter) clazz.getConstructor().newInstance();
        filter.init(this);
        return (this.filter);
    }

    FilterDef getFilterDef() {
        return (this.filterDef);
    }

    void release() {
        if (this.filter != null) {
            filter.destroy();
        }
        this.filter = null;
    }

    void setFilterDef(FilterDef filterDef) throws ClassCastException, ClassNotFoundException, IllegalAccessException, InstantiationException, ServletException, InvocationTargetException, NoSuchMethodException {
        this.filterDef = filterDef;
        if (filterDef == null) {
            // 释放之前分配的所有过滤器实例
            if (this.filter != null) this.filter.destroy();
            this.filter = null;
        } else {
            // 分配一个新的过滤器实例
            Filter filter = getFilter();
        }
    }

    @Override
    public String getFilterName() {
        return filterDef.getFilterName();
    }

    @Override
    public ServletContext getServletContext() {
        return (this.context.getServletContext());
    }

    @Override
    public String getInitParameter(String name) {
        Map<String, String> map = filterDef.getParameterMap();
        if (map == null) {
            return (null);
        } else {
            return map.get(name);
        }
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        Map<String, String> map = filterDef.getParameterMap();
        if (map == null) {
            return Collections.enumeration(new ArrayList<>());
        } else {
            return (Collections.enumeration(map.keySet()));
        }
    }

    @Override
    public String toString() {
        return ("ApplicationFilterConfig[" + "name=" +
                filterDef.getFilterName() +
                ", filterClass=" +
                filterDef.getFilterClass() +
                "]");

    }
}
