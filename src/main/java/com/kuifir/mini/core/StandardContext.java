package com.kuifir.mini.core;

import com.kuifir.mini.*;
import com.kuifir.mini.connector.http.HttpConnector;
import com.kuifir.mini.logger.FileLogger;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardContext extends ContainerBase implements Context {

    // 与本容器关联的connector
    HttpConnector connector;
    //包含servlet类和实例的map
    Map<String, String> servletClsMap = new ConcurrentHashMap<>(); //servletName - ServletClassName
    Map<String, StandardWrapper> servletInstanceMap = new ConcurrentHashMap<>();//servletName - servlet
    //下面的属性记录了filter的配置
    private Map<String, ApplicationFilterConfig> filterConfigs = new ConcurrentHashMap<>();
    private Map<String, FilterDef> filterDefs = new ConcurrentHashMap<>();
    private FilterMap[] filterMaps = new FilterMap[0];

    private ArrayList<ContainerListenerDef> listenerDefs = new ArrayList<>();
    private ArrayList<ServletContextListener> listeners = new ArrayList<>();

    private Map<String, String> initParametersMap = new ConcurrentHashMap<>();
    private ServletContext servletContext;
    private Map<String, Map<String, String>> servletInitParametersMap = new ConcurrentHashMap<>();
    private Map<String, String> servletMappingMap = new ConcurrentHashMap<>();
    public StandardContext() {
        super();
        pipeline.setBasic(new StandardContextValve(this));
        log("Container created.");
    }

    public void start() {
        // 触发一个容器启动事件
        fireContainerEvent("Container Started", this);
        // 添加日志组件
        Logger logger = new FileLogger();
        setLogger(logger);
        //scan web.xml
        String file = System.getProperty("minit.base") + File.separator +
                this.docbase + File.separator + "WEB-INF" + File.separator + "web.xml";
        SAXReader reader = new SAXReader();
        Document document;
        try {
            document = reader.read(file);
            Element root = document.getRootElement();

            List<Element> contextParams = root.elements("context-param");

            for (Element contextParam : contextParams) {
                Element paramNameElement = contextParam.element("param-name");
                String paramName = paramNameElement.getText();
                Element paramValueElement = contextParam.element("param-value");
                String fileternamestr = paramValueElement.getText();
                this.initParametersMap.put(paramName, fileternamestr);
            }
            this.servletContext = new StandardServletContext(this.docbase, this.initParametersMap);
            //listeners
            List<Element> listeners = root.elements("listener");
            for (Element listener : listeners) {
                Element listenerclass = listener.element("listener-class");
                String listenerclassname = listenerclass.getText();
                System.out.println("listenerclassname: " + listenerclassname);

                //load listeners
                ContainerListenerDef listenerDef = new ContainerListenerDef();
                listenerDef.setListenerName(listenerclassname);
                listenerDef.setListenerClass(listenerclassname);
                addListenerDef(listenerDef);
            }
            listenerStart();

            //filters
            List<Element> filters = root.elements("filter");
            for (Element filter : filters) {
                Element filetername = filter.element("filter-name");
                String fileternamestr = filetername.getText();
                Element fileterclass = filter.element("filter-class");
                String fileterclassstr = fileterclass.getText();
                System.out.println("filter " + fileternamestr + fileterclassstr);

                //load filters
                FilterDef filterDef = new FilterDef();
                filterDef.setFilterName(fileternamestr);
                filterDef.setFilterClass(fileterclassstr);
                addFilterDef(filterDef);
            }

            //filter mappings
            List<Element> filtermaps = root.elements("filter-mapping");
            for (Element filtermap : filtermaps) {
                Element filetername = filtermap.element("filter-name");
                String fileternamestr = filetername.getText();
                Element urlpattern = filtermap.element("url-pattern");
                String urlpatternstr = urlpattern.getText();
                System.out.println("filter mapping " + fileternamestr + urlpatternstr);

                FilterMap filterMap = new FilterMap();
                filterMap.setFilterName(fileternamestr);
                filterMap.setURLPattern(urlpatternstr);
                addFilterMap(filterMap);
            }

            filterStart();

            //servlet
            List<Element> servlets = root.elements("servlet");
            for (Element servlet : servlets) {
                Element servletname = servlet.element("servlet-name");
                String servletnamestr = servletname.getText();
                Element servletclass = servlet.element("servlet-class");
                String servletclassstr = servletclass.getText();

                Element servletInitParamElement = servlet.element("init-param");
                Element servletInitParamNameElement = servletInitParamElement.element("param-name");
                String servletInitParamName = servletInitParamNameElement.getText();
                Element servletInitParamValueElement = servletInitParamElement.element("param-value");
                String servletInitParamValue = servletInitParamValueElement.getText();
                Map<String, String> servletInitParamMap = new ConcurrentHashMap();
                servletInitParamMap.put(servletInitParamName, servletInitParamValue);
                this.servletInitParametersMap.put(servletclassstr, servletInitParamMap);

                Element loadonstartup = servlet.element("load-on-startup");
                String loadonstartupstr = null;
                if (loadonstartup != null) {
                    loadonstartupstr = loadonstartup.getText();
                }

                System.out.println("servlet " + servletnamestr + servletclassstr);

                this.servletClsMap.put(servletnamestr, servletclassstr);
                if (loadonstartupstr != null) {
                    getWrapper(servletnamestr);
                }
            }
            List<Element> servletMappings = root.elements("servlet-mapping");

            for (Element servletMapping : servletMappings) {
                Element servletname = servletMapping.element("servlet-name");
                String servletnamestr = servletname.getText();
                Element servletclass = servletMapping.element("url-pattern");
                String urlPatternStr = servletclass.getText();
                this.servletMappingMap.put(urlPatternStr, servletnamestr);
            }
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("Context started.........");

//        // 添加过滤器
//        FilterDef filterDef = new FilterDef();
//        filterDef.setFilterName("TestFilter");
//        filterDef.setFilterClass("test.TestFilter");
//        addFilterDef(filterDef);
//
//        FilterMap filterMap = new FilterMap();
//        filterMap.setFilterName("TestFilter");
//        filterMap.setURLPattern("/*");
//        addFilterMap(filterMap);
//        filterStart();
//
//        // 添加监听器
//        ContainerListenerDef listenerDef = new ContainerListenerDef();
//        listenerDef.setListenerName("TestListener");
//        listenerDef.setListenerClass("test.TestListener");
//        addListenerDef(listenerDef);
//        listenerStart();
    }

    public void addContainerListener(ServletContextListener listener) {
        // 添加一个新的容器监听器到监听器列表，并确保线程安全
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeContainerListener(ContainerListener listener) {
        // 移除指定的容器监听器，并确保线程安全
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void fireContainerEvent(String type, Object data) {
        // 检查是否已经有监听器，如果没有则直接返回
        if (listeners.isEmpty()) {
            return;
        }
        ContainerEvent event = new ContainerEvent(this, type, data);
        ContainerListener[] list = new ContainerListener[0];
        synchronized (listeners) {
            list = listeners.toArray(list);
        }
        // 遍历所有监听器并触发事件
        for (ContainerListener containerListener : list) {
            containerListener.containerEvent(event);
        }
    }

    public void addListenerDef(ContainerListenerDef listenererDef) {
        synchronized (listenerDefs) {
            listenerDefs.add(listenererDef);
        }
    }

    public boolean listenerStart() {
        ServletContextEvent servletContextEvent = new ServletContextEvent(this.servletContext);
        System.out.println("Listener Start..........");
        boolean ok = true;
        synchronized (listeners) {
            listeners.clear();
            for (ContainerListenerDef def : listenerDefs) {
                ServletContextListener listener = null;
                try {
                    // 确定我们将要使用的类加载器
                    String listenerClass = def.getListenerClass();
                    Loader classLoader = null;
                    classLoader = this.getLoader();
                    ClassLoader oldCtxClassLoader = Thread.currentThread().getContextClassLoader();
                    // 创建这个过滤器的新实例并返回它
                    Class<?> clazz = classLoader.getClassLoader().loadClass(listenerClass);
                    listener = (ServletContextListener) clazz.getConstructor().newInstance();
                    addContainerListener(listener);
                    listener.contextInitialized(servletContextEvent);
                } catch (Throwable t) {
                    t.printStackTrace();
                    ok = false;
                }
            }
        }
        return (ok);
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
            String servletClassName = servletClsMap.get(name);
            if(StringUtils.isEmpty(servletClassName)){
                servletClassName = name;
            }
            System.out.println(servletClassName);
            servletWrapper = new StandardWrapper(servletClassName, this);
            this.servletClsMap.put(name, servletClassName);
            this.servletInstanceMap.put(name, servletWrapper);
        }
        return servletWrapper;
    }

    public void addFilterDef(FilterDef filterDef) {
        filterDefs.put(filterDef.getFilterName(), filterDef);
    }

    public void addFilterMap(FilterMap filterMap) {
        // 验证所建议的过滤器映射
        String filterName = filterMap.getFilterName();
        String servletName = filterMap.getServletName();
        String urlPattern = filterMap.getURLPattern();
        if (findFilterDef(filterName) == null)
            throw new IllegalArgumentException("standardContext.filterMap.name" + filterName);
        if ((servletName == null) && (urlPattern == null))
            throw new IllegalArgumentException("standardContext.filterMap.either");
        if ((servletName != null) && (urlPattern != null))
            throw new IllegalArgumentException("standardContext.filterMap.either");
        // 因为过滤器模式是2.3中的新功能，所以不需要调整
        // 对于2.2版本的向后兼容性
        if ((urlPattern != null) && !validateURLPattern(urlPattern))
            throw new IllegalArgumentException("standardContext.filterMap.pattern" + urlPattern);
        // 将这个过滤器映射添加到我们已注册的集合中
        synchronized (filterMaps) {
            FilterMap[] results = new FilterMap[filterMaps.length + 1];
            System.arraycopy(filterMaps, 0, results, 0, filterMaps.length);
            results[filterMaps.length] = filterMap;
            filterMaps = results;
        }
    }

    public FilterDef findFilterDef(String filterName) {
        return filterDefs.get(filterName);
    }

    public FilterDef[] findFilterDefs() {
        synchronized (filterDefs) {
            FilterDef results[] = new FilterDef[filterDefs.size()];
            return ((FilterDef[]) filterDefs.values().toArray(results));
        }
    }

    public FilterMap[] findFilterMaps() {
        return (filterMaps);
    }

    public void removeFilterDef(FilterDef filterDef) {
        filterDefs.remove(filterDef.getFilterName());
    }

    public void removeFilterMap(FilterMap filterMap) {
        synchronized (filterMaps) {
            // 确保当前存在这个过滤器映射
            int n = -1;
            for (int i = 0; i < filterMaps.length; i++) {
                if (filterMaps[i] == filterMap) {
                    n = i;
                    break;
                }
            }
            if (n < 0) {
                return;
            }
            // 删除指定的过滤器映射
            FilterMap[] results = new FilterMap[filterMaps.length - 1];
            System.arraycopy(filterMaps, 0, results, 0, n);
            System.arraycopy(filterMaps, n + 1, results, n, (filterMaps.length - 1) - n);
            filterMaps = results;
        }
    }

    //对配置好的所有filter名字，创建实例，存储在filterConfigs中，可以生效了
    public boolean filterStart() {
        System.out.println("Filter Start..........");
        // 为每个定义的过滤器实例化并记录一个FilterConfig
        boolean ok = true;
        synchronized (filterConfigs) {
            filterConfigs.clear();
            for (String name : filterDefs.keySet()) {
                ApplicationFilterConfig filterConfig = null;
                try {
                    filterConfig = new ApplicationFilterConfig(this, filterDefs.get(name));
                    filterConfigs.put(name, filterConfig);
                } catch (Throwable t) {
                    ok = false;
                }
            }
        }
        return (ok);
    }

    public FilterConfig findFilterConfig(String name) {
        return (filterConfigs.get(name));
    }

    private boolean validateURLPattern(String urlPattern) {
        if (urlPattern == null) return (false);
        if (urlPattern.startsWith("*.")) {
            return urlPattern.indexOf('/') < 0;
        }
        return urlPattern.startsWith("/");
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
        return this.docbase;
    }

    @Override
    public void setDocBase(String docBase) {
        this.docbase =docBase;
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
        return servletContext;
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

    public Map<String, Map<String, String>> getServletInitParametersMap() {
        return servletInitParametersMap;
    }

    public void setServletInitParametersMap(Map<String, Map<String, String>> servletInitParametersMap) {
        this.servletInitParametersMap = servletInitParametersMap;
    }

    public Map<String, String> getServletMappingMap() {
        return servletMappingMap;
    }

    public void setServletMappingMap(Map<String, String> servletMappingMap) {
        this.servletMappingMap = servletMappingMap;
    }
}
