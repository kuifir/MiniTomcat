package com.kuifir.mini.core;

import com.kuifir.mini.Container;
import com.kuifir.mini.Loader;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

public class WebappLoader implements Loader {
    ClassLoader classLoader;
    ClassLoader parent;
    String path;
    String docbase;
    Container container;

    public WebappLoader(String docbase) {
        this.docbase = docbase;
    }

    public WebappLoader(String docbase, ClassLoader parent) {
        this.docbase = docbase;
        this.parent = parent;
    }

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
            // create a URLClassLoader
            //加载目录是minit.base规定的根目录，加上应用目录，
            //然后之下的WEB-INF/classes目录
            //这意味着每一个应用有自己的类加载器，达到隔离的目的
            URL[] urls = new URL[1];
            File classPath = new File(System.getProperty("minit.base"));
            String repository = classPath.getCanonicalPath() + File.separator;
            if (docbase != null && !docbase.isEmpty()) {
                repository = repository + docbase + File.separator;
            }
            repository = repository + "WEB-INF" + File.separator + "classes" + File.separator;
            urls[0] = Paths.get(repository).toUri().toURL();
            System.out.println("Webapp classloader Repository : " + repository);
            classLoader = new WebappClassLoader(urls, parent);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void stop() {
    }
}