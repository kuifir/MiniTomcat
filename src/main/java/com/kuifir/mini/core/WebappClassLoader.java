package com.kuifir.mini.core;

import com.kuifir.mini.Container;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

public class WebappClassLoader {
    ClassLoader classLoader;
    String path;
    String docbase;
    Container container;

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDocbase() {
        return docbase;
    }

    public void setDocbase(String docbase) {
        this.docbase = docbase;
    }

    public WebappClassLoader() {
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public String getInfo() {
        return "A simple loader";
    }

    public void addRepository(String repository) {
    }

    public String[] findRepositories() {
        return null;
    }

    public synchronized void start() {
        System.out.println("Starting WebappLoader");
        try {
            // 创建一个 URLClassLoader
            URL[] urls = new URL[1];
            //设置这个URLClassloader的工作目录
            File classpath = new File(System.getProperty("minit.base"));
            String repository = classpath.getCanonicalPath() + File.separator;
            if (docbase != null && !docbase.isEmpty()) {
                repository = repository + docbase + File.separator;
            }
            urls[0] = Paths.get(repository).toUri().toURL();
            System.out.println("Webapp classloader Repository : " + repository);
            classLoader = new URLClassLoader(urls);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
    }
}