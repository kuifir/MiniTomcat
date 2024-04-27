package com.kuifir.mini.core;

import com.kuifir.mini.ContainerEvent;
import com.kuifir.mini.ContainerListener;
import com.kuifir.mini.Request;
import com.kuifir.mini.Response;
import com.kuifir.mini.connector.http.HttpConnector;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardHost extends ContainerBase {
    HttpConnector connector = null;
    //host中用一个map存储了所管理的context，一个context代表了一个独立的web应用
    Map<String, StandardContext> contextMap = new ConcurrentHashMap<>();//contextName - servletContext
    //下面的listener是host本身的监听
    private ArrayList<ContainerListenerDef> listenerDefs = new ArrayList<>();
    private ArrayList<ContainerListener> listeners = new ArrayList<>();

    public StandardHost() {
        super();
        pipeline.setBasic(new StandardHostValve());
        log("Host created.");
    }

    public String getInfo() {
        return "Minit host, vesion 0.1";
    }

    public HttpConnector getConnector() {
        return connector;
    }

    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        System.out.println("StandardHost invoke()");
        super.invoke(request, response);
    }

    //从host中根据context名(路径名)找到对应的context
    //如果找不到就新建一个context
    public StandardContext getContext(String name) {
        StandardContext context = contextMap.get(name);
        if (context == null) {
            //创建新的context，有自己独立的根目录和类加载器
            context = new StandardContext();
            context.setDocBase(name);
            context.setConnector(connector);
            WebappClassLoader loader = new WebappClassLoader();
            context.setLoader(loader);
            loader.start();
            this.contextMap.put(name, context);
        }
        return context;
    }

    //host的启动方法，现在没有做什么事情，仅仅是启用监听器
    //在MiniTomcat中，Host是一个极简化的形态
    public void start() {
        fireContainerEvent("Host Started", this);

        // 添加监听器
        ContainerListenerDef listenerDef = new ContainerListenerDef();
        listenerDef.setListenerName("TestListener");
        listenerDef.setListenerClass("test.TestListener");
        addListenerDef(listenerDef);
        listenerStart();
    }

    public void addContainerListener(ContainerListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeContainerListener(ContainerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void fireContainerEvent(String type, Object data) {
        if (listeners.isEmpty())
            return;
        ContainerEvent event = new ContainerEvent(this, type, data);
        ContainerListener list[] = new ContainerListener[0];
        synchronized (listeners) {
            list = listeners.toArray(list);
        }
        for (ContainerListener containerListener : list) {
            containerListener.containerEvent(event);
        }
    }

    public void addListenerDef(ContainerListenerDef listenererDef) {
        synchronized (listenerDefs) {
            listenerDefs.add(listenererDef);
        }
    }

    //初始化监听器
    public boolean listenerStart() {
        System.out.println("Listener Start..........");
        boolean ok = true;
        synchronized (listeners) {
            listeners.clear();
            Iterator<ContainerListenerDef> defs = listenerDefs.iterator();
            while (defs.hasNext()) {
                ContainerListenerDef def = defs.next();
                ContainerListener listener = null;
                try {
                    // Identify the class loader we will be using
                    String listenerClass = def.getListenerClass();
                    WebappClassLoader classLoader = null;
                    //host对应的loader就是listener的loader
                    classLoader = this.getLoader();
                    ClassLoader oldCtxClassLoader =
                            Thread.currentThread().getContextClassLoader();
                    // Instantiate a new instance of this filter and return it
                    Class<?> clazz = classLoader.getClassLoader().loadClass(listenerClass);
                    listener = (ContainerListener) clazz.newInstance();
                    addContainerListener(listener);
                } catch (Throwable t) {
                    t.printStackTrace();
                    ok = false;
                }
            }
        }
        return (ok);
    }
}
