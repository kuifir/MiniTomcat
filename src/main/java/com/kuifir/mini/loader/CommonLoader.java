package com.kuifir.mini.loader;

import com.kuifir.mini.Container;
import com.kuifir.mini.Loader;

import java.io.File;
import java.net.URL;
import java.net.URLStreamHandler;

public class CommonLoader implements Loader {
    ClassLoader classLoader;
    ClassLoader parent;
    String path;
    String docbase;
    Container container;
    public CommonLoader() {
    }
    public CommonLoader(ClassLoader parent) {
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
        System.out.println("Starting Common Loader, docbase: " + docbase);
        try {
            // 创建一个URLClassLoader
            //类加载目录是minit安装目录下的lib目录
            URL[] urls = new URL[1];
            File classPath = new File(System.getProperty("minit.home"));
            URLStreamHandler streamHandler = null;
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString() ;
            repository = repository + "lib" + File.separator;
            urls[0] = new URL(null, repository, streamHandler);
            System.out.println("Common classloader Repository : "+repository);
            classLoader = new CommonClassLoader(urls);
        }
        catch (Exception e) {
            System.out.println(e.toString() );
        }
    }
    public void stop() {
    }
}