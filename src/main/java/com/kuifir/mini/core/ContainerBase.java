package com.kuifir.mini.core;

import com.kuifir.mini.Container;
import com.kuifir.mini.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ContainerBase implements Container {
    //子容器
    protected final Map<String, Container> children = new ConcurrentHashMap<>();
    //类加载器
    protected ClassLoader loader = null;
    protected String name = null;
    //父容器
    protected Container parent = null;

    //ContainerBase中增加与日志相关的代码
    protected Logger logger = null;

    //下面是基本的get和set方法
    public abstract String getInfo();

    protected void log(String message) {
        Logger logger = getLogger();
        if (logger != null) {
            logger.log(logName() + ": " + message);
        } else {
            System.out.println(logName() + ": " + message);
        }
    }

    protected void log(String message, Throwable throwable) {
        Logger logger = getLogger();
        if (logger != null) {
            logger.log(logName() + ": " + message, throwable);
        } else {
            System.out.println(logName() + ": " + message + ": " + throwable);
            throwable.printStackTrace(System.out);
        }
    }

    protected String logName() {
        String className = this.getClass().getName();
        int period = className.lastIndexOf(".");
        if (period >= 0) className = className.substring(period + 1);
        return (className + "[" + getName() + "]");
    }

    public ClassLoader getLoader() {
        if (loader != null)
            return (loader);
        if (parent != null)
            return (parent.getLoader());
        return (null);
    }

    public synchronized void setLoader(ClassLoader loader) {
        ClassLoader oldLoader = this.loader;
        if (oldLoader == loader) {
            return;
        }
        this.loader = loader;
    }

    public String getName() {
        return (name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Container getParent() {
        return (parent);
    }

    public void setParent(Container container) {
        Container oldParent = this.parent;
        this.parent = container;
    }

    //下面是对children map的增删改查操作
    public void addChild(Container child) {
        addChildInternal(child);
    }

    private void addChildInternal(Container child) {
        synchronized (children) {
            if (children.get(child.getName()) != null)
                throw new IllegalArgumentException("addChild:  Child name '" +
                        child.getName() +
                        "' is not unique");
            child.setParent(this);
            children.put(child.getName(), child);
        }
    }

    public Container findChild(String name) {
        if (name == null)
            return (null);
        synchronized (children) {       // Required by post-start changes
            return children.get(name);
        }
    }

    public Container[] findChildren() {
        synchronized (children) {
            Container[] results = new Container[children.size()];
            return children.values().toArray(results);
        }
    }

    public void removeChild(Container child) {
        synchronized (children) {
            if (children.get(child.getName()) == null)
                return;
            children.remove(child.getName());
        }
        child.setParent(null);
    }

    @Override
    public Logger getLogger() {
        if (logger != null) return (logger);
        if (parent != null) return (parent.getLogger());
        return (null);
    }

    @Override
    public void setLogger(Logger logger) {
        Logger oldLogger = this.logger;
        if (oldLogger == logger) return;
        this.logger = logger;
    }
}